package com.example.water_logger;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

public class WaterLevelView extends View {

    private Paint circlePaint, ringPaint, waterPaint, textPaint, subTextPaint, percentBgPaint, percentTextPaint;
    private Path wavePath = new Path();

    private int targetMl = 2000;
    private int currentMl = 0;

    private float animatedPercent = 0f;
    private float waveShift = 0f;

    private ValueAnimator levelAnimator;
    private ValueAnimator waveAnimator;

    public WaterLevelView(Context context) { super(context); init(); }
    public WaterLevelView(Context context, AttributeSet attrs) { super(context, attrs); init(); }
    public WaterLevelView(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(); }

    private void init() {
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        // Main glassmorphism circle (Custom light blue interior)
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(Color.parseColor("#533BF5"));
        circlePaint.setAlpha(60); // Semi-transparent for glass effect

        // Light blue border with subtle glow effect
        ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(dp(12));
        ringPaint.setColor(Color.parseColor("#A8D4FF"));
        ringPaint.setAlpha(120);
        ringPaint.setShadowLayer(dp(12), 0, 0, Color.parseColor("#80A8D4FF"));

        // Water fill (modern blue)
        waterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        waterPaint.setColor(Color.parseColor("#3B82F6"));
        waterPaint.setStyle(Paint.Style.FILL);
        waterPaint.setAlpha(160);

        // Main text (ml value) - Large bold white
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textPaint.setTextSize(sp(68));
        textPaint.setShadowLayer(dp(4), 0, dp(2), Color.parseColor("#20000000"));

        // Sub text (ml label) - Small light white
        subTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        subTextPaint.setColor(Color.parseColor("#CCFFFFFF"));
        subTextPaint.setTextAlign(Paint.Align.CENTER);
        subTextPaint.setTextSize(sp(20));
        subTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        // Percent badge background (pill shape glassmorphism)
        percentBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        percentBgPaint.setColor(Color.argb(50, 255, 255, 255));
        percentBgPaint.setStyle(Paint.Style.FILL);

        // Percent text - Bold white
        percentTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        percentTextPaint.setColor(Color.WHITE);
        percentTextPaint.setTextAlign(Paint.Align.CENTER);
        percentTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        percentTextPaint.setTextSize(sp(16));

        startWaveAnimation();
    }

    public void setTargetMl(int targetMl) {
        this.targetMl = Math.max(1, targetMl);
        setCurrentMl(currentMl);
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

        // Draw main circle with glassmorphism and soft glowing border
        canvas.drawCircle(cx, cy, radius, circlePaint);
        canvas.drawCircle(cx, cy, radius, ringPaint);

        // Clip to circle for water
        Path clip = new Path();
        clip.addCircle(cx, cy, radius - dp(1), Path.Direction.CW);

        int save = canvas.save();
        canvas.clipPath(clip);

        // Water level calculation
        float levelY = cy + radius - (2f * radius * animatedPercent);

        // Wave parameters
        float waveAmp = dp(6);
        float waveLen = radius * 1.15f;
        float phase = waveShift * waveLen;

        wavePath.reset();
        wavePath.moveTo(cx - radius - dp(20), levelY);

        float startX = cx - radius - dp(20);
        float endX = cx + radius + dp(20);

        for (float x = startX; x <= endX; x += dp(3)) {
            float y = (float) (levelY + waveAmp * Math.sin((2 * Math.PI / waveLen) * (x + phase)));
            wavePath.lineTo(x, y);
        }

        wavePath.lineTo(endX, cy + radius + dp(20));
        wavePath.lineTo(startX, cy + radius + dp(20));
        wavePath.close();

        canvas.drawPath(wavePath, waterPaint);
        canvas.restoreToCount(save);

        // Center Text elements: Large number, label, and pill badge
        // Spacing adjusted to match reference image
        canvas.drawText(String.valueOf(currentMl), cx, cy - dp(5), textPaint);
        canvas.drawText("ml", cx, cy + dp(30), subTextPaint);

        // Percent badge (rounded pill shape)
        int percent = Math.round(animatedPercent * 100f);
        String percentText = percent + "%";
        float textWidth = percentTextPaint.measureText(percentText);
        float badgeWidth = textWidth + dp(24);
        float badgeHeight = dp(36);
        float badgeX = cx - badgeWidth / 2f;
        float badgeY = cy + dp(50);

        RectF badgeRect = new RectF(badgeX, badgeY, badgeX + badgeWidth, badgeY + badgeHeight);
        canvas.drawRoundRect(badgeRect, dp(18), dp(18), percentBgPaint);
        canvas.drawText(percentText, cx, badgeY + dp(23), percentTextPaint);
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
