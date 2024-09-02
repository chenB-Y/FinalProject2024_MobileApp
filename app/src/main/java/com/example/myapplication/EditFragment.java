package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;

import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;

import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import yuku.ambilwarna.AmbilWarnaDialog;

public class EditFragment extends Fragment {

    private ConstraintLayout editableArea;
    private Button saveButton;
    private Button randomizeButton;
    private SharedPreferences sharedPreferences;
    private int originalOrientation;

    private TextView dateTextView;
    private TextView timeTextView;

    private Handler handler;
    private Runnable updateTimeRunnable;
    private  Button colorPickerButton;

    private ConstraintLayout constraintLayout;

    private String defaultColor;
    private String email;
    private TextView stockDataTextView;
    private String username;
    private View box8;
    private View box5;
    private Boolean box8Flage=false;





    public EditFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit, container, false);



        editableArea = view.findViewById(R.id.editableArea_backround);
        saveButton = view.findViewById(R.id.saveButton);
        dateTextView = view.findViewById(R.id.dateTextView);
        timeTextView = view.findViewById(R.id.timeTextView);
        colorPickerButton= view.findViewById(R.id.backround_color_button);
        constraintLayout=view.findViewById(R.id.editableArea_backround);
        defaultColor = String.valueOf(ContextCompat.getColor(requireActivity(),R.color.colorPrimary));
        randomizeButton = view.findViewById(R.id.randomizeButton);
        stockDataTextView = view.findViewById(R.id.stockDataTextView);
        box8 = view.findViewById(R.id.box8);
        box5 = view.findViewById(R.id.box5);
        //progressSpinner = view.findViewById(R.id.progressBar);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        dateTextView.setText(currentDate);

        sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);

        originalOrientation = getActivity().getRequestedOrientation();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setupEditableArea();
        setupSaveButton();
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        email = sharedPreferences.getString("email", "");
        username= sharedPreferences.getString("username", "");
        String city= sharedPreferences.getString("city", "");
        fetchAndPopulateBoxPositions(email);
        fetchAndDisplayMessages(email);
        fetchWeather(city);
        fetchAndSetBackgroundColor(email);
        fetchAndDisplayImage();
        checkAndRunApiFunctions();
        Log.d("done","all done");

        handler = new Handler();
        updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                displayCurrentTimeForCityOrCountry();
                handler.postDelayed(this, 60000);
            }
        };
        handler.post(updateTimeRunnable);


        colorPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openColorPicker();
            }
        });
        randomizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the spinner when randomization start
                // Call a method to randomize the layout with a small delay to show the spinner
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        randomizeLayout(); // Your randomization logic
                        // Hide the spinner once randomization is done
                    }
                }, 1000); // 1 second delay to simulate work being done
            }
        });


        return view;
    }

    private void fetchCryptoPrice() {
        OkHttpClient client = new OkHttpClient();
        String url = "https://api.api-ninjas.com/v1/cryptoprice?symbol=LTCBTC";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("X-Api-Key", "yfIKTF2vJ3a2niVZTH8Jdg==eAvp2ibBR9rsxFRX") // Replace with your actual API key
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                // Update UI on failure
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), "Failed to fetch crypto price: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    try {
                        // Parse the response JSON
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        double price = jsonResponse.getDouble("price"); // Adjust according to the actual response structure
                        String cryptoString = "LTC/BTC Price: " + price;

                        if(!isBox8Used()){
                            // Display the quote in the UI
                            getActivity().runOnUiThread(() -> displayJokeInBox8(cryptoString));
                        }
                        else{
                            getActivity().runOnUiThread(() -> displayInStockDataTextView(cryptoString));
                        }
                        // Update UI with the cryptocurrency price
                    } catch (JSONException e) {
                        e.printStackTrace();
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getActivity(), "Error parsing crypto price data", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getActivity(), "Request failed with code: " + response.code(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void fetchAndDisplayQuote() {


        new Thread(() -> {

            HttpURLConnection connection = null;
            try {
                Thread.sleep(1000);
                // Set up the request
                URL url = new URL("https://api.api-ninjas.com/v1/quotes");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("accept", "application/json");
                connection.setRequestProperty("X-Api-Key", "yfIKTF2vJ3a2niVZTH8Jdg==eAvp2ibBR9rsxFRX"); // Replace with your API key

                // Check the response code
                int responseCode = connection.getResponseCode();
                InputStream responseStream;

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // If the request is successful
                    responseStream = connection.getInputStream();
                } else {
                    // If the request fails, read the error stream
                    responseStream = connection.getErrorStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream));
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    Log.e("FetchQuoteTask", "Error response: " + errorResponse.toString());
                    throw new Exception("API request failed with response code: " + responseCode);
                }

                // Parse the response
                BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream));
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }
                String responseBody = responseBuilder.toString();
                Log.d("FetchQuoteTask", "Response body: " + responseBody); // Log the response body

                JSONArray jsonArray = new JSONArray(responseBody);
                if (jsonArray.length() > 0) {
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    String quote = jsonObject.optString("quote", "No quote found");
                    if(!isBox8Used()){
                        // Display the quote in the UI
                        getActivity().runOnUiThread(() -> displayJokeInBox8(quote));
                    }
                    else{
                        getActivity().runOnUiThread(() -> displayInStockDataTextView(quote));
                    }

                } else {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "No quotes found.", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                Log.e("FetchQuoteTask", "Failed to fetch quote", e);
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Failed to fetch quote: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }



    private void displayInStockDataTextView(String string) {
        TextView stockDataTextView = getView().findViewById(R.id.stockDataTextView); // Assuming stockDataTextView is a TextView
        if (stockDataTextView != null) {
            stockDataTextView.setText(string);
        }
    }

    private boolean isBox8Used() {

        if(box8Flage){
            Log.d("FetchQuoteTask", "isBox8Used: " + true);
            return true;
        }
        Log.d("FetchQuoteTask", "isBox8Used: " + false);
        return false;

    }





    private void checkAndRunApiFunctions() {
        String url;
        try {
            url = "http://193.106.55.136:5432/user/getApi?username=" + URLEncoder.encode(username, "UTF-8");
            Log.d("API_REQUEST_URL", "Encoded URL: " + url); // Log the encoded URL
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getActivity(), "Error encoding URL: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
            return; // Exit the method if URL encoding fails
        }

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.e("API_REQUEST_FAILED", "Failed to fetch API list", e); // Log the error
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), "Failed to fetch API list: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "No response body";
                    Log.e("API_REQUEST_ERROR", "Error fetching API list: " + response.code() + " " + errorBody); // Log the error
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getActivity(), "Error fetching API list: " + response.code() + " " + errorBody, Toast.LENGTH_LONG).show();
                    });
                    return;
                }

                String responseBody = response.body() != null ? response.body().string() : "";
                Log.d("API_LIST_RESPONSE", "Response body: " + responseBody); // Log the response body

                try {
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.getBoolean("success");
                    if (success) {
                        JSONArray jsonArray = jsonObject.getJSONArray("api");
                        final boolean[] hasStockExchangeApi = {false};
                        final boolean[] hasDadJokesApi = {false};
                        final boolean[] hasQuotesApi = {false};
                        final boolean[] hasCryptoPriceApi = {false};


                        if(jsonArray.length()==1){
                            box5.setVisibility(View.GONE);
                        }
                        if(jsonArray.length()==0){
                            box5.setVisibility(View.VISIBLE);
                            box8.setVisibility(View.GONE);
                        }

                        for (int i = 0; i < jsonArray.length(); i++) {
                            String apiName = jsonArray.getString(i);
                            Log.d("API_NAME", "API name: " + apiName); // Log each API name

                            if ("Stock Exchange API".equals(apiName)) {
                                hasStockExchangeApi[0] = true;
                            }
                             if ("Dad Jokes API".equals(apiName)) {
                                hasDadJokesApi[0] = true;
                            }
                             if ("Quotes API".equals(apiName)) {
                                hasQuotesApi[0] = true;
                            }
                             if ("Crypto Price API".equals(apiName)) {
                                hasCryptoPriceApi[0] = true;
                            }

                        }

                        // Run the functions based on API names
                        getActivity().runOnUiThread(() -> {
                            if (hasStockExchangeApi[0]) {
                                fetchExchangeRate();
                                Log.d("API_RESPONSE", "Stock Exchange API called");
                            }
                            if (hasDadJokesApi[0]) {
                                fetchAndDisplayDadJoke();
                                Log.d("API_RESPONSE", "Dad Jokes API called");
                            }
                            if (hasQuotesApi[0]) {
                                fetchAndDisplayQuote();
                                Log.d("API_RESPONSE", "Quotes API called");
                            }
                            if (hasCryptoPriceApi[0]) {
                                fetchCryptoPrice();
                                Log.d("API_RESPONSE", "Crypto Price API called");
                            }
                        });
                    } else {
                        String message = jsonObject.optString("message", "Unknown error");
                        Log.e("API_LIST_ERROR", "Failed to retrieve APIs: " + message); // Log the error message
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getActivity(), "Failed to retrieve APIs: " + message, Toast.LENGTH_LONG).show();
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("API_JSON_PARSE_ERROR", "Error parsing API list", e); // Log the parsing error
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getActivity(), "Error parsing API list: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }


    private void fetchAndDisplayDadJoke() {
        new Thread(() -> {
            HttpURLConnection connection = null;
            InputStream responseStream = null;
            BufferedReader reader = null;

            try {
                // Set up the request
                URL url = new URL("https://api.api-ninjas.com/v1/dadjokes");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("accept", "application/json");
                connection.setRequestProperty("X-Api-Key", "BOGTWWvanlVNMf4uO063eA==fXBEbwJmM6G9CZHp");

                // Check the response code
                int responseCode = connection.getResponseCode();
                Log.d("FetchDadJokeTask", "Response code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // If the request is successful
                    responseStream = connection.getInputStream();
                } else {
                    // If the request fails, read the error stream
                    responseStream = connection.getErrorStream();
                    reader = new BufferedReader(new InputStreamReader(responseStream));
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    Log.e("FetchDadJokeTask", "Error response: " + errorResponse.toString());
                    throw new Exception("API request failed with response code: " + responseCode);
                }

                // Parse the response
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(responseStream);
                String joke = root.get(0).get("joke").asText();

                // Update UI based on box8 usage
                getActivity().runOnUiThread(() -> {
                    if (!isBox8Used()) {
                        displayJokeInBox8(joke);
                    } else {
                        displayInStockDataTextView(joke);

                    }
                });

            } catch (Exception e) {
                Log.e("FetchDadJokeTask", "Failed to fetch dad joke", e);
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Failed to fetch dad joke: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            } finally {
                // Close resources and disconnect
                try {
                    if (reader != null) {
                        reader.close();
                    }
                    if (responseStream != null) {
                        responseStream.close();
                    }
                } catch (IOException e) {
                    Log.e("FetchDadJokeTask", "Error closing resources", e);
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }

    private void displayJokeInBox8(final String joke) {
        // Find the custom JokeBoxView
        JokeBoxView box8 = getView().findViewById(R.id.box8);
        // Set the joke to be displayed
        box8.setJoke(joke);
        box8Flage=true;
        stockDataTextView.setTag("used");
    }

    private void displayStockDataInStockDataTextView(final String stockData) {
        // Find the TextView with ID stockDataTextView
        TextView stockDataTextView = getView().findViewById(R.id.stockDataTextView);

        // Check if the TextView is not null
        if (stockDataTextView != null) {
            // Set the stock data to be displayed
            stockDataTextView.setText(stockData);

            // Optionally, you can set a tag to mark that the TextView has been updated
            stockDataTextView.setTag("used");
        }
    }







    private Void fetchExchangeRate() {
         // Assume this method returns a boolean indicating if box8 is used

        OkHttpClient client = new OkHttpClient();
        String url = "https://v6.exchangerate-api.com/v6/8f250aa369eee15da080fdc6/latest/USD";

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("FetchExchangeRate", "Request failed", e);
                // Update UI on failure
                getActivity().runOnUiThread(() -> {
                    if (!isBox8Used()) {
                        displayJokeInBox8("Request failed");

                    } else {
                        displayInStockDataTextView("Request failed");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    Log.d("FetchExchangeRate", "Response body: " + responseBody);

                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        Log.d("FetchExchangeRate", "Parsed JSON: " + jsonResponse.toString());

                        JSONObject conversionRates = jsonResponse.getJSONObject("conversion_rates");
                        double rate = conversionRates.getDouble("ILS");

                        if (!isBox8Used()) {
                            getActivity().runOnUiThread(() -> {
                                displayJokeInBox8("1 USD = " + rate + " ILS");
                            });

                        } else {
                            getActivity().runOnUiThread(() -> {
                                displayInStockDataTextView("1 USD = " + rate + " ILS");
                            });
                        }
                    } catch (JSONException e) {
                        Log.e("FetchExchangeRate", "Error parsing JSON data", e);
                        getActivity().runOnUiThread(() -> {
                            if (!isBox8Used()) {
                                displayJokeInBox8("Error parsing data");

                            } else {
                                displayInStockDataTextView("Error parsing data");

                            }
                        });
                    }
                } else {
                    Log.e("FetchExchangeRate", "Request failed with response code: " + response.code());
                    getActivity().runOnUiThread(() -> {
                        if (!isBox8Used()) {
                            displayJokeInBox8("Request failed with response code: " + response.code());
                        } else {
                            displayInStockDataTextView("Request failed with response code: " + response.code());
                        }
                    });
                }
            }
        });

        return null;
    }

    private void randomizeLayout() {
        int heigth = editableArea.getWidth()-editableArea.getViewById(R.id.box8).getHeight();
        String prompt = "You are a layout designer tasked with generating JSON layout configurations for a digital bulletin board. Each layout consists of five boxes: \"Messages\", \"Weather\", \"Image\", \"Date & Time\", \"Commercial\", \"Api 1\" and \"Api 2\". The layout should specify the position (x, y), width, and height of each box in pixels.\n" +
                "\n" +
                "Ensure that:\n" +
                "1. **All boxes fit within the frame**: The frame has a width of " + (editableArea.getWidth()) + " and a height of " + editableArea.getHeight()  + ".\n" +
                "2. **No boxes overlap or touch each other**.\n" +
                "3. **The \"News\" box (box6) has a fixed size and position**: it has a width of `956dp`, a height of `50dp`, and is located at the position `(x: 4dp, y: 405dp)`.\n" +
                "4. Ensure that all boxes are large enough to display their contents properly with height and width.\n" +
                "5. Ensure to consider the boxes content and the size of the news box, and dont fill the editable area but make every box big enough to display its content. "+
                "6. Ensure that the boxes are orgenized in a way that they apear one box in the top left corner and one box in the top right corner" +
                "7. one box in the bottom left corner and one box in the bottom right corner. "+
                "8. there are three boxes left randomize them in a good size and places in the screen that left"+
                "9. make it look nice and make sure that the boxes are not overlaping each other."+
                "\n" +
                "The JSON output should follow this structure:\n" +
                "```json\n" +
                "{\n" +
                "  \"Messages\": { \"x\": VALUE, \"y\": VALUE, \"width\": VALUE, \"height\": VALUE },\n" +
                "  \"Weather\": { \"x\": VALUE, \"y\": VALUE, \"width\": VALUE, \"height\": VALUE },\n" +
                "  \"Image\": { \"x\": VALUE, \"y\": VALUE, \"width\": VALUE, \"height\": VALUE },\n" +
                "  \"Date & Time\": { \"x\": VALUE, \"y\": VALUE, \"width\": VALUE, \"height\": VALUE },\n" +
                "  \"Commercial\": { \"x\": VALUE, \"y\": VALUE, \"width\": VALUE, \"height\": VALUE },\n" +
                "  \"Api 1\": { \"x\": VALUE, \"y\": VALUE, \"width\": VALUE, \"height\": VALUE },\n" +
                "  \"Api 2\": { \"x\": VALUE, \"y\": VALUE, \"width\": VALUE, \"height\": VALUE }\n" +
                "}\n" +
                "```";




        GenerativeModel gm = new GenerativeModel(
                "gemini-1.5-flash",
                "AIzaSyCNGwHb1j4ekh47ib6J4BNRUJ7HDBmx5ig" // Replace with your actual API key
        );

        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        Content content = new Content.Builder()
                .addText(prompt)
                .build();

        Executor executor = MoreExecutors.newDirectExecutorService();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String resultText = result.getText();
                Log.d("GeminiFragment", "Raw Response: " + resultText);

                // Clean the response by removing the "```json" and "```" parts
                String cleanedResponse = resultText
                        .replace("```json", "")  // Remove the opening markdown-style tag
                        .replace("```", "")      // Remove the closing markdown-style tag
                        .trim();

                // Now check if the cleaned response is valid JSON
                if (cleanedResponse.startsWith("{")) {
                    try {
                        JSONObject jsonResponse = new JSONObject(cleanedResponse); // Parse the cleaned response as JSON
                        String layoutString = jsonResponse.toString(); // Convert JSONObject to String
                        Log.d("GeminiFragment", "Parsed Layout String: " + layoutString);

                        // Pass the layout string to applyLayout
                        applyLayout(layoutString);
                    } catch (JSONException e) {
                        Log.e("GeminiFragment", "JSON Parsing Error", e);
                    }
                } else {
                    Log.e("GeminiFragment", "Unexpected Response Format: " + cleanedResponse);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("GeminiFragment", "API Request Failed", t);
            }
        }, executor);
    }



    private void applyLayout(String layout) {
        try {
            // Parse the layout JSON
            JSONObject jsonLayout = new JSONObject(layout);

            // Map the layout JSON keys to the box IDs in your UI
            String[] boxNames = {"Messages", "Weather", "Image", "Date & Time", "Commercial","Api 1","Api 2"};
            String[] boxIds = { "box1", "box2", "box3", "box4", "box7", "box8" , "box5" };

            // Get the size of the frame (editableArea)
            int frameWidth = editableArea.getWidth();
            int frameHeight = editableArea.getHeight();

            // Create a list to track box positions and sizes for overlap checking
            List<Rect> boxRectangles = new ArrayList<>();

            // Loop through each box and apply the layout
            for (int i = 0; i < boxNames.length; i++) {
                JSONObject boxLayout = jsonLayout.getJSONObject(boxNames[i]);
                int x = boxLayout.getInt("x");
                int y = boxLayout.getInt("y");
                int width = boxLayout.getInt("width");
                int height = boxLayout.getInt("height");

                // Find the corresponding view by ID
                View box = editableArea.findViewById(getResources().getIdentifier(boxIds[i], "id", getContext().getPackageName()));
                if (box != null) {
                    // Ensure the box stays within the bounds of the frame
                    if (x < 0) x = 0;
                    if (y < 0) y = 0;
                    if (x + width > frameWidth) x = frameWidth - width;
                    if (y + height > frameHeight) y = frameHeight - height;

                    // Create a Rect representing the current box's position and size
                    Rect currentBoxRect = new Rect(x, y, x + width, y + height);

                    // Check for overlap and reposition if necessary
                    boolean overlaps = true;
                    int maxAttempts = 100; // Max attempts to find a non-overlapping position

                    // Attempt to find a non-overlapping position
                    while (overlaps && maxAttempts > 0) {
                        overlaps = false;
                        for (Rect rect : boxRectangles) {
                            if (isTouchingOrOverlapping(currentBoxRect, rect)) {
                                overlaps = true;
                                // Try to move the box to a nearby position to avoid overlap
                                currentBoxRect = moveBoxToFreePosition(currentBoxRect, rect, frameWidth, frameHeight, width, height);
                                break;
                            }
                        }
                        maxAttempts--;
                    }

                    // If after max attempts it still overlaps, log a warning
                    if (overlaps) {
                        Log.d("GeminiFragment","Could not find non-overlapping position for box " + boxIds[i]);
                    } else {
                        // Save the current box's rect for future overlap checks
                        boxRectangles.add(currentBoxRect);

                        // Apply layout parameters
                        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) box.getLayoutParams();
                        params.width = width;
                        params.height = height;
                        box.setLayoutParams(params);
                        box.setOnTouchListener(new BoxTouchListener(editableArea));
                        box.setX(currentBoxRect.left);
                        box.setY(currentBoxRect.top);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            // Handle JSON parsing error
        }
    }

    // Helper function to check if two rectangles are touching or overlapping
    private boolean isTouchingOrOverlapping(Rect rect1, Rect rect2) {
        // Check if the two rectangles are touching or overlapping
        return Rect.intersects(rect1, rect2) || rect1.left == rect2.right || rect1.right == rect2.left ||
                rect1.top == rect2.bottom || rect1.bottom == rect2.top;
    }

    // Helper function to move the box to a new position to avoid overlap
    private Rect moveBoxToFreePosition(Rect currentBox, Rect otherBox, int frameWidth, int frameHeight, int boxWidth, int boxHeight) {
        int moveDistance = 10;

        // Try moving the box in different directions to avoid overlap
        // Check all four directions: right, left, down, up
        if (currentBox.right + moveDistance <= frameWidth) {
            currentBox.offset(moveDistance, 0); // Move right
        } else if (currentBox.left - moveDistance >= 0) {
            currentBox.offset(-moveDistance, 0); // Move left
        } else if (currentBox.bottom + moveDistance <= frameHeight) {
            currentBox.offset(0, moveDistance); // Move down
        } else if (currentBox.top - moveDistance >= 0) {
            currentBox.offset(0, -moveDistance); // Move up
        }

        return currentBox;
    }





    private void setupEditableArea() {
        for (int i = 0; i < editableArea.getChildCount(); i++) {
            View box = editableArea.getChildAt(i);
            if(box.getId()!=R.id.box6){
                box.setOnTouchListener(new BoxTouchListener(editableArea));
            }
        }
    }

    private void fetchAndDisplayImage() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String email = sharedPreferences.getString("email", "");

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", email); // Send email, not username
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error preparing request", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonBody.toString());
        Request request = new Request.Builder()
                .url("http://193.106.55.136:5432/user/getBoardImage")
                .post(requestBody) // Use POST instead of GET
                .build();

        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Failed to fetch image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String jsonResponse = response.body().string();
                Log.d("ImageLoading", "Raw JSON Response: " + jsonResponse);

                if (response.isSuccessful()) {
                    Log.d("ImageLoading", "111111111111111111111");
                    try {
                        Log.d("ImageLoading", "2222222222222222222222222222222555555555555555");
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        Log.d("ImageLoading", "222222222222222222222222");
                        String imageUrl = jsonObject.getString("imageUrl");
                        Log.d("ImageLoading", "3333333333333333333333333333");
                        Log.d("ImageLoading", "Image Url: " + imageUrl);
                        imageUrl = imageUrl.replace("public/BoardImages/","/");
                        String fullImageUrl = "http://193.106.55.136:5432" + imageUrl;



                        Log.d("ImageLoading", "Full Image URL: " + fullImageUrl);

                            View box3 = requireView().findViewById(R.id.box3);
                            int targetWidth = box3.getWidth();
                            int targetHeight = box3.getHeight();

                            Log.d("ImageLoading", "box3 dimensions: " + targetWidth + " x " + targetHeight);

                            // Load the image into box3
                            Glide.with(requireContext())
                                    .asBitmap()
                                    .load(fullImageUrl)
                                    .override(targetWidth, targetHeight)
                                    .centerCrop()
                                .into(new CustomTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                        getActivity().runOnUiThread(() -> {
                                            box3.setBackground(new BitmapDrawable(getResources(), resource));
                                            // After loading the image, now fetch and populate box positions
                                            fetchAndPopulateBoxPositions(email);
                                        });
                                    }

                                    @Override
                                    public void onLoadCleared(@Nullable Drawable placeholder) {
                                    }
                                });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Error parsing JSON response", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    Log.e("ImageLoading", "Failed to fetch image: " + response.message());
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Failed to fetch image: " + response.message(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }





    private void fetchAndDisplayMessages(String email) {
        OkHttpClient client = new OkHttpClient();
        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://193.106.55.136:5432/user/getUserMessages").newBuilder();
        urlBuilder.addQueryParameter("email", email);
        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Failed to fetch messages: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonResponse = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        JSONArray messagesArray = jsonObject.getJSONArray("messages");

                        // Create a SpannableStringBuilder to handle text with different colors
                        SpannableStringBuilder spannableBuilder = new SpannableStringBuilder();

                        for (int i = 0; i < messagesArray.length(); i++) {
                            JSONObject messageObject = messagesArray.getJSONObject(i);
                            String message = messageObject.getString("text");
                            String color = messageObject.getString("color"); // Fetch color as int

                            // Append the message text with the color
                            SpannableString spannableString = new SpannableString(message + "\n\n");
                            int parsedColor = Color.parseColor(color);
                            spannableString.setSpan(new ForegroundColorSpan(parsedColor), 0, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            spannableBuilder.append(spannableString);
                        }

                        String messagesText = spannableBuilder.toString();

                        getActivity().runOnUiThread(() -> {
                            // Find the TextView and set the messages
                            TextView messageTextView = requireView().findViewById(R.id.messageEditText);
                            messageTextView.setText(spannableBuilder);

                            // Resize text to fit view
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                messageTextView.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                messageTextView.setAutoSizeTextTypeUniformWithConfiguration(
                                        12, // min text size in sp
                                        30, // max text size in sp
                                        1,  // granularity in sp
                                        TypedValue.COMPLEX_UNIT_SP
                                );
                            }

                            // Set max height to ensure text fits within the box
                            messageTextView.setMaxHeight(R.id.box1 - (2 * messageTextView.getPaddingTop()));

                            // Optionally use a ScrollView if text size is still too large
                            // ScrollView scrollView = requireView().findViewById(R.id.scrollView);
                            // scrollView.addView(messageTextView);

                            // Alternatively, you can limit the text size based on the view's dimensions manually
                            // get the view dimensions and adjust text size as needed
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Error parsing JSON response for messages", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Failed to fetch messages: " + response.message(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }



    private void fetchAndPopulateBoxPositions(String email) {
        // Construct the URL with the username as a query parameter
        String fetchBoxesUrl = "http://193.106.55.136:5432/user/getUserBoxes?email=" + email;
        // Create the GET request
        Request request = new Request.Builder()
                .url(fetchBoxesUrl)
                .get()
                .build();

        // Initialize the OkHttpClient
        OkHttpClient client = new OkHttpClient();

        // Enqueue the request to be executed asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Handle the failure case on the main thread
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Failed to fetch box positions: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Handle the success case
                if (response.isSuccessful()) {
                    String jsonResponse = response.body().string();
                    Log.d("FetchBoxesResponse", jsonResponse); // Log the response for debugging

                    try {
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        if (jsonObject.getBoolean("success")) {
                            JSONArray boxesArray = jsonObject.getJSONArray("boxes");
                            // Update the UI on the main thread
                            getActivity().runOnUiThread(() -> displayUserBoxes(boxesArray));
                        } else {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "Failed to fetch box positions: User not found", Toast.LENGTH_SHORT).show();
                            });

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Error parsing JSON response for box positions", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Failed to fetch box positions: " + response.message(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void fetchWeather(String city) {
        OkHttpClient client = new OkHttpClient();

        String cityParts = city;
        Log.d("Wether", city);



        String apiKey = "af0bade2d470c93f334b0068390afd98";
        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + "ראשון לציון" + "&units=metric&appid=" + apiKey;

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Failed to fetch weather information: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONObject json = new JSONObject(responseData);
                        String cityName = json.getString("name");
                        JSONObject main = json.getJSONObject("main");
                        double temperature = main.getDouble("temp");

                        getActivity().runOnUiThread(() -> {
                            // Update the TextViews within box2
                            TextView cityTextView = requireView().findViewById(R.id.cityTextView);
                            TextView weatherTextView = requireView().findViewById(R.id.weatherTextView);
                            cityTextView.setText(cityName);
                            String weatherInfo = String.format("Temperature: %.1f°C", temperature);
                            weatherTextView.setText(weatherInfo);
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Failed to parse weather information", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Failed to fetch weather information: " + response.message(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }



    private void displayUserBoxes(JSONArray boxesArray) {
        try {
            ConstraintLayout editableArea = requireView().findViewById(R.id.editableArea_backround);
            int containerWidth = editableArea.getWidth();
            int containerHeight = editableArea.getHeight();

            for (int i = 0; i < boxesArray.length(); i++) {
                JSONObject boxData = boxesArray.getJSONObject(i);
                float xPercentage = (float) boxData.getDouble("x");
                float yPercentage = (float) boxData.getDouble("y");
                float widthPercentage = (float) boxData.getDouble("width");
                float heightPercentage = (float) boxData.getDouble("height");

                // Calculate absolute position and size based on container dimensions
                int newX = Math.round((xPercentage/100)* containerWidth);
                int newY = Math.round((yPercentage/100)* containerHeight);
                int newWidth = Math.round((widthPercentage/100)* containerWidth);
                int newHeight = Math.round((heightPercentage/100)* containerHeight);

                // Update the corresponding box view in editableArea
                int finalI = i;
                getActivity().runOnUiThread(() -> {
                    View box = editableArea.getChildAt(finalI);
                    if (box != null) {
                        // Update box layout parameters
                        ViewGroup.LayoutParams layoutParams = box.getLayoutParams();
                        layoutParams.width = newWidth;
                        layoutParams.height = newHeight;
                        box.setLayoutParams(layoutParams);
                        box.setTranslationX(newX);
                        box.setTranslationY(newY);
                        box.requestLayout();  // Request layout to apply changes

                        Log.d("BoxPosition", "Box " + finalI + " updated with position (" + newX + ", " + newY + ") and size (" + newWidth + ", " + newHeight + ")");
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "Error parsing JSON response for box positions", Toast.LENGTH_SHORT).show();
            });
        }
    }

    // Method to convert dp to pixels
    private int dpToPx(float dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void setupSaveButton() {
        saveButton.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            String username = sharedPreferences.getString("username", "");
            saveColorToServer(defaultColor,username);
            saveBoxPositions(username);
        });
    }

    private void saveBoxPositions(String username) {
        getUserIdIfLoggedIn(username, userId -> {
            sendBoxPositions(userId);
        }, errorMessage -> {
            getActivity().runOnUiThread(() ->
                    Toast.makeText(getContext(), "Failed to retrieve user ID: " + errorMessage, Toast.LENGTH_SHORT).show()
            );
        });
    }

    private void sendBoxPositions(String userId) {
        String urlString = "http://193.106.55.136:5432/user/EditeUserBoxes"; // Replace with your actual endpoint

        JSONArray boxesArray = new JSONArray();
        ConstraintLayout editableArea = requireView().findViewById(R.id.editableArea_backround);
        int containerWidth = editableArea.getWidth();
        int containerHeight = editableArea.getHeight();

        for (int i = 0; i < editableArea.getChildCount(); i++) {
            //
            View box = editableArea.getChildAt(i);

            JSONObject boxData = new JSONObject();
            try {
                // Convert positions and sizes to percentages
                float xPercentage = (box.getX() / containerWidth) * 100;
                float yPercentage = (box.getY() / containerHeight) * 100;
                float widthPercentage = ((float) box.getWidth() / containerWidth) * 100;
                float heightPercentage = ((float) box.getHeight() / containerHeight) * 100;

                boxData.put("x", xPercentage);
                boxData.put("y", yPercentage);
                boxData.put("width", widthPercentage);
                boxData.put("height", heightPercentage);
            } catch (JSONException e) {
                Toast.makeText(getContext(), "Failed to prepare box data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            boxesArray.put(boxData);
        }

        JSONObject jsonInput = new JSONObject();
        try {
            jsonInput.put("username", userId); // Make sure the key matches what your server expects
            jsonInput.put("boxes", boxesArray);
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


    private void getUserIdIfLoggedIn(String username, Consumer<String> onSuccess, Consumer<String> onFailure) {
        // Replace with your authentication logic to get userId
        // For demonstration, I'm assuming direct use of username as userId
        onSuccess.accept(username);
    }

    private void displayCurrentTimeForCityOrCountry() {
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Jerusalem");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        timeFormat.setTimeZone(timeZone);
        String currentTime = timeFormat.format(new Date());
        getActivity().runOnUiThread(() -> timeTextView.setText(currentTime));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().setRequestedOrientation(originalOrientation);
        handler.removeCallbacks(updateTimeRunnable);
    }


    public void openColorPicker() {
        // Define a list of bright colors
        List<Integer> colors = Arrays.asList(
                Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW,
                Color.CYAN, Color.MAGENTA, Color.GRAY, Color.DKGRAY
        );

        // Create an instance of the custom adapter
        ColorAdapter colorAdapter = new ColorAdapter(requireContext(), colors);

        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Choose a Color");

        // Set the adapter to the dialog
        builder.setAdapter(colorAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Get the selected color
                int selectedColor = colors.get(which);
                defaultColor = String.valueOf(selectedColor);
                // Set the background color of your layout
                constraintLayout.setBackgroundColor(selectedColor);
            }
        });

        builder.setNegativeButton("Cancel", null);

        // Show the dialog
        builder.show();
    }


    private void saveColorToServer(String color, String username) {
        OkHttpClient client = new OkHttpClient();
        String url = "http://193.106.55.136:5432/user/saveMainBackground";

        // Create JSON object with the color and username data
        JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("username", username);
            jsonParam.put("color", color);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        // Prepare request body
        RequestBody requestBody = RequestBody.create(
                jsonParam.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Failed to save color: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                requireActivity().runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(requireContext(), "Color saved successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Failed to save color: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void fetchAndSetBackgroundColor(String email) {
        OkHttpClient client = new OkHttpClient();
        String url = "http://193.106.55.136:5432/user/getUserMainBackgroundColor?email=" + email;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Failed to fetch background color: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Parse the response
                    String responseBody = response.body().string();
                    JSONObject jsonResponse;
                    try {
                        jsonResponse = new JSONObject(responseBody);
                        int color = jsonResponse.getInt("backgroundColor");

                        // Update the background color on the main thread
                        requireActivity().runOnUiThread(() -> {
                            // Set the background color of your view
                            constraintLayout.setBackgroundColor(color);
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "Failed to parse background color: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
                    }
                } else {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Failed to fetch background color: " + response.message(), Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }










}
