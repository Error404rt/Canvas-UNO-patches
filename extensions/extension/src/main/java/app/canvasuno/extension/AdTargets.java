package app.canvasuno.extension;

import android.app.Activity;

/**
 * Detects fullscreen ad activities of every ad network bundled with UNO,
 * using only stable public SDK class names so this survives app updates.
 */
@SuppressWarnings("unused")
public final class AdTargets {

    private AdTargets() {
    }

    /** Activities that render fullscreen ads (interstitial, rewarded, app-open). */
    private static final String[] EXACT_AD_ACTIVITIES = {
            // AppLovin (direct + MAX mediation presenter host)
            "com.applovin.adview.AppLovinFullscreenActivity",
            // Google AdMob
            "com.google.android.gms.ads.AdActivity",
            // Meta Audience Network
            "com.facebook.ads.AudienceNetworkActivity",
            // Unity Ads
            "com.unity3d.services.ads.adunit.AdUnitActivity",
            "com.unity3d.services.ads.adunit.AdUnitSoftwareActivity",
            "com.unity3d.services.ads.adunit.AdUnitTransparentActivity",
            "com.unity3d.services.ads.adunit.AdUnitTransparentSoftwareActivity",
            // Liftoff / Vungle
            "com.vungle.ads.internal.ui.VungleActivity",
            "com.vungle.war.ui.VungleActivity",
            // ironSource / LevelPlay
            "com.ironsource.sdk.controller.ControllerActivity",
            "com.ironsource.sdk.controller.InterstitialActivity",
            // InMobi
            "com.inmobi.ads.rendering.InMobiAdActivity",
            // BidMachine (fullscreen ad rendering; the IAB/MRAID browser is intentionally excluded)
            "io.bidmachine.rendering.ad.fullscreen.FullScreenActivity",
            "io.bidmachine.iab.vast.activity.VastActivity"
    };

    /** Package prefixes whose activities are always ad UI (never a click-through browser). */
    private static final String[] AD_ACTIVITY_PREFIXES = {
            // Mintegral video players
            "com.mintegral.msdk.video.",
            "com.mintegral.msdk.interstitialvideoplayview.",
            // Moloco fullscreen renderers (MRAID browser excluded on purpose)
            "com.moloco.sdk.xenoss.sdkdevkit.android.adrenderer.internal.vast.",
            "com.moloco.sdk.xenoss.sdkdevkit.android.adrenderer.internal.staticrenderer."
    };

    /** Game/engine activities that must never be treated as ads. */
    private static final String[] NEVER_MATCH_PREFIXES = {
            "com.mattel.",
            "com.netease.",
            "com.unity3d.player.",
            "android.",
            "androidx."
    };

    public static boolean isFullscreenAdActivity(Activity activity) {
        try {
            String name = activity.getClass().getName();
            return matches(name);
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean matches(String name) {
        if (name == null) {
            return false;
        }
        for (String prefix : NEVER_MATCH_PREFIXES) {
            if (name.startsWith(prefix)) {
                return false;
            }
        }
        for (String exact : EXACT_AD_ACTIVITIES) {
            if (name.equals(exact)) {
                return true;
            }
        }
        for (String prefix : AD_ACTIVITY_PREFIXES) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }
        return false;

    }
}
