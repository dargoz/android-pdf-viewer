plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}


android {
    namespace = "com.github.barteksc.sample"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
        targetSdk = 35
        versionCode = 3
        versionName = "3.1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

}

dependencies {
    implementation(project(":android-pdf-viewer"))
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.github.dargoz:pdfium-android:2.1.0@aar")
}
