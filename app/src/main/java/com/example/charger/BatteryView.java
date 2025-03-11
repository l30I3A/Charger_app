package com.example.charger;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public class BatteryView extends View {
    private Paint backgroundPaint;
    private Paint backgroundStrokePaint;
    private Paint levelPaint;        // Краска для уровня заряда
    private Path wavePath;           // Путь для волны
    private Paint wavePaint;
    private Paint waveDarkerPaint;
    private float temperatureLevel;
    private Paint circlelevelPaint;
    private RectF batteryRect;
    private float wave1OffsetX = 0f;
    private float wave2OffsetX = 40f;
    private final float wave1Speed = 3f;
    private final float wave2Speed = 1.95f;

    public BatteryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    private void init(Context context) {
        // Настройка краски фона
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(ContextCompat.getColor(context, R.color.progress_gray));

        backgroundStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundStrokePaint.setColor(ContextCompat.getColor(context, R.color.black));
        
        // Настройка краски уровня заряда
        levelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        levelPaint.setColor(ContextCompat.getColor(context, R.color.progress_green));

        // Настройка краски волны
        wavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wavePaint.setColor(ContextCompat.getColor(context, R.color.progress_green));
        wavePaint.setStyle(Paint.Style.FILL);

        waveDarkerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        waveDarkerPaint.setColor(ContextCompat.getColor(context, R.color.progress_dark_green));
        waveDarkerPaint.setStyle(Paint.Style.FILL);

        circlelevelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        wavePath = new Path();
        batteryRect = new RectF();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Shader circlelevelshader = new LinearGradient(0, h, 0, (float) (4 * w) / 5,
                new int[]{
                        ContextCompat.getColor(getContext(), R.color.progress_green),
                        ContextCompat.getColor(getContext(), R.color.progress_green_light)
                },
                new float[]{0.3f,1},
                Shader.TileMode.CLAMP);
        circlelevelPaint.setShader(circlelevelshader);
    }

    public void setTemperatureLevel(float level) {
        temperatureLevel = Math.max(0, Math.min(level, 100)); // Ограничиваем диапазон 0-100
        invalidate(); // Перерисовываем View
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();
        float borderCornerRadius = 60f;
        float cornerRadius = 50f;
        float borderThickness = 10f;
        float waveHeight = 30f;
        float waveAmplitude = 10f; // Высота волны
        float waveFrequency = 0.05f; // Частота волны

        RectF borderRect = new RectF(0, 0, width, height);
        canvas.drawRoundRect(borderRect, borderCornerRadius, borderCornerRadius, backgroundStrokePaint);

        // Настройка прямоугольника батареи
        batteryRect.set(borderThickness, borderThickness, width-borderThickness, height-borderThickness);

        Path clipPath = new Path();
        clipPath.addRoundRect(batteryRect, cornerRadius, cornerRadius, Path.Direction.CW);
        canvas.clipPath(clipPath);

        // Рисуем фон батареи с скругленными углами
        canvas.drawRoundRect(batteryRect, cornerRadius, cornerRadius, backgroundPaint);
        float levelHeight = (height - borderThickness*2) * ((temperatureLevel - 3) / 100);
        canvas.drawRoundRect(borderThickness, height - borderThickness - levelHeight, width-borderThickness, height-borderThickness, cornerRadius, cornerRadius, circlelevelPaint);

        if (temperatureLevel != 0){
            wavePath.reset();
            wavePath.moveTo(borderThickness, height - borderThickness - 15 - levelHeight - waveHeight);
            for (int x = (int) borderThickness; x <= getWidth() - borderThickness; x += 1) {
                float y = (float) (height - borderThickness - levelHeight - 15 - waveHeight + (waveAmplitude-4) * Math.sin((x + wave2OffsetX) * waveFrequency));
                wavePath.lineTo(x, y);
            }
            wavePath.lineTo(width-borderThickness, height - levelHeight);
            wavePath.lineTo(borderThickness, height- levelHeight);
            wavePath.lineTo(borderThickness, height - borderThickness - levelHeight - waveHeight);
            wavePath.close();
            canvas.drawPath(wavePath, waveDarkerPaint);

            wavePath.reset();
            wavePath.moveTo(borderThickness, height - borderThickness - levelHeight - waveHeight);
            for (int x = (int) borderThickness; x <= getWidth() - borderThickness; x += 1) {
                float y = (float) (height - borderThickness - levelHeight - waveHeight + waveAmplitude * Math.sin((x + wave1OffsetX) * waveFrequency));
                wavePath.lineTo(x, y);
            }
            wavePath.lineTo(width-borderThickness, height + cornerRadius - levelHeight);
            wavePath.lineTo(borderThickness, height + cornerRadius - levelHeight);
            wavePath.lineTo(borderThickness, height - borderThickness - levelHeight - waveHeight);
            wavePath.close();
            canvas.drawPath(wavePath, circlelevelPaint);
        }
    }
    public void startWaveAnimation() {
        post(new Runnable() {
            @Override
            public void run() {
                wave1OffsetX += wave1Speed;
                wave2OffsetX += wave2Speed;
                invalidate();
                postDelayed(this, 16);
            }
        });
    }

}
