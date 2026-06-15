package com.ivan.freeglukmp.utils

import coil3.PlatformContext
import okio.Path
import okio.Path.Companion.toPath
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

actual fun getCacheDir(context: PlatformContext): Path? {
    val cachePath = NSSearchPathForDirectoriesInDomains(
        NSCachesDirectory,
        NSUserDomainMask,
        true
    ).firstOrNull() as? String ?: return null
    
    return "$cachePath/image_cache".toPath()
}