plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.minorfinal"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.minorfinal"
        minSdk = 28
        targetSdk = 35
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    aaptOptions {
        noCompress("tflite")

    }
    aaptOptions {
        noCompress("tflite")
    }



}
dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.foundation)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    // TensorFlow Lite
    implementation("org.tensorflow:tensorflow-lite:2.15.0")

    // TensorFlow Lite Task Library for Vision
    implementation("org.tensorflow:tensorflow-lite-task-vision-play-services:0.4.4")

    // (optional) If you use metadata or support libs
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
    implementation("androidx.compose.material:material-icons-extended:1.7.0")
    // 3. Foundation (for HorizontalPager)
    implementation("androidx.compose.foundation:foundation:1.7.0") // <-- THIS LINE IS CRITICAL

    // 4. Material Icons
    implementation("androidx.compose.material:material-icons-extended:1.7.0-alpha06")
    // Retrofit for networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")

    // Gson converter for parsing JSON
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // OkHttp logging (helps with debugging network calls)
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    implementation("com.google.android.gms:play-services-location:21.2.0")
    // ViewModel for Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")

    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Accompanist for Permissions (Google's library for Compose permissions)
    implementation("com.google.accompanist:accompanist-permissions:0.37.3")
}