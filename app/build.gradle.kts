import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

val localProperties = Properties().apply {
    val propertiesFile = rootProject.file("local.properties")
    if (propertiesFile.exists()) {
        propertiesFile.inputStream().use { load(it) }
    }
}
val mapkitApiKey = providers.environmentVariable("MAPKIT_API_KEY")
    .orElse(localProperties.getProperty("MAPKIT_API_KEY", ""))
    .get()
val escapedMapkitApiKey = mapkitApiKey.replace("\\", "\\\\").replace("\"", "\\\"")
    .replace("\n", "\\n").replace("\r", "\\r")

android {
    buildFeatures {
        buildConfig = true
    }
    namespace = "ru.danii.taxiappcw"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "ru.danii.taxiappcw"
        minSdk = 24
        targetSdk = 36
        buildConfigField("String", "MAPKIT_API_KEY", "\"$escapedMapkitApiKey\"")
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Яндекс Карты (Исправленный синтаксис для Kotlin DSL)
    implementation("com.yandex.android:maps.mobile:4.4.0-full")
}

