package app.canvasuno.patches.ad

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.canvasuno.patches.shared.Constants.COMPATIBILITY_UNO

private const val HOOK_CLASS = "Lapp/canvasuno/extension/CurtainHook;"

/**
 * The extension registers an activity lifecycle callback on the application.
 * Whenever a fullscreen ad activity resumes, the hook paints the window black and
 * hides all media views (WebView / VideoView / SurfaceView / TextureView).
 *
 * Native controls drawn by the ad SDK (close button, skip button, countdown clock)
 * are regular views and stay visible and clickable, so ads can still be dismissed
 * and rewarded ad callbacks still fire. No server communication is modified.
 */
@Suppress("unused")
val blackCurtainPatch = bytecodePatch(
    name = "Black curtain",
    description = "Hides fullscreen ad visuals behind a black screen while keeping close and skip buttons visible.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_UNO)

    dependsOn(adExtensionPatch)

    execute {
        CustomApplicationOnCreateFingerprint.method.addInstructions(
            0,
            """
                invoke-static {p0}, $HOOK_CLASS->init(Landroid/app/Application;)V
            """,
        )
    }
}
