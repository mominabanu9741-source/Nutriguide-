package com.example.nutriguide;

import java.util.List;

public class Food {
    public String name;
    public double calories;
    public double carbs;
    public double sugar;
    public double sodium;
    public double fiber;
    public List<String> allergens;

    public Food(String name, double calories, double carbs, double sugar,
                double sodium, double fiber, List<String> allergens) {
        this.name = name;
        this.calories = calories;
        this.carbs = carbs;
        this.sugar = sugar;
        this.sodium = sodium;
        this.fiber = fiber;
        this.allergens = allergens;
    }
}
