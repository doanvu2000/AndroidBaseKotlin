import java.text.SimpleDateFormat
import java.util.Date

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.googleService)
    alias(libs.plugins.firebaseCrashlyticsGradle)
}

android {
    compileSdk = 36
    namespace = "com.example.baseproject"
    defaultConfig {
        applicationId = "com.example.baseproject"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        val formattedDate = SimpleDateFormat("MM.dd.yyyy_hh.mm").format(Date())
        base.archivesName = "Base_Project_Kotlin_${formattedDate}"

        buildConfigField("AD_MOD_APP_ID", "ca-app-pub-3940256099942544~3347511713")

        //key test
        buildConfigField("ADS_BANNER_TEST", " /21775744923/example/adaptive-banner")
        buildConfigField("ADS_BANNER_COLLAPSIBLE_TEST", "ca-app-pub-3940256099942544/2014213617")
        buildConfigField("ADS_INTER_TEST", "/21775744923/example/interstitial")
        buildConfigField("ADS_NATIVE_TEST", "/21775744923/example/native")
        buildConfigField("ADS_NATIVE_TEST_VIDEO", "/21775744923/example/native-video")
        buildConfigField("ADS_ADMOB_OPEN_APP_TEST", "/21775744923/example/app-open")

        buildConfigField("ADS_ADMOB_BANNER", "ca-app-pub-3940256099942544/9214589741")

        buildConfigField("ADS_ADMOB_INTER_ID1", "ca-app-pub-3940256099942544/1033173712")
        buildConfigField("ADS_ADMOB_INTER_ID2", "ca-app-pub-3940256099942544/1033173712")

        buildConfigField("ADS_NATIVE_ID1", "ca-app-pub-3940256099942544/2247696110")
        buildConfigField("ADS_NATIVE_ID2", "ca-app-pub-3940256099942544/2247696110")

        buildConfigField("ADS_ADMOB_OPEN_APP_ID1", "ca-app-pub-3940256099942544/9257395921")
        buildConfigField("ADS_ADMOB_OPEN_APP_ID2", "ca-app-pub-3940256099942544/9257395921")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )

            manifestPlaceholders["AD_MOD_APP_ID"] = "ca-app-pub-2622117118790581~6221023877"
        }

        debug {
            manifestPlaceholders["AD_MOD_APP_ID"] = "ca-app-pub-2622117118790581~6221023877"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":skeleton"))
    implementation(project(":sliderview"))
    implementation(project(":stickerview"))
    implementation(project(":simplecropview"))
    implementation(project(":cameraview"))
    implementation(project(":weekviewevent"))

    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(libs.activity)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.androidx.lifecycle.process)

    //retrofit
    implementation(libs.retrofit)
    // https://mvnrepository.com/artifact/com.squareup.retrofit2/converter-gson
    implementation(libs.converter.gson)

    // ViewModel
    implementation(libs.lifecycle.viewmodel.ktx)
    // LiveData
    implementation(libs.lifecycle.livedata.ktx)
    // Lifecycles only (without ViewModel or LiveData)
    implementation(libs.lifecycle.runtime.ktx)

//    //Coroutines
//   implementation( "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    //logging
    implementation(libs.logging.interceptor)
    //M_MKV
    implementation(libs.mmkv.static)

    //glide
    implementation(libs.glide)
    //lottie
    implementation(libs.lottie)

    //biometric - authenticator with fingerprint
    implementation(libs.biometric.ktx)

    implementation(libs.review.ktx)
    implementation(libs.review)

    //location
    implementation(libs.play.services.location)

    //firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics.ktx)
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.firebase.config.ktx)
    //ads
    implementation(libs.play.services.ads)
    //coil (Coroutine Image loader)
    implementation(libs.coil)

    //flexbox layout recyclerView
    implementation(libs.flexbox)

    implementation(libs.commons.lang3)
}

inline fun <reified ValueT> com.android.build.api.dsl.VariantDimension.buildConfigField(
    name: String, value: ValueT
) {
    val resolvedValue = when (value) {
        is String -> "\"$value\""
        else -> value
    }.toString()
    buildConfigField(ValueT::class.java.simpleName, name, resolvedValue)
}