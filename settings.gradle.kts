pluginManagement {
    plugins {
        id("org.jetbrains.intellij") version "1.8.0" apply false
        id ("de.undercouch.download") version "4.1.1"
        id ("org.jetbrains.changelog") version "1.2.1"
        id ("com.adarshr.test-logger") version "3.0.0" apply(false)
    }

    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }
        maven {
            url = uri("https://www.jetbrains.com/intellij-repository/releases")
        }
        maven {
            url = uri("https://jetbrains.bintray.com/intellij-plugin-service")
        }
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }
}

rootProject.name = "intelliJPlugin"


include ("RelengsTools")
include("Fast")

