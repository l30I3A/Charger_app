package com.example.charger;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ColorCircleView extends View {
    private Paint backSelectorPaint;
    private Paint selectorPaint;
    private Paint borderPaint;
    private boolean isEnabled = false;
    private float selectorX, selectorY;
    private float selectorMargin = 33;
    private float boarderWidth = 25;
    private float selectorBoarder = 5;
    private OnColorSelectedListener listener;
    private Bitmap colorWheelBitmap;

    public ColorCircleView(Context context) {
        super(context);
        init();
    }

    public ColorCircleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ColorCircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        selectorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectorPaint.setColor(getColorAtPoint(selectorX, selectorY));

        backSelectorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backSelectorPaint.setColor(Color.BLACK);

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(Color.BLACK);

        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        generateColorWheel();
        selectorX = getWidth() / 2f;
        selectorY = getHeight() / 2f;
    }

    private void generateColorWheel() {
        float width = (getWidth() - boarderWidth);
        float height = (getHeight() - boarderWidth);
        float radius = Math.min(width, height) / 2;
        float centerX = width / 2f -1;
        float centerY = height/ 2f - 1;

        colorWheelBitmap = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float dx = x - centerX;
                float dy = y - centerY;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);
                if (distance <= radius) {
                    float hue = (float) Math.toDegrees(Math.atan2(dy, dx));
                    if (hue <= 0) hue += 360;
                    float saturation = (float) Math.min(1, (distance / radius)-0.03);
                    float brightnessFactor = 1 - (distance / radius) * 0.00f;
                    int color = Color.HSVToColor(new float[]{hue, saturation, Math.max(brightnessFactor, 0.7f)});
                    colorWheelBitmap.setPixel(x, y, color);
                }
            }
        }
    }

    @Override
    public void setEnabled(boolean enabled){
        isEnabled = enabled;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        float radius = ((float) Math.min(width, height) / 2);
        canvas.drawCircle(width / 2f, height / 2f, radius, borderPaint);
        if (colorWheelBitmap != null) {
            canvas.drawBitmap(colorWheelBitmap, boarderWidth/2, boarderWidth/2, null);
        }
        canvas.drawCircle(selectorX, selectorY, 15+selectorBoarder, backSelectorPaint);
        canvas.drawCircle(selectorX, selectorY, 15, selectorPaint);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isEnabled){
            getParent().requestDisallowInterceptTouchEvent(true);
            float x = event.getX();
            float y = event.getY();
            int width = getWidth();
            int height = getHeight();
            int radius = Math.min(width, height) / 2 - (int) selectorMargin;

            float centerX = width / 2f;
            float centerY = height / 2f;
            float dx = x - centerX;
            float dy = y - centerY;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if (distance <= radius) {
                selectorX = x;
                selectorY = y;
            } else {
                float angle = (float) Math.atan2(dy, dx);
                selectorX = centerX + (float) (radius * Math.cos(angle));
                selectorY = centerY + (float) (radius * Math.sin(angle));
            }

            invalidate();
            int selectedColor = getColorAtPoint(selectorX, selectorY);
            if (listener != null) {
                listener.onColorSelected(selectedColor);
            }
            selectorPaint.setColor(getColorAtPoint(selectorX, selectorY));
            return true;
        }
        else {
            return false;
        }
    }

    public void setSelectorPosition(float x, float y) {
        selectorX = x;
        selectorY = y;
        selectorPaint.setColor(getColorAtPoint(selectorX, selectorY));
        invalidate();
        if (listener != null) {
            listener.onColorSelected(getColorAtPoint(selectorX, selectorY));
        }
    }

    public float get_my_X() {
        return selectorX;
    }

    public float get_my_Y() {
        return selectorY;
    }

    private int getColorAtPoint(float x, float y) {
        if (colorWheelBitmap == null) return Color.WHITE;
        return colorWheelBitmap.getPixel((int) x, (int) y);
    }

    public int getColorAtPoint() {
        if (colorWheelBitmap == null) return Color.WHITE;
        return colorWheelBitmap.getPixel((int) selectorX, (int) selectorY);
    }

    public interface OnColorSelectedListener {
        void onColorSelected(int color);
    }

    public void setOnColorSelectedListener(OnColorSelectedListener listener) {
        this.listener = listener;
    }
}