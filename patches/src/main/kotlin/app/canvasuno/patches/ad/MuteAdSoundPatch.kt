package app.canvasuno.patches.ad

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.canvasuno.patches.shared.Constants.COMPATIBILITY_UNO

private const val HOOK_CLASS = "Lapp/canvasuno/extension/MuteHook;"

/**
 * The extension registers an activity lifecycle callback on the application.
 * While a fullscreen ad activity is resumed, the music stream volume is forced to
 * zero using the system AudioManager; when the ad closes, the previous volume is
 * restored. This works for every ad network regardless of its own mute options.
 */
@Suppress("unused")
val muteAdSoundPatch = bytecodePatch(
    name = "Mute ad sound",
    description = "Mutes the system media volume while an ad is playing and restores it afterwards.",
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
