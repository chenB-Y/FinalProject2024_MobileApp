package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import android.Manifest;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

public class EditProfileFragment extends Fragment {

    private static final int STORAGE_PERMISSION_CODE = 101;
    private static final int IMAGE_PICK_CODE = 1000;

    private EditText passwordEditText,emailEditText, nameEditText, buildingAddressEditText, devicesCountEditText, floorsCountEditText, cityEditText;
    private ImageView profileImageView;
    private Button updateButton;
    private Uri imageUri;
    private OkHttpClient client;

    public EditProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        nameEditText = view.findViewById(R.id.nameEditText);
        buildingAddressEditText = view.findViewById(R.id.buildingAddressEditText);
        cityEditText = view.findViewById(R.id.cityEditText);
        devicesCountEditText = view.findViewById(R.id.devicesCountEditText);
        floorsCountEditText = view.findViewById(R.id.floorsCountEditText);
        profileImageView = view.findViewById(R.id.profileImageView);
        updateButton = view.findViewById(R.id.updateButton);

        client = new OkHttpClient();

        // Load user data from SharedPreferences
        loadUserData();

        profileImageView.setOnClickListener(v -> selectImage());
        updateButton.setOnClickListener(v -> updateProfile());

        // Request storage permission if needed
        if (!checkStoragePermission()) {
            requestStoragePermission();
        }

        return view;
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) &&
                        !ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // User has denied permissions and selected "Don't ask again"
                    Toast.makeText(getContext(), "You need to enable permissions from settings", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                }
            }
        }
    }

    private void loadUserData() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String email = sharedPreferences.getString("email", "");
        emailEditText.setText(sharedPreferences.getString("email", ""));
        nameEditText.setText(sharedPreferences.getString("username", ""));
        cityEditText.setText(sharedPreferences.getString("city", ""));
        // Load other data as needed

        // Set up OkHttp client
        OkHttpClient client = new OkHttpClient();

        // Create request to the server
        Request request = new Request.Builder()
                .url("http://193.106.55.136:5432/auth/getProfileData?email=" + email)
                .build();

        // Execute the request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Handle the error
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();

                    try {
                        // Parse the JSON response
                        JSONObject json = new JSONObject(responseData);

                        String buildingAddress = json.getString("buildingAddress");
                        int deviceCount = json.getInt("devicesCount");
                        int floorsCount = json.getInt("floorsCount");
                        String profileImageUrl = json.getString("profileImage");
                        String profileImageUrlFinal = profileImageUrl.replace("public/images", "http://193.106.55.136:5432");
                        // Update UI on the main thread
                        requireActivity().runOnUiThread(() -> {

                            // Update other UI elements with the data from the server
                            buildingAddressEditText.setText(buildingAddress);
                            devicesCountEditText.setText(String.valueOf(deviceCount));
                            floorsCountEditText.setText(String.valueOf(floorsCount));
                            Log.d("EditProfileFragment", "Profile image URL: " + profileImageUrlFinal);
                            Picasso.get().load(profileImageUrlFinal).into(profileImageView);
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
        }
    }

    private void updateProfile() {
        String username = nameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String buildingAddress = buildingAddressEditText.getText().toString();
        String city = cityEditText.getText().toString();
        String devicesCount = devicesCountEditText.getText().toString();
        String floorsCount = floorsCountEditText.getText().toString();

        uploadImageAndUpdateUserProfile(username, email,password, buildingAddress, city, devicesCount, floorsCount);
    }

    private void uploadImageAndUpdateUserProfile(String username, String email,String password, String buildingAddress, String city, String devicesCount, String floorsCount) {
        if (imageUri != null) {
            try {
                // Convert imageUri to File
                File imageFile = new File(getContext().getCacheDir(), "profileImage.jpg");

                // Copy file from imageUri to File
                try (InputStream inputStream = getContext().getContentResolver().openInputStream(imageUri);
                     FileOutputStream outputStream = new FileOutputStream(imageFile)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }

                // Build multipart request body with image file and other parameters
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("profileImage", imageFile.getName(), RequestBody.create(MediaType.parse("image/*"), imageFile))
                        .addFormDataPart("username", username)
                        .addFormDataPart("password", password)
                        .addFormDataPart("email", email)
                        .addFormDataPart("buildingAddress", buildingAddress)
                        .addFormDataPart("city", city)
                        .addFormDataPart("devicesCount", devicesCount)
                        .addFormDataPart("floorsCount", floorsCount)
                        .build();

                // Create HTTP request
                Request request = new Request.Builder()
                        .url("http://193.106.55.136:5432/auth/updateProfile") // Replace with your server's URL
                        .post(requestBody)
                        .build();

                // Execute HTTP request asynchronously
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        getActivity().runOnUiThread(() -> {
                            Log.d("EditProfileFragment", "Response: " + e.getMessage());
                            Toast.makeText(getContext(), "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            getActivity().runOnUiThread(() -> {
                                // Update shared preferences with new data
                                SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("email", email);
                                editor.putString("username", username);
                                editor.putString("city", city);
                                editor.apply();

                                // Navigate to home or other fragment
                                Bundle bundle = new Bundle();
                                bundle.putString("username", username);

                                // Navigate to home or other fragment with the bundle
                                NavHostFragment.findNavController(EditProfileFragment.this)
                                        .navigate(R.id.action_editProfileFragment_to_homeFragment, bundle);

                                Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            getActivity().runOnUiThread(() -> {
                                Log.d("EditProfileFragment", "Response: " + response);
                                Toast.makeText(getContext(), "Update failed: " + response.message(), Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                });
            } catch (Exception e) {
                Toast.makeText(getContext(), "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

}
