package app.canvasuno.extension;

import android.app.Activity;
import android.app.Application;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.VideoView;

/**
 * Black curtain: while a fullscreen ad activity is on screen, paint its window
 * black and hide every media view (WebView / VideoView / SurfaceView / TextureView).
 *
 * Controls drawn natively by the ad SDK (close button, skip button, countdown clock)
 * are plain views, so they remain visible and clickable. Playback keeps running in
 * the background, which means ad lifecycle callbacks and rewards still work and no
 * server-side behaviour is changed.
 */
@SuppressWarnings("unused")
public final class CurtainHook implements Application.ActivityLifecycleCallbacks {

    private static final Object LOCK = new Object();
    private static boolean sRegistered;

    /**
     * Ad views are inflated asynchronously after the activity resumes,
     * so the hierarchy is swept a few times to catch late-added views.
     */
    private static final long[] SWEEP_DELAYS_MS = {50L, 300L, 800L, 1500L, 2500L, 4000L};

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private CurtainHook() {
    }

    public static void init(Application application) {
        if (application == null) {
            return;
        }
        synchronized (LOCK) {
            if (sRegistered) {
                return;
            }
            sRegistered = true;
            application.registerActivityLifecycleCallbacks(new CurtainHook());
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        try {
            if (!AdTargets.isFullscreenAdActivity(activity)) {
                return;
            }
        } catch (Throwable t) {
            return;
        }
        applyCurtain(activity);
    }

    private void applyCurtain(final Activity activity) {
        try {
            Window window = activity.getWindow();
            if (window != null) {
                window.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
            }
        } catch (Throwable t) {
            // Ignore, sweeping below is enough on exotic setups.
        }
        // Immediate sweep plus delayed ones for asynchronously inflated ad views.
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                sweep(activity);
            }
        });
        for (long delay : SWEEP_DELAYS_MS) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    sweep(activity);
                }
            }, delay);
        }
    }

    private void sweep(Activity activity) {
        try {
            if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
                return;
            }
            View content = activity.findViewById(android.R.id.content);
            if (content instanceof ViewGroup) {
                hideMediaViews((ViewGroup) content);
            }
        } catch (Throwable t) {
            // Never break the hosting app.
        }
    }

    private static void hideMediaViews(ViewGroup group) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child instanceof ViewGroup) {
                hideMediaViews((ViewGroup) child);
                continue;
            }
            if (isMediaView(child) && child.getVisibility() == View.VISIBLE) {
                // INVISIBLE keeps the layout intact (buttons stay where the SDK put them),
                // while the actual ad pixels are no longer drawn.
                child.setVisibility(View.INVISIBLE);
            }
        }
    }

    private static boolean isMediaView(View view) {
        return view instanceof WebView
                || view instanceof VideoView
                || view instanceof android.view.SurfaceView
                || view instanceof android.view.TextureView;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }
}
