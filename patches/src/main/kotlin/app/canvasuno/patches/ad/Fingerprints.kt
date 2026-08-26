package app.canvasuno.patches.ad

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Matches [UNO#CustomApplication.onCreate].
 *
 * The class name is a stable, non-obfuscated app entry point,
 * so this fingerprint is expected to survive app updates.
 */
object CustomApplicationOnCreateFingerprint : Fingerprint(
    definingClass = "Lcom/netease/uno/CustomApplication;",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "V",
    parameters = listOf(),
    name = "onCreate",
)
