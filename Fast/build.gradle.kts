import java.net.URL

plugins {
    id("org.jetbrains.intellij")
    id("maven-publish")
    java
}

System.out.println("Java version: " + System.getProperty("java.version"))

fun properties(key: String) = project.findProperty(key).toString()

fun readVersion(): String {
    val xmlDoc: org.w3c.dom.Document =
        javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(project.file("src/main/resources/META-INF/plugin.xml"))
    val textContent = xmlDoc.getElementsByTagName("version").item(0).textContent
    println("Version found in XML: $textContent")
    return textContent
}

val pluginVersion = readVersion()

description = "FastLauncher"
group = "com.video"
version = pluginVersion
project.ext.set("artifact", "shortcutplugin-${version}.jar")


val repository = project.findProperty("repo")

intellij {
    pluginName.set("shortcutplugin")
    version.set("IU-2022.2")
    instrumentCode.set(false)
    intellij.plugins.set(listOf("java", "git4idea", "junit", "maven", "maven-model"))
}

dependencies {
    implementation("net.lingala.zip4j:zip4j:2.9.0")
    implementation("org.javassist:javassist:3.25.0-GA")
    implementation("org.codehaus.jettison:jettison:1.3.7")
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }

    patchPluginXml {
        version.set(pluginVersion)
        sinceBuild.set("203")
        untilBuild.set("222.*")
    }

    runIde {
        jvmArgs = listOf("-Xmx6G")
    }
}

tasks.named("publish").configure {
    dependsOn("updateVersionService")
}



