plugins {
    id("com.android.application")
}

android {
    namespace = "rkr.simplekeyboard.inputmethod"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.lightclip.keyboard"
        minSdk = 24
        targetSdk = 35
        versionCode = 3
        versionName = "1.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Keep packaging simple and CI-friendly
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // Avoid dependency info bloat
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
}

dependencies {
    // Intentionally empty — keep the keyboard extremely lightweight
    // (only Android framework + our own code)
}
