package com.example.myapplication;


public class Message {
    private String text;
    private String color;

    public Message(String text, String color) {
        this.text = text;
        this.color = color;
    }

    public String getText() {
        return text;
    }

    public String getColor() {
        return color;
    }
}
