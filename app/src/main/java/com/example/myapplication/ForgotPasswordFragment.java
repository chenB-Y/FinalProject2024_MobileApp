package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ForgotPasswordFragment extends Fragment {

    private EditText emailEditText, usernameEditText, newPasswordEditText, confirmPasswordEditText;
    private Button validateButton, savePasswordButton;
    private String userEmail; // Store the email for resetting the password

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forgot_password, container, false);

        emailEditText = view.findViewById(R.id.emailEditText);
        usernameEditText = view.findViewById(R.id.usernameEditText);
        newPasswordEditText = view.findViewById(R.id.newPasswordEditText);
        confirmPasswordEditText = view.findViewById(R.id.confirmPasswordEditText);
        validateButton = view.findViewById(R.id.validateButton);
        savePasswordButton = view.findViewById(R.id.savePasswordButton);

        validateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    validateUser();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        savePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    saveNewPassword();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        return view;
    }

    private void validateUser() throws JSONException {
        String email = emailEditText.getText().toString();
        String username = usernameEditText.getText().toString();

        OkHttpClient client = new OkHttpClient();

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                new JSONObject()
                        .put("email", email)
                        .put("username", username)
                        .toString()
        );

        Request request = new Request.Builder()
                .url("http://193.106.55.136:5432/auth/checkBeforeReset")
                .post(body)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    requireActivity().runOnUiThread(() -> {
                        // Show password fields and save button
                        newPasswordEditText.setVisibility(View.VISIBLE);
                        confirmPasswordEditText.setVisibility(View.VISIBLE);
                        savePasswordButton.setVisibility(View.VISIBLE);

                        // Store email for password reset
                        userEmail = email;
                        Toast.makeText(getContext(), "User validated. Enter new password.", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Invalid email or username", Toast.LENGTH_SHORT).show()
                    );
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void saveNewPassword() throws JSONException {
        String newPassword = newPasswordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        if (newPassword.equals(confirmPassword)) {
            OkHttpClient client = new OkHttpClient();

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"),
                    new JSONObject()
                            .put("email", userEmail)
                            .put("newPassword", newPassword)
                            .toString()
            );

            Request request = new Request.Builder()
                    .url("http://193.106.55.136:5432/auth/resetPass")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Password updated successfully", Toast.LENGTH_SHORT).show();

                            // Navigate back to login
                            NavHostFragment.findNavController(ForgotPasswordFragment.this)
                                    .navigate(R.id.action_forgotPasswordFragment_to_loginFragment);
                        });
                    } else {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Failed to update password", Toast.LENGTH_SHORT).show()
                        );
                    }
                }

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }
            });
        } else {
            Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
        }
    }
}


