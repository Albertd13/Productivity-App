// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    extra.apply {
        set("room_version", "2.6.1")
    }
}


plugins {
    alias(libs.plugins.androidApplication) apply false
    id("com.android.library") version "8.5.0" apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
}