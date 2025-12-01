plugins {
    alias(libs.plugins.android.application)
    // Cukup gunakan alias ini, yang sudah mencakup id("com.google.gms.google-services")
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.deliveryfood"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.deliveryfood"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures{
        viewBinding = true
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
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.google.android.material:material:1.11.0")

    // Import Firebase BoM (Bill of Materials)
    // Pastikan versi BOM sudah didefinisikan di file libs.versions.toml Anda
    implementation(platform("com.google.firebase:firebase-bom:33.1.0")) // Saya gunakan versi stabil, sesuaikan jika perlu

    // Tambahkan dependency untuk Authentication dan Firestore (Database)
    // Gunakan tanda kutip ganda
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.github.bumptech.glide:glide:4.16.0")
}