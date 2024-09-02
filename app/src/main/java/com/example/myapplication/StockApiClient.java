package com.example.myapplication;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;

public class StockApiClient {

    private static final String BASE_URL = "https://www.alphavantage.co/query";
    private static final String API_KEY = "VIL290LGPIMDBEW7"; // Your API key

    // Method to fetch stock data for a given symbol
    public String getStockData(String symbol) throws IOException {
        OkHttpClient client = new OkHttpClient();
        HttpUrl url = HttpUrl.parse(BASE_URL)
                .newBuilder()
                .addQueryParameter("function", "TIME_SERIES_DAILY")
                .addQueryParameter("symbol", symbol)
                .addQueryParameter("apikey", API_KEY)
                .build();

        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            return response.body().string();
        }
    }

    // Method to parse the JSON data
    public JsonObject parseStockData(String jsonData) {
        JsonParser parser = new JsonParser();
        return parser.parse(jsonData).getAsJsonObject();
    }
}
