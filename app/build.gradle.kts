plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.duan_laptop"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.duan_laptop"
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // --- Thư viện hệ thống ---
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)

    // --- Thư viện hỗ trợ UI ---
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("me.relex:circleindicator:2.1.6")

    // --- Xử lý hình ảnh & Network ---
    implementation("com.squareup.picasso:picasso:2.8")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.google.code.gson:gson:2.10.1")

    // --- Firebase ---
    implementation(platform("com.google.firebase:firebase-bom:34.7.0"))
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-analytics")
    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    implementation(libs.firebase.messaging)
    implementation("net.gotev:uploadservice:4.7.0")
    implementation("net.gotev:uploadservice-okhttp:4.7.0")
    implementation(libs.firebase.storage)
    implementation(libs.google.material)

    // --- Test ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
    implementation("com.r0adkll:slidableactivity:2.1.0")
    //thư viện animation cho dialog của nhà Airbnb
    implementation("com.airbnb.android:lottie:6.1.0")
    // thư viện dùng 2 ngón tay zoom ảnh chi tiết
    implementation("com.github.chrisbanes:PhotoView:2.3.0")
}