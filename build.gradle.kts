/* Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
}*/
plugins {
    // Apply the Android plugin
    alias(libs.plugins.android.application) apply false
    id("com.android.library") version "7.0.4" apply false
    kotlin("android") version "1.6.21" apply false
    kotlin("android.extensions") version "1.5.21" apply false



}





