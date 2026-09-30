import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.shakeexpense.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.shakeexpense.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 4
        versionName = "4.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    val localProps = Properties().apply {
        val localPropsFile = rootProject.file("local.properties")
        if (localPropsFile.exists()) {
            FileInputStream(localPropsFile).use { load(it) }
        }
    }

    val releaseStoreFile = System.getenv("SHAKEEXPENSE_STORE_FILE")
        ?: localProps.getProperty("shakeexpense.storeFile")
        ?: "shakeexpense.keystore"
    val releaseStorePassword = System.getenv("SHAKEEXPENSE_STORE_PASSWORD")
        ?: localProps.getProperty("shakeexpense.storePassword")
        ?: ""
    val releaseKeyAlias = System.getenv("SHAKEEXPENSE_KEY_ALIAS")
        ?: localProps.getProperty("shakeexpense.keyAlias")
        ?: "shakeexpense"
    val releaseKeyPassword = System.getenv("SHAKEEXPENSE_KEY_PASSWORD")
        ?: localProps.getProperty("shakeexpense.keyPassword")
        ?: ""

    signingConfigs {
        create("release") {
            if (releaseStorePassword.isNotBlank()) {
                val keystoreFile = file(releaseStoreFile)
                if (keystoreFile.exists()) {
                    storeFile = keystoreFile
                    storePassword = releaseStorePassword
                    keyAlias = releaseKeyAlias
                    keyPassword = releaseKeyPassword
                }
            }
        }
    }

    buildTypes {
        debug {
            // Use standard Android debug signing
        }
        release {
            isMinifyEnabled = false
            val relConfig = signingConfigs.findByName("release")
            if (relConfig?.storeFile != null) {
                signingConfig = relConfig
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    // Firebase & Google Auth
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.play.services.auth)
    implementation(libs.coil.compose)
    implementation(libs.kotlinx.coroutines.play.services)

    // Razorpay Payment Gateway
    implementation(libs.razorpay.checkout)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockito.core)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
