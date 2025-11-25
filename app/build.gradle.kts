plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.meowfocus"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.meowfocus"
        minSdk = 24
        targetSdk = 36
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
    // MPAndroidChart สำหรับกราฟ
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    // Material Design สำหรับ TabLayout
    implementation("com.google.android.material:material:1.9.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.drawerlayout:drawerlayout:1.1.1")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
