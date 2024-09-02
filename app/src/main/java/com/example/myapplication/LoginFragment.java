package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.fragment.NavHostFragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneMultiFactorAssertion;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class LoginFragment extends Fragment {

    private EditText emailEditText, passwordEditText;
    private Button loginButton, registerButton;

    private Button forgotPasswordButton;
    //private FirebaseAuth mAuth;
    private RequestQueue requestQueue;


    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestQueue = Volley.newRequestQueue(requireContext());
        checkLoginStatus();


        double x = 0, y = 0;
        double screenInches = 0;


        try {
            WindowManager windowManager = requireActivity().getWindowManager();
            Display display = windowManager.getDefaultDisplay();
            DisplayMetrics dm = new DisplayMetrics();
            display.getMetrics(dm);

            // Get the DPI values
            float xDpi = dm.xdpi;
            float yDpi = dm.ydpi;

            // Get the pixel dimensions
            Point realSize = new Point();
            Display.class.getMethod("getRealSize", Point.class).invoke(display, realSize);
            int mWidthPixels = realSize.x;
            int mHeightPixels = realSize.y;
            Log.d("DPI", mWidthPixels + " " + mHeightPixels);
            // Calculate the width and height in inches
            double widthInches = mWidthPixels / xDpi;
            double heightInches = mHeightPixels / yDpi;
            Log.d("DPI", widthInches + " " + heightInches + " " + xDpi + " " + yDpi);
            // Calculate the diagonal size in inches
            screenInches = Math.sqrt(Math.pow(widthInches, 2) + Math.pow(heightInches, 2));
            Log.d("DPI", "Screen size in inches: " + screenInches);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        // Inflate the loading view
        forgotPasswordButton = view.findViewById(R.id.forgotPasswordButton);
        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        loginButton = view.findViewById(R.id.loginButton);
        registerButton = view.findViewById(R.id.registerButton);
        loginButton.setOnClickListener(v ->{
            loginUser();
        });

        registerButton.setOnClickListener(v ->{
            navigateToRegister();
        });
        forgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(LoginFragment.this)
                        .navigate(R.id.action_loginFragment_to_forgotPasswordFragment);
            }
        });

        return view;
    }

    private void checkLoginStatus() {
        String url = "http://193.106.55.136:5432/auth/checkLogin"; // Replace with your server's URL
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "");

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("username", username);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    try {
                        boolean isLoggedIn = response.getBoolean("isLoggedIn");
                        if (isLoggedIn) {
                            String usernametosend = response.getString("username");

                            // Navigate to HomeFragment
                            Bundle bundle = new Bundle();
                            bundle.putString("username", usernametosend); // Pass username to HomeFragment
                            NavHostFragment.findNavController(LoginFragment.this)
                                    .navigate(R.id.action_loginFragment_to_homeFragment, bundle);
                        } else {
                            // User is not logged in, stay on the login screen

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Error parsing JSON response", Toast.LENGTH_SHORT).show();

                    }
                },
                error -> {
                    // Handle error response
                    Toast.makeText(requireContext(), "Not Logged In" , Toast.LENGTH_SHORT).show();

                });

        requestQueue.add(jsonObjectRequest);
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        String url = "http://193.106.55.136:5432/auth/login"; // Replace with your server URL
        //extract the size of the screen

        DisplayMetrics displayMetrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        float xdpi = displayMetrics.xdpi;

        double screenInches = 0 ;
        Log.d("DPI", screenInches+ " " + xdpi + " " + width + " " + height);

        screenInches = getScreenSize();
        screenInches = truncateToTwoDecimalPlaces(screenInches);

        Log.d("DPI", screenInches+ " ");
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", email);
            jsonBody.put("password", password);

            JSONObject phoneSize = new JSONObject();
            phoneSize.put("inches", screenInches);
            phoneSize.put("width", width);
            phoneSize.put("height", height);


            jsonBody.put("phoneSize", phoneSize);
            //jsonBody.put("deviceName", getDeviceName());
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        if (success) {
                            String username = response.getString("username");
                            String city = response.getString("city");

                            // Save email and username to SharedPreferences
                            SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("email", email);
                            editor.putString("username", username);
                            editor.putString("city", city);
                            editor.apply();

                            Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show();

                            // Navigate to HomeFragment
                            Bundle bundle = new Bundle();
                            bundle.putString("username", username); // Pass username to HomeFragment
                            NavHostFragment.findNavController(LoginFragment.this)
                                    .navigate(R.id.action_loginFragment_to_homeFragment, bundle);
                        } else {
                            Toast.makeText(requireContext(), "Login failed. Invalid credentials.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Error parsing JSON response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // Handle error response
                    Toast.makeText(requireContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });

        requestQueue.add(jsonObjectRequest);
    }
    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.DISPLAY;
        Log.d("DPI", manufacturer + " " + model);
        return manufacturer + " " + model;
    }
    public static double truncateToTwoDecimalPlaces(double value) {
        // Truncate the number to 2 decimal places
        return Math.round(value * 100) / 100.00;
    }

   // public static double calculateScreenInches(double widthPixels, double heightPixels, float ppi) {
        // Calculate the diagonal in inches
    //    return Math.sqrt(Math.pow(widthPixels, 2) + Math.pow(heightPixels, 2));
   // }

    private void navigateToRegister() {
        NavHostFragment.findNavController(this).navigate(R.id.action_loginFragment_to_registerFragment);
    }


    private Double getScreenSize(){
        Point point = new Point();
        ((WindowManager)requireContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealSize(point);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int width = point.x;
        int height = point.y;
        double wi = (double)width/(double) displayMetrics.xdpi;
        double hi = (double)height/(double) displayMetrics.ydpi;
        double x = Math.pow(wi, 2);
        double y = Math.pow(hi, 2);
        return Math.sqrt(x+y);
    }


}



