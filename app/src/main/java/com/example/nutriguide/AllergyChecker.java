package com.example.nutriguide;

import java.util.ArrayList;
import java.util.List;

public class AllergyChecker {

    // Checks a food's ingredient list against the user's stored allergies.
    // Returns the list of matched allergens (empty list = safe).
    public static List<String> checkAllergies(List<String> foodIngredients, List<String> userAllergies) {
        List<String> matchedAllergens = new ArrayList<>();

        if (foodIngredients == null || userAllergies == null) {
            return matchedAllergens; // nothing to check against
        }

        for (String allergy : userAllergies) {
            for (String ingredient : foodIngredients) {
                if (ingredient.toLowerCase().contains(allergy.toLowerCase())) {
                    matchedAllergens.add(allergy);
                    break; // no need to check other ingredients for this allergy
                }
            }
        }

        return matchedAllergens;
    }
}
