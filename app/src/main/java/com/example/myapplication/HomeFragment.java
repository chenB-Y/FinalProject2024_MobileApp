package com.example.myapplication;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.android.material.navigation.NavigationView;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.Executor;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class HomeFragment extends Fragment {

    private DrawerLayout drawerLayout;
    private ImageButton menuButton;
    private TextView helloTextView;
    private Button editButton;

    private OkHttpClient client;

    private CheckBox selectTemplate1, selectTemplate2, selectTemplate3;
    private Button saveTemplateButton;

    public HomeFragment() {
        // Required empty public constructor

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        drawerLayout = view.findViewById(R.id.drawer_layout);
        NavigationView navigationView = view.findViewById(R.id.navigationView);
        helloTextView = view.findViewById(R.id.helloTextView);
        editButton = view.findViewById(R.id.editButton);
        menuButton = view.findViewById(R.id.menuButton);

        selectTemplate1 = view.findViewById(R.id.selectTemplate1);
        selectTemplate2 = view.findViewById(R.id.selectTemplate2);
        selectTemplate3 = view.findViewById(R.id.selectTemplate3);
        saveTemplateButton = view.findViewById(R.id.saveTemplateButton);
        CompoundButton.OnCheckedChangeListener listener = (buttonView, isChecked) -> {
            if (isChecked) {
                if (buttonView != selectTemplate1) selectTemplate1.setChecked(false);
                if (buttonView != selectTemplate2) selectTemplate2.setChecked(false);
                if (buttonView != selectTemplate3) selectTemplate3.setChecked(false);
            }
        };
        selectTemplate1.setOnCheckedChangeListener(listener);
        selectTemplate2.setOnCheckedChangeListener(listener);
        selectTemplate3.setOnCheckedChangeListener(listener);
        // Retrieve username passed from RegisterFragment
        Bundle args = getArguments();
        if (args != null && args.containsKey("username")) {
            String username = args.getString("username");
            helloTextView.setText("Hello, " + username + "!");
        }
        saveTemplateButton.setOnClickListener(v -> saveTemplate());
        client = new OkHttpClient();
        editButton.setOnClickListener(v ->
                NavHostFragment.findNavController(HomeFragment.this).navigate(R.id.action_homeFragment_to_editFragment));

        navigationView.setNavigationItemSelectedListener(item -> {
            String title = item.getTitle().toString();
            switch (title) {
                case "Home":

                    break;
                case "Logout":
                    logoutUser();
                    break;
                case "MessageBoard":
                    NavHostFragment.findNavController(HomeFragment.this).navigate(R.id.messageBoardFragment);
                    break;
                case "ImageBoard":
                    NavHostFragment.findNavController(HomeFragment.this).navigate(R.id.action_homeFragment_to_imageUploadFragment);
                    break;
                case "Api Choice":
                    NavHostFragment.findNavController(HomeFragment.this).navigate(R.id.action_homeFragment_to_apiFragment);
                    break;
                case "Edit Profile":
                    NavHostFragment.findNavController(HomeFragment.this).navigate(R.id.action_homeFragment_to_editProfileFragment);
                    break;
                case "Logout From TV":
                    logoutUserFromTV();
                    break;



            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        logScreenSizeInInches();
        return view;
    }

    private void logoutUserFromTV() {
        // Get email from SharedPreferences
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String email = sharedPreferences.getString("email", "");

        // Create a JSON object to send in the request body
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email", email);
        } catch (JSONException e) {
            e.printStackTrace();
            return; // Exit if there is an error creating the JSON object
        }

        // Create OkHttpClient instance
        OkHttpClient client = new OkHttpClient();

        // Create a RequestBody with the JSON object
        RequestBody requestBody = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json; charset=utf-8"));

        // Build the request
        Request request = new Request.Builder()
                .url("http://193.106.55.136:5432/user/logoutFromTv")
                .post(requestBody)
                .build();

        // Send the request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                Log.e("LogoutUserFromTV", "Request failed", e);
                // Handle failure here
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {

                    Log.d("LogoutUserFromTV", "Request succeeded");
                } else {

                    Log.e("LogoutUserFromTV", "Request failed with response code: " + response.code());
                }
            }
        });
    }

    public void logScreenSizeInInches() {
        String deviceName = Build.MODEL;
        String manufacturer = Build.MANUFACTURER;
        String deviceInfo = manufacturer + " " + deviceName;

        Log.d("DeviceInfo", "Device Name: " + deviceInfo);

        getScreenSizeUsingGemini(deviceInfo);

    }
    public void getScreenSizeUsingGemini(String deviceName) {
        // Initialize the Gemini AI API
        GenerativeModel gm = new GenerativeModel(
                "gemini-1.5-flash",
                "AIzaSyCNGwHb1j4ekh47ib6J4BNRUJ7HDBmx5ig" // Replace with your actual API key
        );

        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        // Create a prompt to get the screen size for the device
        String prompt = "What is the screen size of " + deviceName + "?";

        Content content = new Content.Builder()
                .addText(prompt)
                .build();

        Executor executor = MoreExecutors.newDirectExecutorService();

        // Send the request to the Gemini API
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        // Handle the response
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String resultText = result.getText();
                Log.d("GeminiFragment", "Screen Size Response: " + resultText);
                // Parse the resultText to extract screen size information if necessary
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("GeminiFragment", "Error getting screen size from Gemini API", t);
            }
        }, executor);
    }

    private void saveTemplate() {
        String templateId = null;
        if (selectTemplate1.isChecked()) {
            templateId = "template1";
        } else if (selectTemplate2.isChecked()) {
            templateId = "template2";
        } else if (selectTemplate3.isChecked()) {
            templateId = "template3";
        }

        String urlString = "http://193.106.55.136:5432/user/saveTemplate"; // Replace with your actual endpoint
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "");

        // Create a JSONObject for the selected template
        JSONObject jsonInput = new JSONObject();
        try {
            jsonInput.put("username", username);
            jsonInput.put("templateId", templateId);
        } catch (JSONException e) {
            Toast.makeText(getContext(), "Failed to prepare data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getContext(), "Failed to save box positions: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
                e.printStackTrace(); // Log the error for further investigation
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                getActivity().runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Box positions saved", Toast.LENGTH_SHORT).show();
                        String userId = sharedPreferences.getString("username", "");
                        notifyTVAppOfUpdate(userId);

                    } else {
                        Toast.makeText(getContext(), "Failed to save box positions: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
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


    private void logoutUser() {
        // Retrieve username from SharedPreferences or wherever it's stored
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "");

        // Build JSON body with username
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("username", username);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error preparing logout request", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create HTTP request
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonBody.toString());
        Request request = new Request.Builder()
                .url("http://193.106.55.136:5432/auth/logout") // Replace with your server's URL
                .post(requestBody)
                .build();

        // Execute HTTP request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(() -> {
                    Log.d("Logout", "Failed to logout: " + e.getMessage());
                    Toast.makeText(getContext(), "Failed to logout: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                getActivity().runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        // Clear SharedPreferences or any session information
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.remove("username");
                        editor.remove("email");
                        editor.apply();

                        // Navigate to loginFragment
                        Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
                        NavHostFragment.findNavController(HomeFragment.this).navigate(R.id.loginFragment);
                    } else {
                        Log.d("Logout", "Failed to logout: " + response.message());
                        Toast.makeText(getContext(), "Failed to logout: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

}

