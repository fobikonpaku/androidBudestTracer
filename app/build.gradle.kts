plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.budgettracker"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.budgettracker"
        minSdk = 24
        targetSdk = 34
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("org.jetbrains:annotations:15.0")
    implementation("org.jetbrains:annotations:15.0")
//    implementation(libs.recyclerview)
    implementation("com.google.code.gson:gson:2.8.5")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("org.projectlombok:lombok:1.18.24")


    implementation ("cz.adaptech.tesseract4android:tesseract4android:4.7.0")
}