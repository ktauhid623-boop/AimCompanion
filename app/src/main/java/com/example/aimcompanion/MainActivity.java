package com.example.aimcompanion;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.content.SharedPreferences;

public class MainActivity extends Activity {
    private static final int BG = Color.rgb(10, 10, 13);
    private static final int PANEL = Color.rgb(27, 27, 33);
    private static final int RED = Color.rgb(255, 65, 65);
    private LinearLayout root;
    private SharedPreferences prefs;


    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("aim_companion", MODE_PRIVATE);
        showHome();
    }

    private TextView text(String value, int size, int color) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setPadding(0, 8, 0, 8);
        return t;
    }

    private LinearLayout page(String heading) {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(22, 28, 22, 22);
        root.setBackgroundColor(BG);
        root.addView(text("AIM COMPANION", 13, RED));
        root.addView(text(heading, 28, Color.WHITE));
        setContentView(root);
        return root;
    }

    private Button button(String label, Runnable action) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextColor(Color.WHITE);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(PANEL));
        b.setOnClickListener(v -> action.run());
        return b;
    }

    private void showHome() {
        page("Practice smarter");
        root.addView(text("Standalone aim-training companion", 15, Color.LTGRAY));
        root.addView(button("Aim Practice", this::showPractice));
        root.addView(button("Sensitivity Settings", this::showSettings));
        root.addView(text("This app does not connect to or modify any game.", 12, Color.GRAY));
    }

    private void showSettings() {
        page("Sensitivity profiles");
        addSlider("General", 0);
        addSlider("Red Dot", 1);
        addSlider("2X Scope", 2);
        addSlider("4X Scope", 3);
        addSlider("Sniper Scope", 4);
        root.addView(button("Back", this::showHome));
    }

    private void addSlider(String label, int index) {
        TextView value = text("", 14, Color.WHITE);
        int saved = prefs.getInt("s" + index, new int[]{95,90,85,80,50}[index]);
        value.setText(label + ": " + saved);
        root.addView(value);
        SeekBar bar = new SeekBar(this);
        bar.setMax(100);
        bar.setProgress(saved);
        bar.setProgressTintList(android.content.res.ColorStateList.valueOf(RED));
        bar.setThumbTintList(android.content.res.ColorStateList.valueOf(RED));
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                value.setText(label + ": " + progress);
                if (fromUser) prefs.edit().putInt("s" + index, progress).apply();
            }
            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        root.addView(bar);
    }

    private void showPractice() {
        page("Aim practice");
        root.addView(text("Tap the target as it moves. This is a local practice game.", 13, Color.LTGRAY));
        PracticeView practice = new PracticeView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        params.topMargin = 14;
        params.bottomMargin = 14;
        root.addView(practice, params);
        root.addView(button("Back", this::showHome));
    }

    private class PracticeView extends View {
        private final android.graphics.Paint paint = new android.graphics.Paint(3);
        private float targetX = 180, targetY = 180, radius = 38;
        private int score = 0, attempts = 0;
        private final java.util.Random random = new java.util.Random();

        PracticeView(Activity context) {
            super(context);
            setBackgroundColor(PANEL);
        }

        @Override protected void onDraw(android.graphics.Canvas canvas) {
            super.onDraw(canvas);
            paint.setColor(Color.WHITE);
            paint.setTextSize(18);
            canvas.drawText("Hits: " + score + "    Taps: " + attempts, 18, 32, paint);
            paint.setColor(RED);
            canvas.drawCircle(targetX, targetY, radius, paint);
            paint.setColor(Color.WHITE);
            paint.setStyle(android.graphics.Paint.Style.STROKE);
            canvas.drawCircle(targetX, targetY, radius + 8, paint);
            paint.setStyle(android.graphics.Paint.Style.FILL);
        }

        @Override public boolean onTouchEvent(android.view.MotionEvent event) {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                attempts++;
                float dx = event.getX() - targetX, dy = event.getY() - targetY;
                if (dx * dx + dy * dy <= radius * radius) score++;
                targetX = radius + random.nextFloat() * Math.max(1, getWidth() - 2 * radius);
                targetY = 75 + random.nextFloat() * Math.max(1, getHeight() - 100);
                invalidate();
                return true;
            }
            return true;
        }
    }
}
