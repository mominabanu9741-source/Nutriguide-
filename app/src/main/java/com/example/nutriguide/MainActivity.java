package com.example.nutriguide;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@androidx.camera.core.ExperimentalGetImage
public class MainActivity extends AppCompatActivity {

    private TextView tvGreeting, tvConditions, tvAllergies, tvBmi, tvBmr, tvSuggestions, tvTodayCalories;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        tvGreeting = findViewById(R.id.tvGreeting);
        tvConditions = findViewById(R.id.tvConditions);
        tvAllergies = findViewById(R.id.tvAllergies);
        tvBmi = findViewById(R.id.tvBmi);
        tvBmr = findViewById(R.id.tvBmr);
        tvSuggestions = findViewById(R.id.tvSuggestions);
        tvTodayCalories = findViewById(R.id.tvTodayCalories);

        Button btnSearchFood = findViewById(R.id.btnSearchFood);
        Button btnScan = findViewById(R.id.btnScan);
        Button btnEditProfile = findViewById(R.id.btnEditProfile);
        Button btnLogMeal = findViewById(R.id.btnLogMeal);

        btnSearchFood.setOnClickListener(v ->
                startActivity(new android.content.Intent(this, FoodSearchActivity.class)));

        btnScan.setOnClickListener(v ->
                startActivity(new android.content.Intent(this, ScannerActivity.class)));

        btnEditProfile.setOnClickListener(v ->
                startActivity(new android.content.Intent(this, ProfileSetupActivity.class)));

        btnLogMeal.setOnClickListener(v ->
                startActivity(new android.content.Intent(this, MealLogActivity.class)));

        loadProfile();
        loadTodayCalories();
        requestNotificationPermission();
        scheduleWaterReminder();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTodayCalories(); // refresh whenever user returns from Meal Log screen
    }

    private void loadTodayCalories() {
        String uid = auth.getCurrentUser().getUid();

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date startOfDay = cal.getTime();

        db.collection("mealLogs")
                .whereEqualTo("userId", uid)
                .whereGreaterThanOrEqualTo("timestamp", startOfDay)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    double total = 0;
                    for (var doc : snapshots) {
                        Double cal2 = doc.getDouble("calories");
                        if (cal2 != null) total += cal2;
                    }
                    tvTodayCalories.setText(String.format("Today's intake: %.0f kcal", total));
                })
                .addOnFailureListener(e ->
                        tvTodayCalories.setText("Today's intake: -- kcal"));
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 200);
            }
        }
    }

    private void scheduleWaterReminder() {
        PeriodicWorkRequest waterRequest = new PeriodicWorkRequest.Builder(
                WaterReminderWorker.class, 15, TimeUnit.MINUTES)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "water_reminder",
                ExistingPeriodicWorkPolicy.KEEP,
                waterRequest
        );
    }

    private void loadProfile() {
        String uid = auth.getCurrentUser().getUid();

        db.collection("users").document(uid)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null || !snapshot.exists()) return;

                    List<String> conditions = (List<String>) snapshot.get("conditions");
                    List<String> allergies = (List<String>) snapshot.get("allergies");
                    Double height = snapshot.getDouble("height");
                    Double weight = snapshot.getDouble("weight");
                    Long age = snapshot.getLong("age");
                    String gender = snapshot.getString("gender");

                    tvGreeting.setText("Welcome back");
                    tvConditions.setText("Conditions: " + (conditions != null ? conditions : "none"));
                    tvAllergies.setText("Allergies: " + (allergies != null ? allergies : "none"));

                    if (height != null && weight != null && height > 0) {
                        double heightM = height / 100.0;
                        double bmi = weight / (heightM * heightM);
                        tvBmi.setText(String.format("BMI: %.1f", bmi));
                    }

                    // --- BMR calculation (Mifflin-St Jeor formula) ---
                    if (height != null && weight != null && age != null && gender != null) {
                        double bmr;
                        if (gender.equalsIgnoreCase("male")) {
                            bmr = (10 * weight) + (6.25 * height) - (5 * age) + 5;
                        } else {
                            bmr = (10 * weight) + (6.25 * height) - (5 * age) - 161;
                        }
                        tvBmr.setText(String.format("BMR: %.0f kcal/day", bmr));
                    }

                    // --- Healthy food suggestions ---
                    List<String> suggestions = SuggestionHelper.getSuggestions(conditions);
                    if (!suggestions.isEmpty()) {
                        StringBuilder sb = new StringBuilder("Suggested for you:\n");
                        for (String s : suggestions) {
                            sb.append("• ").append(s).append("\n");
                        }
                        tvSuggestions.setText(sb.toString());
                    }
                });
    }
}