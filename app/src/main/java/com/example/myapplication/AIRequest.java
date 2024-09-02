package com.example.myapplication;

import java.util.List;

public class AIRequest {
    private String text; // Make sure this matches the API's expected field name
    private int max_tokens;
    private List<String> providers; // Optional, depending on API requirements

    public AIRequest(String text, int max_tokens, List<String> providers) {
        this.text = text;
        this.max_tokens = max_tokens;
        this.providers = providers;
    }

    // Getters and setters
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getMax_tokens() {
        return max_tokens;
    }

    public void setMax_tokens(int max_tokens) {
        this.max_tokens = max_tokens;
    }

    public List<String> getProviders() {
        return providers;
    }

    public void setProviders(List<String> providers) {
        this.providers = providers;
    }
}
