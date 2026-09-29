package com.example.nutriguide;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FoodSearchActivity extends AppCompatActivity {

    private EditText etSearchQuery;
    private Button btnSearch;
    private TextView tvResult;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private List<String> userAllergies = new ArrayList<>();
    private List<String> userConditions = new ArrayList<>(); // NEW

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_search);

        etSearchQuery = findViewById(R.id.etSearchQuery);
        btnSearch = findViewById(R.id.btnSearch);
        tvResult = findViewById(R.id.tvResult);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        loadUserProfile();

        btnSearch.setOnClickListener(v -> search());
    }

    private void loadUserProfile() {
        String uid = auth.getCurrentUser().getUid();
        db.collection("users").document(uid).get().addOnSuccessListener(snapshot -> {
            List<String> allergies = (List<String>) snapshot.get("allergies");
            if (allergies != null) userAllergies = allergies;

            List<String> conditions = (List<String>) snapshot.get("conditions"); // NEW
            if (conditions != null) userConditions = conditions;
        });
    }

    private void search() {
        String query = etSearchQuery.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(this, "Type something to search", Toast.LENGTH_SHORT).show();
            return;
        }

        tvResult.setText("Searching...");

        RetrofitClient.getApiService().searchFood(query).enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                if (response.body() == null || response.body().products == null || response.body().products.isEmpty()) {
                    tvResult.setText("No results found");
                    return;
                }

                List<Product> products = response.body().products;
                Product first = products.get(0);

                StringBuilder sb = new StringBuilder();
                sb.append("Name: ").append(first.productName != null ? first.productName : "Unknown").append("\n");

                if (first.nutriments != null) {
                    sb.append("Calories: ").append(first.nutriments.calories).append(" kcal\n");
                    sb.append("Carbs: ").append(first.nutriments.carbs).append(" g\n");
                    sb.append("Sugar: ").append(first.nutriments.sugar).append(" g\n");
                    sb.append("Sodium: ").append(first.nutriments.sodium).append(" g\n");
                }

                // --- Allergy check ---
                if (first.allergensTags != null) {
                    List<String> matched = AllergyChecker.checkAllergies(first.allergensTags, userAllergies);
                    if (!matched.isEmpty()) {
                        sb.append("\n⚠ WARNING: Contains allergen(s) you flagged: ").append(matched).append("\n");
                    } else {
                        sb.append("\n✓ No matched allergens for your profile.\n");
                    }
                }

                // --- Recommendation check (NEW) ---
                if (first.nutriments != null) {
                    Food food = new Food(
                            first.productName != null ? first.productName : "Unknown",
                            first.nutriments.calories,
                            first.nutriments.carbs,
                            first.nutriments.sugar,
                            first.nutriments.sodium,
                            first.nutriments.fiber,
                            first.allergensTags
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

                tvResult.setText(sb.toString());
            }

            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
                tvResult.setText("Error: " + t.getMessage());
            }
        });
    }
}