package com.example.myapplication;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface AIService {
    @Headers("Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiM2MzYmQxMDAtMjllZC00MmUwLThmZDUtNDI3OTRiM2RiMDQ0IiwidHlwZSI6ImZyb250X2FwaV90b2tlbiJ9.W6qk0R9b0FA20h6Rl4eJ0ZJStuuMVi1bUbE11NpIQZs")
    @POST("text/ai_detection")
    Call<AIResponse> getSuggestion(@Body AIRequest request);
}
