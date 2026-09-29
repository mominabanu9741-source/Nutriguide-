package com.example.nutriguide;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MealLogActivity extends AppCompatActivity {

    private EditText etFoodName, etCalories;
    private Button btnLogMeal;
    private TextView tvMealHistory;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_log);

        etFoodName = findViewById(R.id.etFoodName);
        etCalories = findViewById(R.id.etCalories);
        btnLogMeal = findViewById(R.id.btnLogMeal);
        tvMealHistory = findViewById(R.id.tvMealHistory);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        btnLogMeal.setOnClickListener(v -> saveMeal());

        loadTodaysMeals();
    }

    private void saveMeal() {
        String foodName = etFoodName.getText().toString().trim();
        String calStr = etCalories.getText().toString().trim();

        if (foodName.isEmpty() || calStr.isEmpty()) {
            Toast.makeText(this, "Enter food name and calories", Toast.LENGTH_SHORT).show();
            return;
        }

        double calories = Double.parseDouble(calStr);
        String uid = auth.getCurrentUser().getUid();

        Map<String, Object> meal = new HashMap<>();
        meal.put("foodName", foodName);
        meal.put("calories", calories);
        meal.put("timestamp", new Date());
        meal.put("userId", uid);

        db.collection("mealLogs")
                .add(meal)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Meal logged", Toast.LENGTH_SHORT).show();
                    etFoodName.setText("");
                    etCalories.setText("");
                    loadTodaysMeals();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void loadTodaysMeals() {
        String uid = auth.getCurrentUser().getUid();

        db.collection("mealLogs")
                .whereEqualTo("userId", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(snapshots -> {
                    StringBuilder sb = new StringBuilder();
                    double totalCalories = 0;
                    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());

                    for (var doc : snapshots) {
                        String name = doc.getString("foodName");
                        Double cal = doc.getDouble("calories");
                        Date time = doc.getDate("timestamp");

                        if (cal != null) totalCalories += cal;

                        sb.append(name)
                                .append(" — ")
                                .append(cal)
                                .append(" kcal");
                        if (time != null) {
                            sb.append(" (").append(sdf.format(time)).append(")");
                        }
                        sb.append("\n");
                    }

                    sb.append("\nTotal: ").append(totalCalories).append(" kcal");
                    tvMealHistory.setText(sb.length() > 0 ? sb.toString() : "No meals logged yet");
                })
                .addOnFailureListener(e ->
                        tvMealHistory.setText("Error loading meals: " + e.getMessage()));
    }
}
