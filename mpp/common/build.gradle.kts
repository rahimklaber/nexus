import org.jetbrains.compose.compose

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose") version "0.3.1"
    id("com.android.library")
    id("kotlin-android-extensions")

}

group = "me.rahim"
version = "1.0"

repositories {
    google()
    maven { url = uri("https://jitpack.io") }
}

kotlin {
    android()
    jvm("desktop") {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val commonJvmAndroid = create("commonJvmAndroid") {
            dependsOn(commonMain)
            dependencies{
                api("com.github.stellar:java-stellar-sdk:0.26.0")
                api("com.moandjiezana.toml:toml4j:0.7.2")
                api("com.beust:klaxon:5.5")
                api("com.github.kittinunf.fuel:fuel:2.3.1")
                api("org.ktorm:ktorm-core:3.4.1")
                api("org.ktorm:ktorm-support-sqlite:3.4.1")
                api("org.xerial:sqlite-jdbc:3.36.0")
            }

        }
        val androidMain by getting {
            dependsOn(commonJvmAndroid)
//            kotlin.srcDir("src/commonJvmAndroid/kotlin")
            dependencies {
                api("androidx.appcompat:appcompat:1.2.0")
                api("androidx.core:core-ktx:1.3.1")
                api("com.github.stellar:java-stellar-sdk:0.26.0")


            }
        }
        val androidTest by getting {
            dependencies {
                implementation("junit:junit:4.13")
            }
        }
        val desktopMain by getting{
//            kotlin.srcDir("src/commonJvmAndroid/kotlin")
            dependsOn(commonJvmAndroid)
            dependencies{
                api("com.github.stellar:java-stellar-sdk:0.26.0")
            }
        }
        val desktopTest by getting
    }
}

android {
    compileSdkVersion(29)
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdkVersion(24)
        targetSdkVersion(29)
    }
}