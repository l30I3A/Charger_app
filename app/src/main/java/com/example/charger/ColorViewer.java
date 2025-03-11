package com.example.charger;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public class ColorViewer extends View {
    private Paint backgroundPaint;
    private Paint backgroundStrokePaint;

    public ColorViewer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    private void init(Context context) {
        // Настройка краски фона
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(Color.WHITE);

        backgroundStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundStrokePaint.setColor(ContextCompat.getColor(context, R.color.black));
    }

    public void SetViewingColor (int color){
        backgroundPaint.setColor(color);
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();
        float borderCornerRadius = 62f;
        float cornerRadius = 50f;
        float borderThickness = 12f;

        @SuppressLint("DrawAllocation") var borderRect = new RectF(0, 0, width, height);
        canvas.drawRoundRect(borderRect, borderCornerRadius, borderCornerRadius, backgroundStrokePaint);

        @SuppressLint("DrawAllocation") RectF colorRect = new RectF(borderThickness, borderThickness, width-borderThickness, height-borderThickness);
        canvas.drawRoundRect(colorRect, cornerRadius, cornerRadius, backgroundPaint);
    }

}
