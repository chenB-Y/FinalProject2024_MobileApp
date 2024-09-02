package com.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class JokeBoxView extends View {
    private String joke = "";

    public JokeBoxView(Context context) {
        super(context);
    }

    public JokeBoxView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public JokeBoxView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setJoke(String joke) {
        this.joke = joke;
        invalidate(); // Request a redraw to display the joke
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Create a Paint object for drawing the text
        Paint paint = new Paint();
        paint.setColor(Color.BLACK); // Set text color
        paint.setTextSize(14 * getResources().getDisplayMetrics().density); // Set text size
        paint.setTextAlign(Paint.Align.LEFT); // Left-align the text

        // Get the width of the View
        int viewWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        int xPos = getPaddingLeft(); // Start drawing from the left padding

        // Break the joke into lines that fit within the view width
        List<String> lines = new ArrayList<>();
        String[] words = joke.split(" ");
        StringBuilder lineBuilder = new StringBuilder();

        for (String word : words) {
            if (paint.measureText(lineBuilder.toString() + " " + word) <= viewWidth) {
                lineBuilder.append(word).append(" ");
            } else {
                lines.add(lineBuilder.toString());
                lineBuilder = new StringBuilder(word).append(" ");
            }
        }
        lines.add(lineBuilder.toString()); // Add the last line

        // Calculate the vertical position to start drawing the text block
        int yPos = (int) ((getHeight() / 2) - ((lines.size() * (paint.descent() - paint.ascent())) / 2));

        // Draw each line of text
        for (String line : lines) {
            canvas.drawText(line.trim(), xPos, yPos, paint);
            yPos += paint.descent() - paint.ascent(); // Move to the next line
        }
    }

}
