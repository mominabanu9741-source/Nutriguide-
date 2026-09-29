package com.example.nutriguide;

import com.google.gson.annotations.SerializedName;

public class Nutriments {
    @SerializedName("energy-kcal_100g")
    public double calories;

    @SerializedName("carbohydrates_100g")
    public double carbs;

    @SerializedName("sugars_100g")
    public double sugar;

    @SerializedName("sodium_100g")
    public double sodium;

    @SerializedName("fiber_100g")
    public double fiber;
}
