package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ContainerSuggestionFragment extends Fragment {

    private EditText inputText;
    private Button suggestButton;
    private TextView suggestionText;
    private AIService aiService;

    private static final String TAG = "ContainerSuggestion";
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiM2MzYmQxMDAtMjllZC00MmUwLThmZDUtNDI3OTRiM2RiMDQ0IiwidHlwZSI6ImZyb250X2FwaV90b2tlbiJ9.W6qk0R9b0FA20h6Rl4eJ0ZJStuuMVi1bUbE11NpIQZs"; // Replace with your actual API key

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_container_suggestion, container, false);

        inputText = view.findViewById(R.id.inputText);
        suggestButton = view.findViewById(R.id.suggestButton);
        suggestionText = view.findViewById(R.id.suggestionText);

        suggestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSuggestion(inputText.getText().toString() , suggestionText);
            }
        });


        return view;
    }


    public void getSuggestion(String input ,TextView suggestionText) {
        String prompt = "Write a detailed suggestion for a container size for an item described as: " + input;
        List<String> providers = Collections.singletonList("google"); // Use your actual provider
        AIRequest request = new AIRequest(prompt, 100, providers); // Example max tokens

        Log.d(TAG, "Request: " + new Gson().toJson(request)); // Log the request body

        aiService.getSuggestion(request).enqueue(new Callback<AIResponse>() {
            @Override
            public void onResponse(Call<AIResponse> call, Response<AIResponse> response) {
                Log.d(TAG, "Response Code: " + response.code()); // Log response code
                Log.d(TAG, "Response Body: " + response.body()); // Log response body

                if (response.isSuccessful()) {
                    AIResponse aiResponse = response.body();
                    if (aiResponse != null && aiResponse.getChoices() != null && !aiResponse.getChoices().isEmpty()) {
                        String suggestion = aiResponse.getChoices().get(0).getText();
                        suggestionText.setText(suggestion); // Display the generated text
                    } else {
                        Log.e(TAG, "Empty choices list in response");
                        suggestionText.setText("No suggestions available"); // Show no suggestions message
                    }
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e(TAG, "Failed to get suggestion: " + response.code() + " - " + response.message() + " - Error body: " + errorBody);
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    suggestionText.setText("Failed to get suggestion"); // Show failed message
                }
            }

            @Override
            public void onFailure(Call<AIResponse> call, Throwable t) {
                Log.e(TAG, "Error: " + t.getMessage(), t);
                suggestionText.setText("Error: " + t.getMessage()); // Show error message
            }
        });
    }










}
