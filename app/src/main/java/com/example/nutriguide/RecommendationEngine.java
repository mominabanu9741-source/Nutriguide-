package com.example.nutriguide;

import java.util.ArrayList;
import java.util.List;

public class RecommendationEngine {

    // Checks a food against the user's health conditions.
    // Returns a list of reasons the food is NOT recommended (empty list = suitable).
    public static List<String> checkSuitability(Food food, List<String> conditions) {
        List<String> reasons = new ArrayList<>();

        if (food == null || conditions == null) {
            return reasons;
        }

        for (String condition : conditions) {
            String c = condition.toLowerCase();

            if (c.contains("diabetes")) {
                if (food.sugar > 10) {
                    reasons.add("High sugar (" + food.sugar + "g) — not ideal for diabetes");
                }
            }

            if (c.contains("hypertension")) {
                if (food.sodium > 0.4) { // grams
                    reasons.add("High sodium (" + food.sodium + "g) — not ideal for hypertension");
                }
            }

            if (c.contains("pcos")) {
                if (food.carbs > 30 && food.fiber < 3) {
                    reasons.add("High refined carbs, low fiber — not ideal for PCOS");
                }
            }

            if (c.contains("obesity")) {
                if (food.calories > 250) {
                    reasons.add("High calorie density (" + food.calories + " kcal) — not ideal for weight management");
                }
            }
        }

        return reasons;
    }
}
