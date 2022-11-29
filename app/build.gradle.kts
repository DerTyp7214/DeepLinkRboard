import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion

plugins {
    id("com.android.application")
    id("kotlin-android-extensions")
    kotlin("android")
    kotlin("kapt")
}

val kotlinVersion: String = project.getKotlinPluginVersion()

@Suppress("UnstableApiUsage")
android {
    compileSdk = 33
    buildToolsVersion = "33.0.1"
    buildFeatures.dataBinding = true
    buildFeatures.viewBinding = true

    defaultConfig {
        applicationId = "de.dertyp7214.deeplinkrboard"
        minSdk = 23
        targetSdk = 33
        versionCode = 101000
        versionName = "1.0.1"

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
        sourceCompatibility = JavaVersion.VERSION_15
        targetCompatibility = JavaVersion.VERSION_15
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_15.toString()
    }

    packagingOptions {
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
    implementation("com.github.DerTyp7214:PreferencesPlus:1.0")

    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.6.0-alpha04")
    implementation("androidx.navigation:navigation-ui-ktx:2.6.0-alpha04")
    implementation("org.apache.commons:commons-text:1.10.0")
    implementation("dev.chrisbanes.insetter:insetter:0.6.1")
    implementation("androidx.core:core-ktx:1.9.0")
    //noinspection DifferentStdlibGradleVersion
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.7.21")
    implementation("androidx.core:core:1.9.0")
    implementation("com.google.android.material:material:1.8.0-alpha03")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0-alpha04")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("androidx.activity:activity-ktx:1.7.0-alpha02")
    implementation("androidx.fragment:fragment-ktx:1.6.0-alpha03")
    implementation("com.jaredrummler:android-shell:1.0.0")
    implementation("com.google.firebase:firebase-analytics:21.2.0")
    implementation("com.google.firebase:firebase-messaging:23.1.0")
    implementation("com.google.code.gson:gson:2.10")
    implementation("com.bignerdranch.android:simple-item-decoration:1.0.0")
    implementation("de.dertyp7214:PRDownloader:v0.6.0")
    implementation("com.github.skydoves:balloon:1.4.8-SNAPSHOT")
    implementation("androidx.appcompat:appcompat:1.7.0-alpha01")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.4")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.0")
    implementation("androidx.browser:browser:1.4.0")
    implementation("com.github.bumptech.glide:glide:4.14.2")
    kapt("com.github.bumptech.glide:compiler:4.14.2")
    implementation("com.github.murgupluoglu:flagkit-android:1.0.2")
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))

    debugImplementation("androidx.compose.ui:ui-tooling:1.4.0-alpha02")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.7.21")
}