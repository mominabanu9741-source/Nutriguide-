package com.example.nutriguide;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@androidx.camera.core.ExperimentalGetImage
public class ScannerActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 100;

    private PreviewView previewView;
    private TextView tvScanStatus;
    private ExecutorService cameraExecutor;
    private BarcodeScanner scanner;
    private boolean scanHandled = false;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private List<String> userAllergies = new ArrayList<>();
    private List<String> userConditions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        previewView = findViewById(R.id.previewView);
        tvScanStatus = findViewById(R.id.tvScanStatus);

        scanner = BarcodeScanning.getClient();
        cameraExecutor = Executors.newSingleThreadExecutor();

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        loadUserProfile();



        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
    }

    private void loadUserProfile() {
        String uid = auth.getCurrentUser().getUid();
        db.collection("users").document(uid).get().addOnSuccessListener(snapshot -> {
            List<String> allergies = (List<String>) snapshot.get("allergies");
            if (allergies != null) userAllergies = allergies;

            List<String> conditions = (List<String>) snapshot.get("conditions");
            if (conditions != null) userConditions = conditions;
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to scan", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                androidx.camera.core.Preview preview = new androidx.camera.core.Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, this::analyzeImage);

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageAnalysis);

            } catch (Exception e) {
                Toast.makeText(this, "Camera error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void analyzeImage(@NonNull ImageProxy imageProxy) {
        if (scanHandled || imageProxy.getImage() == null) {
            imageProxy.close();
            return;
        }

        InputImage image = InputImage.fromMediaImage(
                imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());

        scanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    for (Barcode barcode : barcodes) {
                        String rawValue = barcode.getRawValue();
                        if (rawValue != null && !scanHandled) {
                            scanHandled = true;
                            runOnUiThread(() -> lookupBarcode(rawValue));
                        }
                    }
                })
                .addOnFailureListener(e -> {})
                .addOnCompleteListener(task -> imageProxy.close());
    }

    private void lookupBarcode(String barcode) {
        tvScanStatus.setText("Found barcode: " + barcode + " — looking up...");

        RetrofitClient.getApiService().getProductByBarcode(barcode).enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                if (response.body() == null || response.body().product == null) {
                    tvScanStatus.setText("Product not found for this barcode");
                    scanHandled = false;
                    return;
                }

                Product p = response.body().product;
                StringBuilder sb = new StringBuilder();
                sb.append("Name: ").append(p.productName != null ? p.productName : "Unknown").append("\n");

                if (p.nutriments != null) {
                    sb.append("Calories: ").append(p.nutriments.calories).append(" kcal\n");
                    sb.append("Sugar: ").append(p.nutriments.sugar).append(" g\n");
                    sb.append("Sodium: ").append(p.nutriments.sodium).append(" g\n");
                }

                // --- Allergy check ---
                if (p.allergensTags != null) {
                    List<String> matched = AllergyChecker.checkAllergies(p.allergensTags, userAllergies);
                    if (!matched.isEmpty()) {
                        sb.append("\n⚠ WARNING: Contains allergen(s) you flagged: ").append(matched).append("\n");
                    } else {
                        sb.append("\n✓ No matched allergens for your profile.\n");
                    }
                }

                // --- Recommendation check ---
                if (p.nutriments != null) {
                    Food food = new Food(
                            p.productName != null ? p.productName : "Unknown",
                            p.nutriments.calories,
                            p.nutriments.carbs,
                            p.nutriments.sugar,
                            p.nutriments.sodium,
                            p.nutriments.fiber,
                            p.allergensTags
                    );

                    List<String> concerns = RecommendationEngine.checkSuitability(food, userConditions);
                    if (!concerns.isEmpty()) {
                        sb.append("\n⚠ Not fully recommended for your conditions:\n");
                        for (String reason : concerns) {
                            sb.append("- ").append(reason).append("\n");
                        }
                    } else {
                        sb.append("\n✓ Suitable for your health conditions.\n");
                    }
                }

                tvScanStatus.setText(sb.toString());
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                tvScanStatus.setText("Error: " + t.getMessage());
                scanHandled = false;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}