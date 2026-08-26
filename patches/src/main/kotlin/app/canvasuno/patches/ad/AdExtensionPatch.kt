package app.canvasuno.patches.ad

import app.morphe.patcher.patch.bytecodePatch

/**
 * Internal patch that merges the Canvas UNO extension (precompiled DEX)
 * into the app exactly once. Both public patches depend on it.
 * Has no name, so it is not shown in Morphe Manager.
 */
internal val adExtensionPatch = bytecodePatch {
    extendWith("extensions/extension.mpe")
}
