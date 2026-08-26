package app.canvasuno.extension;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

/**
 * Mute ad sound: while a fullscreen ad activity is resumed, force the system
 * music stream volume to zero through AudioManager (system level, not an SDK
 * mute button), and restore the previous volume once the ad is dismissed.
 */
@SuppressWarnings("unused")
public final class MuteHook implements Application.ActivityLifecycleCallbacks {

    private static final Object LOCK = new Object();
    private static boolean sRegistered;
    private static int sAdResumeCount;
    private static int sSavedVolume = -1;

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private MuteHook() {
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
            application.registerActivityLifecycleCallbacks(new MuteHook());
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
        synchronized (LOCK) {
            sAdResumeCount++;
        }
        // Small delay so the game cannot raise the volume back right after us.
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                applyMute(activity);
            }
        }, 250L);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        try {
            if (!AdTargets.isFullscreenAdActivity(activity)) {
                return;
            }
        } catch (Throwable t) {
            return;
        }
        synchronized (LOCK) {
            sAdResumeCount = Math.max(0, sAdResumeCount - 1);
        }
        // Restore shortly after the ad closes; skipped if another ad resumed meanwhile.
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                restoreVolumeIfNeeded(activity);
            }
        }, 200L);
    }

    private static void applyMute(Activity activity) {
        try {
            if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
                return;
            }
            boolean stillInAd;
            synchronized (LOCK) {
                stillInAd = sAdResumeCount > 0;
            }
            if (!stillInAd) {
                return;
            }
            AudioManager audio =
                    (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
            if (audio == null) {
                return;
            }
            int current = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
            synchronized (LOCK) {
                if (current > 0) {
                    sSavedVolume = current;
                }
            }
            audio.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
        } catch (Throwable t) {
            // Never break the hosting app.
        }
    }

    private static void restoreVolumeIfNeeded(Activity activity) {
        try {
            boolean stillInAd;
            int saved;
            synchronized (LOCK) {
                stillInAd = sAdResumeCount > 0;
                saved = sSavedVolume;
                sSavedVolume = -1;
            }
            if (stillInAd || saved < 0 || activity == null) {
                return;
            }
            AudioManager audio =
                    (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
            if (audio == null) {
                return;
            }
            if (audio.getStreamVolume(AudioManager.STREAM_MUSIC) == 0) {
                audio.setStreamVolume(AudioManager.STREAM_MUSIC, saved, 0);
            }
        } catch (Throwable t) {
            // Never break the hosting app.
        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
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
