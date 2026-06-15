package com.ivan.freeglukmp.utils

import coil3.PlatformContext
import okio.Path
import okio.Path.Companion.toPath

actual fun getCacheDir(context: PlatformContext): Path? {
    return context.cacheDir.resolve("image_cache").absolutePath.toPath()
}