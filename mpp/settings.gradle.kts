pluginManagement {
    repositories {
        google()
        jcenter()
        gradlePluginPortal()
        mavenCentral()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
        maven { url = uri("https://jitpack.io") }

    }
    
}
rootProject.name = "nexus_mpp"


include(":android")
include(":desktop")
include(":common")

