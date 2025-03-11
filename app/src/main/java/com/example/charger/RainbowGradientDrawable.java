package com.example.charger;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RainbowGradientDrawable extends Drawable {
    private final Paint background = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rectF = new RectF(); // Для скругления углов
    private float cornerRadius = 16f; // Радиус скругления углов
    private int borderWidth = 4; // Ширина границы
    private int borderColor = Color.BLACK; // Цвет границы

    public RainbowGradientDrawable() {
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(borderWidth);
        borderPaint.setColor(borderColor);
    }

    @Override
    protected void onBoundsChange(@NonNull Rect bounds) {
        super.onBoundsChange(bounds);
        float width = bounds.width();
        background.setShader(new LinearGradient(
                0, 0, width, 0,
                new int[]{
                        Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE, Color.MAGENTA
                },
                new float[]{0f, 0.25f, 0.5f, 0.75f, 1f}, // Позиции цветов в процентах
                Shader.TileMode.CLAMP
        ));

        // Обновляем RectF для скругления углов
        rectF.set(bounds);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        // Рисуем прямоугольник с закругленными углами (фон)
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, background);

        // Рисуем границу с закругленными углами
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, borderPaint);
    }

    @Override
    public void setAlpha(int alpha) {
        background.setAlpha(alpha);
        borderPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        background.setColorFilter(colorFilter);
        borderPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    // Методы для настройки скругления и границы (опционально)
    public void setCornerRadius(float cornerRadius) {
        this.cornerRadius = cornerRadius;
        invalidateSelf(); // Перерисовываем Drawable
    }

    public void setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
        borderPaint.setStrokeWidth(borderWidth);
        invalidateSelf(); // Перерисовываем Drawable
    }

    public void setBorderColor(int borderColor) {
        this.borderColor = borderColor;
        borderPaint.setColor(borderColor);
        invalidateSelf(); // Перерисовываем Drawable
    }
}