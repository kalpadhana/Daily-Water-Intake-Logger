package com.example.water_logger;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

public class WaterLevelView extends View {

    private Paint circlePaint, ringPaint, waterPaint, textPaint, subTextPaint;
    private Path wavePath = new Path();

    private int targetMl = 2000;
    private int currentMl = 0;

    private float animatedPercent = 0f;   // 0..1 (animated)
    private float waveShift = 0f;         // horizontal animation

    private ValueAnimator levelAnimator;
    private ValueAnimator waveAnimator;

    public WaterLevelView(Context context) { super(context); init(); }
    public WaterLevelView(Context context, AttributeSet attrs) { super(context, attrs); init(); }
    public WaterLevelView(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(); }

    private void init() {
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(Color.WHITE);

        ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(dp(6));
        ringPaint.setColor(Color.parseColor("#BBD8FF"));

        waterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        waterPaint.setColor(Color.parseColor("#2E6BFF"));
        waterPaint.setStyle(Paint.Style.FILL);
        waterPaint.setAlpha(200);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.parseColor("#0B1B3F"));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textPaint.setTextSize(sp(42));

        subTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        subTextPaint.setColor(Color.parseColor("#3B4C7A"));
        subTextPaint.setTextAlign(Paint.Align.CENTER);
        subTextPaint.setTextSize(sp(14));

        startWaveAnimation();
    }

    // Public API
    public void setTargetMl(int targetMl) {
        this.targetMl = Math.max(1, targetMl);
        setCurrentMl(currentMl); // refresh percent
    }

    public void setCurrentMl(int ml) {
        int newMl = clamp(ml, 0, targetMl);
        this.currentMl = newMl;

        float newPercent = (float) currentMl / (float) targetMl;
        animateToPercent(newPercent);
    }

    public int getCurrentMl() { return currentMl; }
    public int getTargetMl() { return targetMl; }

    private void animateToPercent(float newPercent) {
        if (levelAnimator != null) levelAnimator.cancel();

        levelAnimator = ValueAnimator.ofFloat(animatedPercent, newPercent);
        levelAnimator.setDuration(700);
        levelAnimator.setInterpolator(new DecelerateInterpolator());
        levelAnimator.addUpdateListener(a -> {
            animatedPercent = (float) a.getAnimatedValue();
            invalidate();
        });
        levelAnimator.start();
    }

    private void startWaveAnimation() {
        waveAnimator = ValueAnimator.ofFloat(0f, 1f);
        waveAnimator.setDuration(1400);
        waveAnimator.setRepeatCount(ValueAnimator.INFINITE);
        waveAnimator.setRepeatMode(ValueAnimator.RESTART);
        waveAnimator.addUpdateListener(a -> {
            waveShift = (float) a.getAnimatedValue();
            invalidate();
        });
        waveAnimator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (waveAnimator != null) waveAnimator.cancel();
        if (levelAnimator != null) levelAnimator.cancel();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        float cx = w / 2f;
        float cy = h / 2f;
        float radius = Math.min(w, h) / 2f - dp(8);

        // Circle base
        canvas.drawCircle(cx, cy, radius, circlePaint);
        canvas.drawCircle(cx, cy, radius, ringPaint);

        // Clip to circle for water
        Path clip = new Path();
        clip.addCircle(cx, cy, radius - dp(3), Path.Direction.CW);

        int save = canvas.save();
        canvas.clipPath(clip);

        // Water level (0 bottom -> 1 top)
        float levelY = cy + radius - (2f * radius * animatedPercent);

        // Wave parameters
        float waveAmp = dp(7); // wave height
        float waveLen = radius * 1.2f; // wave length
        float phase = waveShift * waveLen;

        wavePath.reset();
        wavePath.moveTo(cx - radius - dp(20), levelY);

        float startX = cx - radius - dp(20);
        float endX = cx + radius + dp(20);

        for (float x = startX; x <= endX; x += dp(4)) {
            float y = (float) (levelY + waveAmp * Math.sin((2 * Math.PI / waveLen) * (x + phase)));
            wavePath.lineTo(x, y);
        }

        // Close path to bottom
        wavePath.lineTo(endX, cy + radius + dp(20));
        wavePath.lineTo(startX, cy + radius + dp(20));
        wavePath.close();

        canvas.drawPath(wavePath, waterPaint);

        canvas.restoreToCount(save);

        // Center text
        canvas.drawText(String.valueOf(currentMl), cx, cy - dp(6), textPaint);
        canvas.drawText("ml", cx, cy + dp(22), subTextPaint);

        int percent = Math.round(animatedPercent * 100f);
        canvas.drawText(percent + "%", cx, cy + dp(48), subTextPaint);
    }

    private int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }

    private float sp(float v) {
        return v * getResources().getDisplayMetrics().scaledDensity;
    }
}