package com.f1.quiket.composeapp

internal expect object AppMetadata {
    val appName: String
    val versionName: String
    val buildNumber: String
}
