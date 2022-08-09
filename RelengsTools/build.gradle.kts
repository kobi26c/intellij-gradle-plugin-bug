import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = project.findProperty(key).toString()

fun readVersion(): String {
    val xmlDoc: org.w3c.dom.Document =
        javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(project.file("src/main/resources/META-INF/plugin.xml"))
    val textContent = xmlDoc.getElementsByTagName("version").item(0).textContent
    println("Version found in XML: $textContent")
    return textContent
}


plugins {
    id("org.jetbrains.intellij")
    pmd
    id("org.jetbrains.changelog")
    id("org.jetbrains.kotlin.jvm")
    distribution
    `java-library`
    `maven-publish`

}


val pluginVersion = readVersion()
val repository = project.findProperty("repo")

group = properties("pluginGroup")
version = pluginVersion

description = "RelengsTools"

// Configure project's dependencies
repositories {
    mavenCentral()
}

dependencies {
    compileOnly("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.0")

    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.squareup.moshi:moshi:1.13.0")
    implementation("com.github.ben-manes.caffeine:caffeine:3.0.5")

    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("mysql:mysql-connector-java:8.0.25")
    implementation("org.elasticsearch.client:elasticsearch-rest-high-level-client:7.16.1") {
        exclude(group = "com.fasterxml.jackson.core")
        exclude(group = "com.fasterxml.jackson.dataforma")
    }

    // https://mvnrepository.com/artifact/org.mockito/mockito-core
    testImplementation("org.mockito:mockito-core:4.1.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.9.3")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${properties("kotlinVersion")}")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${properties("kotlinVersion")}")
    implementation ("com.github.vertical-blank:sql-formatter:2.0.3")
}



// Configure gradle-intellij-plugin plugin.
// Read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    pluginName.set("RelengsTools")
    version.set(properties("platformVersion"))
    type.set(properties("platformType"))
    downloadSources.set(properties("platformDownloadSources").toBoolean())
    updateSinceUntilBuild.set(true)
    sameSinceUntilBuild.set(false)
    instrumentCode.set(true)
    intellij.plugins.set(properties("platformPlugins").split(',').map(String::trim).filter(String::isNotEmpty))
}


// Configure gradle-changelog-plugin plugin.
// Read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    version.set(pluginVersion)
    groups.set(emptyList())
}

pmd {
    toolVersion = "6.21.0"
    ruleSetFiles = files("src/main/resources/config/pmd/pmd.xml")
    ruleSets = listOf()
    incrementalAnalysis.set(true)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

tasks { // Set the compatibility versions to 1.8

    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "11"
            languageVersion = "1.7"
            freeCompilerArgs = freeCompilerArgs + ("-Xjvm-default=enable")
            apiVersion = "1.4"
        }
    }

    patchPluginXml {
        version.set(pluginVersion)
        sinceBuild.set(properties("pluginSinceBuild"))
        untilBuild.set(properties("pluginUntilBuild"))


        // Get the latest available change notes from the changelog file
        changeNotes.set(provider { changelog.getLatest().toHTML() })
    }

    runPluginVerifier {
        ideVersions.set(properties("pluginVerifierIdeVersions").split(',').map(String::trim).filter(String::isNotEmpty))
    }

    runIdeForUiTests {
        systemProperty("robot-server.port", properties(
            "remoteRobotPort")) //By default, the port is local, so it could not be reached from another host. In case you need to make it public, you can add system property in the runIdeForUiTests task:
        //systemProperty ("robot-server.host.public", "true")
    }

    runIde {
        jvmArgs = listOf("-Xmx4G")
    }

    downloadRobotServerPlugin {
        version.set(properties("remoteRobotVersion"))
    }
}


val distTar = tasks.getByName("distTar")
distTar.enabled = false


publishing {
    publications {
        create<MavenPublication>("myDistribution") {
            artifact(tasks.distZip)
        }
    }
    repositories {
        maven { // change to point to your repo, e.g. http://my.org/repo
            url = uri("$repository")
            name = "artifactory"
            credentials(PasswordCredentials::class)
        }
    }
}



