package app.canvasuno.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    val COMPATIBILITY_UNO = Compatibility(
        name = "UNO",
        packageName = "com.matteljv.uno",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xE4002B,
        targets = listOf(
            // Version the patches were developed and confirmed working on.
            AppTarget(
                version = "1.17.9681"
            ),
            // Any future version is expected to work (fingerprints target stable, non-obfuscated classes).
            AppTarget(
                version = null,
                isExperimental = true
            )
        )
    )
}
