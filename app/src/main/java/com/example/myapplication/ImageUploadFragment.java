package com.example.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ImageUploadFragment extends Fragment {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;
    private ImageView imagePreview;
    private Uri imageUri;



    private Bitmap imageBitmap;

    private Button buttonSaveImage;

    public ImageUploadFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_upload, container, false);

        Button buttonSelectImage = view.findViewById(R.id.button_select_image);
        imagePreview = view.findViewById(R.id.image_preview);
        buttonSaveImage = view.findViewById(R.id.button_save_image);



        buttonSelectImage.setOnClickListener(v -> showImageSelectionDialog());
        buttonSaveImage.setOnClickListener(v -> uploadImage(this.imageBitmap));
        loadImageFromFirestore();

        return view;
    }
    private void loadImageFromFirestore() {

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

    private void showImageSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select Image");

        String[] options = {"Take Photo", "Choose from Gallery"};

        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    // Take Photo option
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    break;
                case 1:
                    // Choose from Gallery option
                    Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_GALLERY);
                    break;
            }
        });

        builder.create().show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                imagePreview.setImageBitmap(imageBitmap);
                this.imageBitmap = imageBitmap;
                //uploadImage(imageBitmap);
            } else if (requestCode == REQUEST_IMAGE_GALLERY && data != null) {
                imageUri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
                    imagePreview.setImageBitmap(bitmap);
                    this.imageBitmap = bitmap;
                   // uploadImage(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void uploadImage(Bitmap bitmap) {
        // Retrieve email from SharedPreferences
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String email = sharedPreferences.getString("email", "");

        // Convert bitmap to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        // Create OkHttpClient instance
        OkHttpClient client = new OkHttpClient();

        // Build request body
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", "image.jpg", RequestBody.create(MediaType.parse("image/jpeg"), data))
                .addFormDataPart("email", email)  // Add email to the request
                .build();

        // Build request
        Request request = new Request.Builder()
                .url("http://193.106.55.136:5432/user/edit")
                .post(requestBody)
                .build();

        // Execute request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                getActivity().runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                        String username = sharedPreferences.getString("username", "");
                        notifyTVAppOfUpdate(username);
                        Toast.makeText(getContext(), "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                        // Optionally handle response from server
                    } else {
                        Toast.makeText(getContext(), "Failed to upload image: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }


}

