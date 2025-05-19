// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.firebaseCrashlyticsGradle) apply false
    alias(libs.plugins.googleService) apply false
}
////run use gradlew: ./gradlew clean
//task clean(type: Delete) {
//    delete rootProject.buildDir
//}