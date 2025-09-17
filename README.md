# Android Base Kotlin

English | [Tiếng Việt](README_vi.md)

A modern, Kotlin-first, multi-module Android starter that bundles common UI widgets, utilities, and
a demo app. It targets Android 14 (SDK 34/36 toolchain) with Java/Kotlin 17, and ships ready-to-use
integrations for Navigation, Retrofit/OkHttp, Coil/Glide, Firebase (Crashlytics/Analytics/Remote
Config), Google Mobile Ads, Location, and more.

## At a glance

- Tooling: AGP 8.10.1, Kotlin 2.1.21, JDK 17
- Android: compileSdk 36, targetSdk 36, minSdk 26
- Modules:
  - app: Demo app wiring all features together
  - skeleton: Easy skeleton/shimmer loading states for views and lists
  - sliderview: Custom slider UI components
  - stickerview: Sticker/overlay widgets
  - simplecropview: Simple image cropping UI
  - cameraview: Camera view utilities/components
  - weekviewevent: Week calendar view (1–7 days), converted to Kotlin; includes samples
  - jinwidget: Shared widgets/utilities used across modules

## Table of contents

- Requirements
- Project structure
- Getting started
- Build & run
- Configuration (Firebase, Ads)
- Included libraries
- Module docs & samples
- Troubleshooting
- License

## Requirements

- Android Studio (latest stable recommended)
- JDK 17 (Gradle uses Java 17)
- Android SDK installed up to API 36

## Project structure

- settings.gradle.kts includes modules: app, skeleton, sliderview, stickerview, simplecropview,
  cameraview, weekviewevent, jinwidget
- Versions are centralized in gradle/libs.versions.toml

## Getting started

1) Clone the repository and open it in Android Studio.
2) Let Gradle sync; the project uses Version Catalogs and Kotlin DSL.
3) Run the app configuration on a device/emulator (Android 8.0+, API 26+).

To reuse modules in your own project, include them as Gradle module dependencies (example shown in
app/build.gradle.kts):

```kotlin
implementation(project(":jinwidget"))
implementation(project(":skeleton"))
implementation(project(":sliderview"))
implementation(project(":stickerview"))
implementation(project(":simplecropview"))
implementation(project(":cameraview"))
implementation(project(":weekviewevent"))
```

## Build & run

- compileSdk = 36, targetSdk = 36, minSdk = 26
- Java/Kotlin toolchain: 17
- ViewBinding enabled; BuildConfig fields generated for ad IDs

Gradle (Windows):

```bash
./gradlew clean
./gradlew :app:assembleDebug
```

APK artifacts will have a timestamped archivesName like Base_Project_Kotlin_MM.dd.yyyy_hh.mm.

## Configuration

This starter ships with Firebase and Google Mobile Ads integrations. Replace test keys with your own
before publishing.

### Firebase (Analytics, Crashlytics, Remote Config)

- google-services.json is expected under app/ (present in this repo).
- The project uses the Firebase BoM; Crashlytics Gradle plugin is applied.
- Console setup: enable Crashlytics, Analytics, and Remote Config for your app ID.

### Google Mobile Ads (AdMob)

- BuildConfig and manifestPlaceholders are used to inject AdMob IDs.
- app/build.gradle.kts defines placeholders like AD_MOD_APP_ID and test/prod unit IDs. Replace with
  your real IDs when going live.

## Included libraries (highlights)

- AndroidX: appcompat, core-ktx, material, constraintlayout, recyclerview, activity, lifecycle (
  viewmodel/livedata/runtime), navigation (fragment/ui/dynamic-features)
- Networking: retrofit 2 + gson converter, okhttp logging-interceptor
- Imaging/UI: coil, glide, lottie, Facebook shimmer, Flexbox
- Google Play: review, review-ktx, play-services-location, play-services-ads
- Storage/Utils: Tencent MMKV, Apache commons-lang3
- Firebase: analytics-ktx, crashlytics-ktx, config-ktx via Firebase BoM

Versions are defined in gradle/libs.versions.toml for easy updates.

## Module docs & samples

- skeleton: See skeleton/README.md for quick-start, configuration, and shimmer examples
- weekviewevent: See weekviewevent/README.md for API and screenshots
  - Sample images: weekviewevent/images/dayview.png, 3dayview.png, weekview.png

## Troubleshooting

- Ensure JDK 17 is configured in Android Studio (Gradle JVM).
- If Firebase sync fails, re-download google-services.json from the Firebase Console and place it
  under app/.
- For ads, use Google test IDs during development; real IDs require app review and policy
  compliance.
- Clean and rebuild if you change version catalog entries (libs.versions.toml).

## License

Specify your project license here (e.g., MIT, Apache-2.0). If unsure, add a LICENSE file at the
repository root and reference it in this section.

## Author

- Base: doanvu2000
- Support: [hoicham](https://github.com/PNThanggg), [vandatgsts](https://github.com/vandatgsts)