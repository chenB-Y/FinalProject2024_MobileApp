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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
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

import org.json.JSONObject;

public class RegisterFragment extends Fragment {

    private static final int STORAGE_PERMISSION_CODE = 101;
    private EditText emailEditText, passwordEditText, nameEditText, buildingAddressEditText, devicesCountEditText, floorsCountEditText,cityEditText;
    private ImageView profileImageView;
    private CheckBox termsCheckBox;
    private Button registerButton;
    private Uri imageUri;
    private OkHttpClient client;

    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        nameEditText = view.findViewById(R.id.nameEditText);
        buildingAddressEditText = view.findViewById(R.id.buildingAddressEditText);
        cityEditText = view.findViewById(R.id.cityEditText);
        devicesCountEditText = view.findViewById(R.id.devicesCountEditText);
        floorsCountEditText = view.findViewById(R.id.floorsCountEditText);
        profileImageView = view.findViewById(R.id.profileImageView);
        termsCheckBox = view.findViewById(R.id.termsCheckBox);
        registerButton = view.findViewById(R.id.registerButton);

        // Initially disable the register button
        registerButton.setEnabled(false);

        if (!checkStoragePermission()) {
            requestStoragePermission();
        }

        profileImageView.setOnClickListener(v -> selectImage());
        registerButton.setOnClickListener(v -> registerUser());

        // Enable the register button only if the checkbox is checked
        termsCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            registerButton.setEnabled(isChecked);
        });

        client = new OkHttpClient();

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

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
        }
    }

    private void registerUser() {
        String username = nameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String name = nameEditText.getText().toString();
        String buildingAddress = buildingAddressEditText.getText().toString();
        String city = cityEditText.getText().toString();
        String devicesCount = devicesCountEditText.getText().toString();
        String floorsCount = floorsCountEditText.getText().toString();

        if (!termsCheckBox.isChecked()) {
            Toast.makeText(getContext(), "You must agree with the terms and conditions", Toast.LENGTH_SHORT).show();
            return;
        }

        uploadImageAndRegisterUser(username,email, password, name, buildingAddress,city, devicesCount, floorsCount);
    }

    private void uploadImageAndRegisterUser(String username, String email, String password, String name, String buildingAddress,String city, String devicesCount, String floorsCount) {
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
                        .addFormDataPart("email", email)
                        .addFormDataPart("password", password)
                        .addFormDataPart("name", name)
                        .addFormDataPart("buildingAddress", buildingAddress)
                        .addFormDataPart("city", city)
                        .addFormDataPart("devicesCount", devicesCount)
                        .addFormDataPart("floorsCount", floorsCount)
                        .build();

                // Create HTTP request
                Request request = new Request.Builder()
                        .url("http://193.106.55.136:5432/auth/register") // Replace with your server's URL
                        .post(requestBody)
                        .build();

                // Execute HTTP request asynchronously
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        getActivity().runOnUiThread(() -> {
                            Log.d("RegisterFragment", "Response: " + e.getMessage());
                            Toast.makeText(getContext(), "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            getActivity().runOnUiThread(() -> {
                                // Pass username to HomeFragment using bundle

                                SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("email", email);
                                editor.putString("username", username);
                                editor.putString("city", city);
                                editor.apply();
                                Bundle bundle = new Bundle();
                                bundle.putString("username", username);
                                NavHostFragment.findNavController(RegisterFragment.this)
                                        .navigate(R.id.action_registerFragment_to_homeFragment, bundle);

                                Toast.makeText(getContext(), "User registered successfully", Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            getActivity().runOnUiThread(() -> {
                                Log.d("RegisterFragment", "Response: " + response);
                                Toast.makeText(getContext(), "Registration failed: " + response.message(), Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                });
            } catch (Exception e) {
                Toast.makeText(getContext(), "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Please select an image", Toast.LENGTH_SHORT).show();
        }
    }

}


