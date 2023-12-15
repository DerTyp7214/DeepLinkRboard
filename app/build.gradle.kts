import com.android.build.api.dsl.Packaging
import org.jetbrains.kotlin.config.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    alias(libs.plugins.ksp)
}

@Suppress("UnstableApiUsage")
android {
    compileSdk = 34
    buildToolsVersion = "34.0.0"
    buildFeatures.dataBinding = true
    buildFeatures.viewBinding = true
    buildFeatures.buildConfig = true

    defaultConfig {
        applicationId = "de.dertyp7214.deeplinkrboard"
        minSdk = 23
        targetSdk = 34
        versionCode = 113000
        versionName = "1.1.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        resourceConfigurations += listOf(
            "af", "cs", "da", "de",
            "el", "en", "es", "fi",
            "fr", "hi", "hu", "id",
            "it", "ja", "nl", "no",
            "pl", "pt", "ro", "ru",
            "sv", "uk", "vi", "zh-rCN",
            "zh-rTW"
        )
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("debug") {
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_20
        targetCompatibility = JavaVersion.VERSION_20
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_20.toString()
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
        resources.excludes.add("META-INF/*")
    }
    namespace = "de.dertyp7214.deeplinkrboard"
}

dependencies {
    implementation(project(":colorutilsc"))
    implementation(project(":rboardcomponents"))
    implementation(libs.preferencesplus)
    implementation(libs.legacy.support.v4)
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.core.ktx)
    //noinspection DifferentStdlibGradleVersion
    implementation(libs.kotlin.stdlib)
    implementation(libs.core)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.preference.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.gson)
    implementation(libs.prdownloader)
    implementation(libs.androidx.appcompat)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.androidx.browser)
    coreLibraryDesugaring(libs.desugar.jdk.libs.nio)

    debugImplementation(libs.androidx.ui.tooling)
    implementation(libs.kotlin.reflect)
}
