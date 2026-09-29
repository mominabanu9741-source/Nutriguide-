package com.example.nutriguide;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiService {
    @GET("api/v2/product/{barcode}.json")
    Call<ProductResponse> getProductByBarcode(@Path("barcode") String barcode);

    @GET("cgi/search.pl?search_simple=1&action=process&json=1")
    Call<SearchResponse> searchFood(@retrofit2.http.Query("search_terms") String query);
}
