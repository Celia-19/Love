plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
    id("kotlin-kapt") // Necesario para Glide
}

android {
    namespace = "com.example.love"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.love"
        minSdk = 26
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }

}

dependencies {

    // AndroidX y Material
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Navegación
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.play.services.maps)

    // Pruebas
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0") // Procesador de anotaciones para Glide

    // CircleImageView
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // Volley
    implementation("com.android.volley:volley:1.2.1")

    // Async HTTP
    implementation("com.loopj.android:android-async-http:1.4.9")

    // Gson
    implementation("com.google.code.gson:gson:2.8.9")

    // Biometric Authentication
    implementation("androidx.biometric:biometric:1.2.0-alpha03")

    // Retrofit con Gson Converter
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.11.0")

    // Seguridad
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("org.nanohttpd:nanohttpd:2.3.1")

    // Agregar Firebase Messaging
    implementation("com.google.firebase:firebase-messaging:23.2.0")

    // Dependencia de OSMDroid para mapas
    implementation("org.osmdroid:osmdroid-android:6.1.14")
}