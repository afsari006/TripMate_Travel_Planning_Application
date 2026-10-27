package com.example.tripmate;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class DonutChartView extends View {

    private Paint arcPaint;
    private Paint centerCirclePaint;
    private Paint titleTextPaint;
    private Paint subtitleTextPaint;

    private RectF arcBounds = new RectF();

    private long totalAmount = 10000;
    private long transportAmount = 3000;
    private long hotelAmount = 3500;
    private long foodAmount = 2000;
    private long entertainmentAmount = 1000;
    private long localAmount = 500;

    // Segment Colors
    private static final int COLOR_TRANSPORT = 0xFFF97316;    // Vibrant Orange
    private static final int COLOR_HOTEL = 0xFF3B82F6;        // Vibrant Blue
    private static final int COLOR_FOOD = 0xFFEC4899;         // Vibrant Pink
    private static final int COLOR_ENTERTAINMENT = 0xFFEAB308;// Vibrant Yellow
    private static final int COLOR_LOCAL = 0xFF22C55E;        // Vibrant Green

    public DonutChartView(Context context) {
        super(context);
        init();
    }

    public DonutChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DonutChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arcPaint.setStyle(Paint.Style.FILL);

        centerCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerCirclePaint.setStyle(Paint.Style.FILL);
        centerCirclePaint.setColor(0xFFFFFFFF); // White center hole

        titleTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titleTextPaint.setColor(0xFF0F172A);
        titleTextPaint.setTextAlign(Paint.Align.CENTER);
        titleTextPaint.setFakeBoldText(true);

        subtitleTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        subtitleTextPaint.setColor(0xFF64748B);
        subtitleTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setBudgetData(long total, long transport, long hotel, long food, long entertainment, long local) {
        this.totalAmount = Math.max(total, 1);
        this.transportAmount = Math.max(transport, 0);
        this.hotelAmount = Math.max(hotel, 0);
        this.foodAmount = Math.max(food, 0);
        this.entertainmentAmount = Math.max(entertainment, 0);
        this.localAmount = Math.max(local, 0);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        if (width <= 0 || height <= 0) return;

        int size = Math.min(width, height);
        float centerX = width / 2f;
        float centerY = height / 2f;
        float radius = size / 2f - 10;

        arcBounds.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);

        // Calculate sweep angles
        float angleTransport = (transportAmount * 360f) / totalAmount;
        float angleHotel = (hotelAmount * 360f) / totalAmount;
        float angleFood = (foodAmount * 360f) / totalAmount;
        float angleEntertainment = (entertainmentAmount * 360f) / totalAmount;
        float angleLocal = 360f - (angleTransport + angleHotel + angleFood + angleEntertainment);

        float startAngle = -90f;

        // 1. Transport Arc
        arcPaint.setColor(COLOR_TRANSPORT);
        canvas.drawArc(arcBounds, startAngle, angleTransport, true, arcPaint);
        startAngle += angleTransport;

        // 2. Hotel Arc
        arcPaint.setColor(COLOR_HOTEL);
        canvas.drawArc(arcBounds, startAngle, angleHotel, true, arcPaint);
        startAngle += angleHotel;

        // 3. Food Arc
        arcPaint.setColor(COLOR_FOOD);
        canvas.drawArc(arcBounds, startAngle, angleFood, true, arcPaint);
        startAngle += angleFood;

        // 4. Entertainment Arc
        arcPaint.setColor(COLOR_ENTERTAINMENT);
        canvas.drawArc(arcBounds, startAngle, angleEntertainment, true, arcPaint);
        startAngle += angleEntertainment;

        // 5. Local Transport Arc
        arcPaint.setColor(COLOR_LOCAL);
        canvas.drawArc(arcBounds, startAngle, Math.max(angleLocal, 0), true, arcPaint);

        // Center Hole Circle for Donut Effect
        float innerRadius = radius * 0.62f;
        canvas.drawCircle(centerX, centerY, innerRadius, centerCirclePaint);

        // Center Text Display (e.g. ৳ 8,500)
        titleTextPaint.setTextSize(size * 0.12f);
        subtitleTextPaint.setTextSize(size * 0.065f);

        canvas.drawText("৳ " + String.format("%,d", totalAmount), centerX, centerY - 5, titleTextPaint);
        canvas.drawText("Estimated Budget", centerX, centerY + (size * 0.08f), subtitleTextPaint);
    }
}