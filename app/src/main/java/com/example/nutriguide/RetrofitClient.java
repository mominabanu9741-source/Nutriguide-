package com.example.nutriguide;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;

public class RetrofitClient {
    private static Retrofit retrofit;

    public static ApiService getApiService() {
        if (retrofit == null) {
            Interceptor userAgentInterceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();
                    Request withHeader = original.newBuilder()
                            .header("User-Agent", "NutriGuide-Android/1.0 (student project)")
                            .build();
                    return chain.proceed(withHeader);
                }
            };

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(userAgentInterceptor)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl("https://world.openfoodfacts.org/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}