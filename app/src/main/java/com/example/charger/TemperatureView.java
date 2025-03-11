package com.example.charger;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public class TemperatureView extends View {

    private Paint backgroundPaint;
    private Paint backgroundStrokePaint;
    private Paint levelPaint;
    private Paint circlelevelPaint;
    private Paint backgroundCirclePaint;
    private float temperatureLevel = 100;
    private float borderThickness = 10f;

    public TemperatureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(ContextCompat.getColor(context, R.color.progress_gray));

        backgroundStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundStrokePaint.setColor(ContextCompat.getColor(context, R.color.black));

        backgroundCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundCirclePaint.setColor(ContextCompat.getColor(context, R.color.temperature_green));

        levelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlelevelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        Shader levelshader = new LinearGradient(0, 0, 0, h - (float) (4 * w) / 5,
                new int[]{
                        ContextCompat.getColor(getContext(), R.color.temperature_red),
                        ContextCompat.getColor(getContext(), R.color.temperature_yellow),
                        ContextCompat.getColor(getContext(), R.color.temperature_green)
                },
                new float[]{0.3f, 0.8f, 1},
                Shader.TileMode.CLAMP);
        Shader circlelevelshader = new LinearGradient(0, h, 0, (float) (4 * w) / 5,
                new int[]{
                        ContextCompat.getColor(getContext(), R.color.temperature_dark_green),
                        ContextCompat.getColor(getContext(), R.color.temperature_green)
                },
                new float[]{0f,0.6f},
                Shader.TileMode.CLAMP);
        levelPaint.setShader(levelshader);
        circlelevelPaint.setShader(circlelevelshader);
    }

    public void setTemperatureLevel(int level) {
        temperatureLevel = Math.max(30, Math.min(level, 100));
        invalidate();
    }
    public float returnBoarder(){
        return borderThickness;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();
        float borderCornerRadius = 60f;
        float cornerRadius = 50f;
        backgroundStrokePaint.setStrokeWidth(borderThickness);

        canvas.drawRoundRect(width / 3, 0, width * 2 / 3, height - width / 2, cornerRadius, borderCornerRadius, backgroundStrokePaint);
        canvas.drawCircle(width / 2, height - width * 2 / 5, width * 2 / 5, backgroundStrokePaint);
        canvas.drawRoundRect(width / 3 + borderThickness, borderThickness, width * 2 / 3 - borderThickness, height - width / 2, cornerRadius, cornerRadius, backgroundPaint);
        canvas.drawCircle(width / 2, height - width * 2 / 5, width * 2 / 5 - borderThickness, backgroundPaint);

        Path clipPath = new Path();
        clipPath.addRoundRect(width / 3 + borderThickness, borderThickness, width * 2 / 3 - borderThickness, height - width / 2, cornerRadius, cornerRadius, Path.Direction.CW);
        clipPath.addCircle(width / 2, height - width * 2 / 5, width * 2 / 5 - borderThickness, Path.Direction.CW);
        canvas.clipPath(clipPath);

        float levelHeight = (height - borderThickness * 2) * (temperatureLevel / 80);

        canvas.drawCircle(width / 2, height - width * 2 / 5, width * 2 / 5 - borderThickness, circlelevelPaint);
        canvas.drawRect(0, height - borderThickness - levelHeight, width, height - borderThickness - width * 3 / 5, levelPaint);
    }

}
