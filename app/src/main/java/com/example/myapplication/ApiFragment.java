package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ApiFragment extends Fragment {

    private CheckBox stockExchangeCheckBox;
    private CheckBox example2CheckBox;
    private CheckBox example3CheckBox;
    private CheckBox example4CheckBox;
    private Button submitButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_api, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "");

        // Initialize checkboxes
        stockExchangeCheckBox = view.findViewById(R.id.checkbox_stock_exchange);
        example2CheckBox = view.findViewById(R.id.checkbox_example2);
        example3CheckBox = view.findViewById(R.id.checkbox_example3);
        example4CheckBox = view.findViewById(R.id.checkbox_example4);

        // Initialize the submit button
        submitButton = view.findViewById(R.id.btn_submit);

        // Set click listener for the submit button
        submitButton.setOnClickListener(v -> {
            List<String> selectedApis = getSelectedApis();
            if (selectedApis.size() <= 2) {
                saveApiChoices(selectedApis, username);
            } else {
                Toast.makeText(getActivity(), "Please select exactly 2 APIs", Toast.LENGTH_SHORT).show();
            }
        });

        // Set checkboxes listener to ensure only 2 can be checked
        View.OnClickListener checkBoxListener = v -> ensureMaxTwoSelected();
        stockExchangeCheckBox.setOnClickListener(checkBoxListener);
        example2CheckBox.setOnClickListener(checkBoxListener);
        example3CheckBox.setOnClickListener(checkBoxListener);
        example4CheckBox.setOnClickListener(checkBoxListener);
    }

    private void ensureMaxTwoSelected() {
        int selectedCount = 0;
        if (stockExchangeCheckBox.isChecked()) selectedCount++;
        if (example2CheckBox.isChecked()) selectedCount++;
        if (example3CheckBox.isChecked()) selectedCount++;
        if (example4CheckBox.isChecked()) selectedCount++;

        if (selectedCount == 2) {
            if (!stockExchangeCheckBox.isChecked()) stockExchangeCheckBox.setEnabled(false);
            if (!example2CheckBox.isChecked()) example2CheckBox.setEnabled(false);
            if (!example3CheckBox.isChecked()) example3CheckBox.setEnabled(false);
            if (!example4CheckBox.isChecked()) example4CheckBox.setEnabled(false);
        } else {
            stockExchangeCheckBox.setEnabled(true);
            example2CheckBox.setEnabled(true);
            example3CheckBox.setEnabled(true);
            example4CheckBox.setEnabled(true);
        }
    }

    private List<String> getSelectedApis() {
        List<String> selectedApis = new ArrayList<>();
        if (stockExchangeCheckBox.isChecked()) {
            selectedApis.add("Stock Exchange API");
        }
        if (example2CheckBox.isChecked()) {
            selectedApis.add("Dad Jokes API");
        }
        if (example3CheckBox.isChecked()) {
            selectedApis.add("Quotes API");
        }
        if (example4CheckBox.isChecked()) {
            selectedApis.add("Crypto Price API");
        }
        return selectedApis;
    }

    public void notifyTVAppOfUpdate(String userId) {
        String urlString = "http://193.106.55.136:5432/user/notifyTVAppUpdate"; // Replace with your actual endpoint

        JSONObject jsonInput = new JSONObject();
        try {
            jsonInput.put("username", userId);
        } catch (JSONException e) {
            Toast.makeText(getContext(), "Failed to prepare notification data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonInput.toString());
        Request request = new Request.Builder()
                .url(urlString)
                .post(requestBody)
                .build();

        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Failed to notify TV app: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                getActivity().runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "TV app notified", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Failed to notify TV app: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }


    private void saveApiChoices(List<String> apiChoices, String username) {
        OkHttpClient client = new OkHttpClient();
        String url = "http://193.106.55.136:5432/user/saveApi";  // Ensure this URL is correct

        // Convert List to JSONArray
        JSONArray jsonArray = new JSONArray(apiChoices);

        // Create JSON object with username and selected APIs array
        JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("selectedApis", jsonArray);  // Ensure this matches the server's expected parameter name
            jsonParam.put("username", username);

            // Log the JSON object
            Log.d("SaveApiChoices", "JSON Request: " + jsonParam.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        // Create the RequestBody
        RequestBody body = RequestBody.create(jsonParam.toString(), MediaType.get("application/json; charset=utf-8"));

        // Create the POST request
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")  // Ensure correct header
                .build();

        // Send the request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), "Failed to save API choices: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "No response body";

                if (response.isSuccessful()) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getActivity(), "API choices saved successfully", Toast.LENGTH_SHORT).show();
                        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                        String username = sharedPreferences.getString("username", "");
                        notifyTVAppOfUpdate(username);
                    });
                } else {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getActivity(), "Error saving API choices: " + responseBody, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }




}
