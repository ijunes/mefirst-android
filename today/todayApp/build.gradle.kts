plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.ijunes.mefirst.today.app"
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
    api(project(":today"))
    implementation(project(":database"))
    implementation(project(":common"))
    implementation(project(":today:todayImpl"))
    implementation(libs.koin)
}
