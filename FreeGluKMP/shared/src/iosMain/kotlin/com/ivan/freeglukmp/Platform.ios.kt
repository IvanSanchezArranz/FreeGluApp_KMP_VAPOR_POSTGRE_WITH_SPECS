package com.ivan.freeglukmp

import platform.UIKit.UIDevice
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun getApiBaseUrl(): String {
    // Detect if running on iOS simulator
    val name = UIDevice.currentDevice.name
    val model = UIDevice.currentDevice.model
    val isSimulator = name.contains("Simulator") || model.contains("Simulator")
    
    return if (isSimulator) {
        "http://127.0.0.1:8080"
    } else {
        CLOUD_BACKEND_URL
    }
}

actual fun getCurrentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000.0).toLong()