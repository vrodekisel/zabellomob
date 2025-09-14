plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.zabello"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.zabello"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        vectorDrawables { useSupportLibrary = true }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                argument("room.schemaLocation", "$projectDir/schemas")
                argument("room.incremental", "true")
                argument("room.expandProjection", "true")
            }
        }
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

    buildFeatures {
        viewBinding = true
    }

    // нужно для Robolectric, если в unit-тестах используешь ApplicationProvider и ресурсы
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    // --- UI / base ---
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.navigation:navigation-fragment:2.8.2")
    implementation("androidx.navigation:navigation-ui:2.8.2")

    // --- Lifecycle ---
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.6")
    implementation("androidx.lifecycle:lifecycle-livedata:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime:2.8.6")

    // --- Room ---
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1") // часть API пригодится даже в Java
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    // --- WorkManager / Core ---
    implementation("androidx.work:work-runtime:2.9.1")
    implementation(libs.core)
    implementation(libs.firebase.firestore)

    // --- Unit tests (src/test/java) ---
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.test:core:1.5.0")              // ApplicationProvider
    testImplementation("org.robolectric:robolectric:4.12.2")    // запуск Android-кода на JVM
    testImplementation("androidx.room:room-testing:2.6.1")      // Room helpers

    // --- Instrumented tests (src/androidTest/java) ---
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // --- Network: Retrofit + OkHttp ---
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
}
