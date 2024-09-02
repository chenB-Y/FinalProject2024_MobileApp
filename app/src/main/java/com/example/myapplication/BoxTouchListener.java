package com.example.myapplication;

import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class BoxTouchListener implements View.OnTouchListener {

    private float dX, dY; // For single touch movement
    private float initialTouchX, initialTouchY; // For multi-touch resizing
    private int lastAction;
    private int initialWidth, initialHeight;
    private View touchedView;
    private Rect containerRect; // Rect representing the boundaries of the container

    private ViewGroup editableArea;

    public BoxTouchListener(ViewGroup editableArea) {
        this.editableArea = editableArea;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                dX = view.getX() - event.getRawX();
                dY = view.getY() - event.getRawY();
                initialTouchX = event.getX();
                initialTouchY = event.getY();
                lastAction = MotionEvent.ACTION_DOWN;
                initialWidth = view.getWidth();
                initialHeight = view.getHeight();
                touchedView = view;
                if (editableArea != null) {
                    containerRect = new Rect(editableArea.getPaddingLeft(),
                            editableArea.getPaddingTop(),
                            editableArea.getWidth() - view.getWidth() - editableArea.getPaddingRight(),
                            editableArea.getHeight() - view.getHeight() - editableArea.getPaddingBottom());
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 1 && touchedView != null) {
                    // Single touch: Move the view
                    float newX = event.getRawX() + dX;
                    float newY = event.getRawY() + dY;

                    // Adjust position to stay within container bounds
                    newX = Math.max(containerRect.left, Math.min(newX, containerRect.right));
                    newY = Math.max(containerRect.top, Math.min(newY, containerRect.bottom));

                    touchedView.setX(newX);
                    touchedView.setY(newY);

                    // Check for collision with other views
                    checkForCollisions(touchedView);

                    lastAction = MotionEvent.ACTION_MOVE;
                } else if (event.getPointerCount() == 2 && touchedView != null) {
                    // Multi-touch: Resize the view
                    float dx = event.getX(1) - event.getX(0);
                    float dy = event.getY(1) - event.getY(0);

                    float absDx = Math.abs(dx);
                    float absDy = Math.abs(dy);

                    ViewGroup.LayoutParams params = touchedView.getLayoutParams();

                    if (absDx > absDy) {
                        // Horizontal pinch
                        float scaleX = absDx / initialWidth;
                        params.width = (int) (initialWidth * scaleX);
                    } else if (absDy > absDx) {
                        // Vertical pinch
                        float scaleY = absDy / initialHeight;
                        params.height = (int) (initialHeight * scaleY);
                    } else {
                        // Diagonal pinch
                        float distance = (float) Math.sqrt(dx * dx + dy * dy);
                        float scale = distance / Math.min(initialWidth, initialHeight);
                        params.width = (int) (initialWidth * scale);
                        params.height = (int) (initialHeight * scale);
                    }

                    // Constrain size to the container bounds
                    params.width = Math.max(params.width, 100); // Min width
                    params.height = Math.max(params.height, 100); // Min height
                    params.width = Math.min(params.width, containerRect.width());
                    params.height = Math.min(params.height, containerRect.height());

                    touchedView.setLayoutParams(params);

                    lastAction = MotionEvent.ACTION_MOVE;
                }
                break;

            case MotionEvent.ACTION_UP:
                if (lastAction == MotionEvent.ACTION_DOWN) {
                    // Consider it a click event
                }
                touchedView = null;
                break;

            default:
                return false;
        }
        return true;
    }

    // Helper method to check for collisions with other views
    private void checkForCollisions(View view) {
        for (int i = 0; i < editableArea.getChildCount(); i++) {
            View otherView = editableArea.getChildAt(i);
            if (otherView != view && viewIsIntersecting(view, otherView)) {
                // Handle collision: Prevent view from moving towards the overlapping view
                float viewCenterX = view.getX() + view.getWidth() / 2;
                float viewCenterY = view.getY() + view.getHeight() / 2;

                float otherViewCenterX = otherView.getX() + otherView.getWidth() / 2;
                float otherViewCenterY = otherView.getY() + otherView.getHeight() / 2;

                if (Math.abs(viewCenterX - otherViewCenterX) < Math.abs(viewCenterY - otherViewCenterY)) {
                    // Vertical collision: Prevent vertical movement
                    if (viewCenterY < otherViewCenterY) {
                        view.setY(otherView.getY() - view.getHeight());
                    } else {
                        view.setY(otherView.getY() + otherView.getHeight());
                    }
                } else {
                    // Horizontal collision: Prevent horizontal movement
                    if (viewCenterX < otherViewCenterX) {
                        view.setX(otherView.getX() - view.getWidth());
                    } else {
                        view.setX(otherView.getX() + otherView.getWidth());
                    }
                }
            }
        }
    }

    // Helper method to check if two views are intersecting
    private boolean viewIsIntersecting(View view1, View view2) {
        Rect rect1 = new Rect((int) view1.getX(), (int) view1.getY(),
                (int) (view1.getX() + view1.getWidth()), (int) (view1.getY() + view1.getHeight()));
        Rect rect2 = new Rect((int) view2.getX(), (int) view2.getY(),
                (int) (view2.getX() + view2.getWidth()), (int) (view2.getY() + view2.getHeight()));
        return rect1.intersect(rect2);
    }
}
