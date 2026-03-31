plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.ijunes.mefirst.entries.app"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(project(":database"))
    implementation(project(":common"))
    implementation(project(":entries"))
    implementation(project(":entries:entriesImpl"))
    implementation(libs.koin)
}
