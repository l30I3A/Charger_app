package com.example.charger;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public class CustomSwitch extends View {
    private boolean isChecked = false;
    private Paint trackPaint, thumbPaint, borderPaint;
    private RectF trackRect;
    private float thumbX;
    private int trackWidth = 155;
    private int trackHeight = 80;
    private final int thumbSize = 45;
    private float startThumbX, endThumbX;
    private int borderWidth = 10;
    private ValueAnimator animator;
    private OnCheckedChangeListener listener;
    public interface OnCheckedChangeListener {
        void onCheckedChanged(boolean isChecked);
    }

    public CustomSwitch(Context context) {
        super(context);
        init(null);
    }

    public CustomSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public CustomSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        trackRect = new RectF();

        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(borderWidth);
        borderPaint.setColor(Color.BLACK);

        updateColors();
    }

    private void updateColors() {
        int startColor = isChecked ? getResources().getColor(R.color.progress_green)
                : getResources().getColor(R.color.progress_gray);
        int endColor = isChecked ? getResources().getColor(R.color.switch_dark_green)
                : getResources().getColor(R.color.progress_gray);

        trackPaint.setShader(new LinearGradient(
                0, (float) (trackHeight * 5) /18, 0, trackHeight,
                startColor, endColor,
                Shader.TileMode.CLAMP
        ));

        thumbPaint.setColor(ContextCompat.getColor(getContext(), android.R.color.white));
    }

    private void updateThumbPositions() {
        int margin = 20;
        startThumbX = margin;
        endThumbX = trackWidth - thumbSize - margin;
        thumbX = isChecked ? endThumbX : startThumbX;
        invalidate();
    }

    private void animateThumb() {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }

        float targetX = isChecked ? endThumbX : startThumbX;

        animator = ValueAnimator.ofFloat(thumbX, targetX);
        animator.setDuration(200);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animation -> {
            thumbX = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        trackRect.set(borderWidth / 2f, borderWidth / 2f, trackWidth - borderWidth / 2f, trackHeight - borderWidth / 2f);
        canvas.drawRoundRect(trackRect, trackHeight / 2f, trackHeight / 2f, trackPaint);

        canvas.drawRoundRect(trackRect, trackHeight / 2f, trackHeight / 2f, borderPaint);

        float thumbCenterY = trackHeight / 2f;
        float thumbCenterX = thumbX + thumbSize / 2f;
        canvas.drawCircle(thumbCenterX, thumbCenterY, thumbSize / 2f, thumbPaint);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            isChecked = !isChecked;
            updateColors();
            animateThumb();

            if (listener != null) {
                listener.onCheckedChanged(isChecked);
            }
            return true;
        }
        return true;
    }

    public void setChecked(boolean checked) {
        this.isChecked = checked;
        updateColors();
        invalidate();
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(trackWidth, trackHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        trackWidth = w;
        trackHeight = h;
        updateThumbPositions();
    }
}
