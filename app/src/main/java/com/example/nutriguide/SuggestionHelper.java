package com.example.nutriguide;

import java.util.ArrayList;
import java.util.List;

public class SuggestionHelper {

    // Returns suggested food categories based on the user's health conditions.
    public static List<String> getSuggestions(List<String> conditions) {
        List<String> suggestions = new ArrayList<>();

        if (conditions == null) return suggestions;

        for (String condition : conditions) {
            String c = condition.toLowerCase();

            if (c.contains("diabetes")) {
                suggestions.add("Low-GI foods (oats, lentils, whole grains)");
                suggestions.add("Leafy greens and non-starchy vegetables");
            }

            if (c.contains("hypertension")) {
                suggestions.add("Low-sodium foods (fresh fruits, unsalted nuts)");
                suggestions.add("Potassium-rich foods (bananas, spinach)");
            }

            if (c.contains("pcos")) {
                suggestions.add("High-fiber foods (beans, whole grains)");
                suggestions.add("Lean protein (eggs, fish, tofu)");
            }

            if (c.contains("obesity")) {
                suggestions.add("Low-calorie-density foods (vegetables, soups)");
                suggestions.add("High-protein foods to stay full longer");
            }
        }

        return suggestions;
    }
}
