fun properties(key: String) = project.findProperty(key).toString()

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.7.10" apply false
    java
}

subprojects {
    group = "com.testing"
    repositories {
        mavenCentral()
    }

    tasks.whenTaskAdded {
        if (name.contains("buildSearchableOptions")) {
            enabled = false
        }
    }


    project.plugins.withId("org.jetbrains.intellij") {

        tasks.named("downloadRobotServerPlugin").configure {
            version = properties("remoteRobotVersion")
        }
    }
}
