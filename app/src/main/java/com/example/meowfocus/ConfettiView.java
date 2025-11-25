package com.example.meowfocus;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import java.util.Random;

public class ConfettiView extends View {

    private ArrayList<Confetti> confettiList;
    private Paint paint;
    private Random random;

    public ConfettiView(Context context) {
        super(context);
        init();
    }

    public ConfettiView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        confettiList = new ArrayList<>();
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        random = new Random();
        setBackgroundColor(Color.TRANSPARENT);
    }

    public void startConfetti() {
        confettiList.clear();
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        // Create 50 confetti pieces
        for (int i = 0; i < 50; i++) {
            float angle = random.nextFloat() * 360;
            float speed = 5 + random.nextFloat() * 15;
            int color = getRandomColor();
            confettiList.add(new Confetti(centerX, centerY, angle, speed, color));
        }

        invalidate();
    }

    private int getRandomColor() {
        int[] colors = {
                Color.parseColor("#FF6B9D"), // Pink
                Color.parseColor("#FFC75F"), // Yellow
                Color.parseColor("#845EC2"), // Purple
                Color.parseColor("#00C9A7"), // Teal
                Color.parseColor("#FF9671")  // Orange
        };
        return colors[random.nextInt(colors.length)];
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        boolean needsInvalidate = false;

        for (Confetti confetti : confettiList) {
            if (confetti.update()) {
                paint.setColor(confetti.color);
                paint.setAlpha(confetti.alpha);
                canvas.drawCircle(confetti.x, confetti.y, confetti.size, paint);
                needsInvalidate = true;
            }
        }

        if (needsInvalidate) {
            postInvalidateDelayed(16); // ~60 FPS
        }
    }

    private class Confetti {
        float x, y;
        float velocityX, velocityY;
        int color;
        float size;
        int alpha = 255;
        int lifetime = 100;
        int age = 0;

        Confetti(float startX, float startY, float angle, float speed, int color) {
            this.x = startX;
            this.y = startY;
            this.color = color;
            this.size = 8 + random.nextFloat() * 12;

            double radians = Math.toRadians(angle);
            this.velocityX = (float) (Math.cos(radians) * speed);
            this.velocityY = (float) (Math.sin(radians) * speed);
        }

        boolean update() {
            if (age >= lifetime) {
                return false;
            }

            x += velocityX;
            y += velocityY;
            velocityY += 0.5f; // Gravity
            velocityX *= 0.98f; // Air resistance

            age++;
            alpha = 255 - (age * 255 / lifetime);

            return true;
        }
    }
}
