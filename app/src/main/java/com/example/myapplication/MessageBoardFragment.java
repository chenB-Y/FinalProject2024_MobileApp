package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.android.material.textfield.TextInputEditText;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

import okhttp3.Call;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MessageBoardFragment extends Fragment {

    private EditText[] messageEditTexts = new EditText[4];
    private Button addButton;

    private Button[] colorButtons = new Button[4];
    private int[] messageColors = new int[4];
    private TextView[] suggestionTexts = new TextView[4];
    private Button setSuggestionButton1;
    private Button setSuggestionButton2;
    private Button setSuggestionButton3;
    private Button setSuggestionButton4;

    private AIService aiService;

    public MessageBoardFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message_board, container, false);

        suggestionTexts[0] = view.findViewById(R.id.suggestionText1);
        suggestionTexts[1] = view.findViewById(R.id.suggestionText2);
        suggestionTexts[2] = view.findViewById(R.id.suggestionText3);
        suggestionTexts[3] = view.findViewById(R.id.suggestionText4);

        setSuggestionButton1 = view.findViewById(R.id.setSuggestionButton1);
        setSuggestionButton2 = view.findViewById(R.id.setSuggestionButton2);
        setSuggestionButton3 = view.findViewById(R.id.setSuggestionButton3);
        setSuggestionButton4 = view.findViewById(R.id.setSuggestionButton4);

        setButtons();

        // Initialize Color Buttons
        colorButtons[0] = view.findViewById(R.id.colorButton1);
        colorButtons[1] = view.findViewById(R.id.colorButton2);
        colorButtons[2] = view.findViewById(R.id.colorButton3);
        colorButtons[3] = view.findViewById(R.id.colorButton4);

        for (int i = 0; i < colorButtons.length; i++) {
            final int index = i;
            colorButtons[i].setOnClickListener(v -> showColorPicker(index));
        }
        // Initialize EditText fields
        messageEditTexts[0] = view.findViewById(R.id.messageEditText1);
        messageEditTexts[1] = view.findViewById(R.id.messageEditText2);
        messageEditTexts[2] = view.findViewById(R.id.messageEditText3);
        messageEditTexts[3] = view.findViewById(R.id.messageEditText4);
        Log.d("TAG", "the size is "+messageEditTexts.length);
        for (int i = 0; i < messageEditTexts.length; i++) {
            final int index = i;
            messageEditTexts[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // Optional: Implement if needed
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Optional: Implement if needed
                }

                @Override
                public void afterTextChanged(Editable s) {
                    // Ensure that the index is within bounds
                    if (index >= 0 && index < suggestionTexts.length) {
                        if (!s.toString().isEmpty()) {
                            Log.d("MessageBoardFragment", "Fetching suggestion for index " + index);
                            // Verify that suggestionTexts[index] is not null
                            if (suggestionTexts[index] != null) {
                                getSuggestion(s.toString(), suggestionTexts[index]);
                            } else {
                                Log.e("MessageBoardFragment", "SuggestionTextView is null for index " + index);
                            }
                        }
                    } else {
                        Log.e("MessageBoardFragment", "Index out of bounds: " + index);
                    }
                }
            });
        }


        addButton = view.findViewById(R.id.addButton);

        addButton.setOnClickListener(v -> {
            List<Message> messages = new ArrayList<>();
            for (int i = 0; i < messageEditTexts.length; i++) {
                String messageText = messageEditTexts[i].getText().toString().trim();
                if (!messageText.isEmpty()) {
                    if(!(messageColors[i]==Color.BLACK)) {
                        int color = messageColors[i];
                        messages.add(new Message(messageText, String.format("#%06X", (0xFFFFFF & color))));
                    }
                }
            }
            if (!messages.isEmpty()) {
                addMessagesToServer(messages);
            } else {
                Toast.makeText(requireActivity(), "At least one message is required", Toast.LENGTH_SHORT).show();
            }
        });



        fetchUserMessages();

        return view;
    }

    private void setButtons() {
        setSuggestionButton1.setOnClickListener(v -> {messageEditTexts[0].setText(suggestionTexts[0].getText()); });
        setSuggestionButton2.setOnClickListener(v -> {messageEditTexts[1].setText(suggestionTexts[1].getText()); });
        setSuggestionButton3.setOnClickListener(v -> {messageEditTexts[2].setText(suggestionTexts[2].getText()); });
        setSuggestionButton4.setOnClickListener(v -> {messageEditTexts[3].setText(suggestionTexts[3].getText()); });
    }

    public void getSuggestion(final String input, final TextView suggestionText) {
        String prompt = "Write a very short text suggestion (for Digital Bulletin Board) for this input: " + input;
        GenerativeModel gm = new GenerativeModel(/* modelName */ "gemini-1.5-flash",
                /* apiKey */ "AIzaSyCNGwHb1j4ekh47ib6J4BNRUJ7HDBmx5ig"); // Replace with your actual API key
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        Content content = new Content.Builder()
                .addText(prompt) // Use the provided query as the prompt
                .build();

        Executor executor = MoreExecutors.newDirectExecutorService(); // Use a simple executor for this example

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String resultText = result.getText();
                Log.d("MessageBoardFragment", "Suggestion for input \"" + input + "\" : " + resultText);
                // Ensure updating the UI on the main thread
                if (suggestionText != null) {
                    suggestionText.setText(resultText);
                } else {
                    Log.e("MessageBoardFragment", "SuggestionTextView is null");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("MessageBoardFragment", "Error getting suggestion", t);
            }
        }, executor);
    }




    private void showColorPicker(int messageIndex) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Choose a Color");

        // Predefined colors
        final String[] colors = {
                "Red", "Green", "Blue", "Yellow", "Cyan", "Magenta", "Orange", "Purple", "Brown", "Gray"
        };
        final int[] colorValues = {
                Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN, Color.MAGENTA,
                Color.parseColor("#FFA500"), Color.parseColor("#800080"), Color.parseColor("#A52A2A"), Color.GRAY
        };

        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                messageColors[messageIndex] = colorValues[which];
                //colorButtons[messageIndex].setBackgroundColor(messageColors[messageIndex]);
                messageEditTexts[messageIndex].setTextColor(messageColors[messageIndex]);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void addMessagesToServer(List<Message> messages) {
        // Ensure the number of messages does not exceed 4
        if (messages.size() > 4) {
            messages = messages.subList(0, 4);
        }

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String email = sharedPreferences.getString("email", "");

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", email);
            JSONArray jsonMessages = new JSONArray();
            for (Message message : messages) {
                JSONObject jsonMessage = new JSONObject();
                jsonMessage.put("text", message.getText());
                jsonMessage.put("color", message.getColor()); // Include color information
                jsonMessages.put(jsonMessage);
            }
            jsonBody.put("messages", jsonMessages);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error preparing request", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonBody.toString());
        Request request = new Request.Builder()
                .url("http://193.106.55.136:5432/user/addMessages") // Replace with your server's URL
                .post(requestBody)
                .build();

        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(() -> {
                    String errorMessage = "Failed to add messages: " + e.getMessage();
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    Log.e("MessageBoardFragment", errorMessage, e);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                getActivity().runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        String username = sharedPreferences.getString("username", "");
                        notifyTVAppOfUpdate(username);
                        String responseBody;
                        try {
                            responseBody = response.body().string();
                            Log.d("MessageBoardFragment", "Response: " + responseBody);
                            Toast.makeText(getContext(), "Messages added successfully", Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        String errorMessage = "Failed to add messages: " + response.message();
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                        Log.e("MessageBoardFragment", errorMessage);
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

    private void fetchUserMessages() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String email = sharedPreferences.getString("email", "");

        Request request = new Request.Builder()
                .url("http://193.106.55.136:5432/user/getUserMessages?email=" + email) // Replace with your server's URL
                .get()
                .build();

        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(() -> {
                    String errorMessage = "Failed to fetch messages: " + e.getMessage();
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    Log.e("MessageBoardFragment", errorMessage, e);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        JSONArray jsonMessages = jsonResponse.getJSONArray("messages");
                        getActivity().runOnUiThread(() -> {
                            for (int i = 0; i < jsonMessages.length(); i++) {
                                if (i < messageEditTexts.length) {
                                    try {
                                        JSONObject jsonMessage = jsonMessages.getJSONObject(i);
                                        String messageText = jsonMessage.getString("text");
                                        String color = jsonMessage.getString("color");

                                        // Set message text
                                        messageEditTexts[i].setText(messageText);
                                        if(!messageText.isEmpty())
                                            getSuggestion(messageText, suggestionTexts[i]);
                                        // Set text color
                                        int parsedColor = Color.parseColor(color);
                                        messageEditTexts[i].setTextColor(parsedColor);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    String errorMessage = "Failed to fetch messages: " + response.message();
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                        Log.e("MessageBoardFragment", errorMessage);
                    });
                }
            }
        });
    }



}