package com.example.nutriguide;

import com.google.gson.annotations.SerializedName;

public class Product {
    @SerializedName("product_name")
    public String productName;

    @SerializedName("nutriments")
    public Nutriments nutriments;

    @SerializedName("allergens_tags")
    public java.util.List<String> allergensTags;
}
