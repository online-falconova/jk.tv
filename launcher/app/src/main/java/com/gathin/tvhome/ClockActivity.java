package com.gathin.tvhome;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

/**
 * The clock screensaver shown by Home itself after home_idle_clock_minutes without input, or triggered
 * directly by the top-bar Sleep pill. It displays high-definition / 4K wallpapers rotating with slow ambient
 * zoom motion. Any remote key press smoothly zooms out back to the Home screen.
 */
public class ClockActivity extends Activity {
    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LauncherLocale.wrap(base));   // locale
    }

    private ClockScene scene;
    private View root;
    private boolean exiting = false;

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dream_clock);
        root = findViewById(R.id.dreamRoot);
        if (root == null) root = getWindow().getDecorView();

        ClockFormat.apply(getWindow().getDecorView(), Config.load(this).clockFormat);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE);

        // Cinematic Entrance: smoothly zooms in from 1.05 to 1.00
        root.setPivotX(getResources().getDisplayMetrics().widthPixels / 2f);
        root.setPivotY(getResources().getDisplayMetrics().heightPixels / 2f);
        root.setScaleX(1.05f);
        root.setScaleY(1.05f);
        root.setAlpha(0f);
        root.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(450)
                .setInterpolator(new DecelerateInterpolator(1.5f))
                .start();

        scene = new ClockScene(getWindow().getDecorView());
        scene.useHomeWallpaper(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        scene.start();
    }

    @Override
    protected void onPause() {
        scene.stop();
        super.onPause();
        exiting = true;
        finish();   // never left behind in the back stack
    }

    private void dismissWithZoomOut() {
        if (exiting) return;
        exiting = true;
        if (root != null) {
            root.animate().cancel();
            root.setPivotX(root.getWidth() / 2f);
            root.setPivotY(root.getHeight() / 2f);
            root.animate()
                    .scaleX(0.88f)
                    .scaleY(0.88f)
                    .alpha(0f)
                    .setDuration(320)
                    .setInterpolator(new AccelerateInterpolator(1.4f))
                    .withEndAction(ClockActivity.super::finish)
                    .start();
        } else {
            super.finish();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Any key down wakes the screensaver and smoothly zooms out to Home
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            dismissWithZoomOut();
            return true;
        }
        return true;
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_BUTTON_PRESS) {
            dismissWithZoomOut();
            return true;
        }
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void finish() {
        if (!exiting) {
            dismissWithZoomOut();
            return;
        }
        super.finish();
        overridePendingTransition(0, 0);
    }
}
