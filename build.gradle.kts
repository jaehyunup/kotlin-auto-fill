plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.7.20"
    // Gradle Changelog Plugin
    id("org.jetbrains.changelog") version "2.0.0"
    id("org.jetbrains.intellij") version "1.10.1"
}

group = "io.autofill.kotlin"
version = "1.0.0"

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    pluginName.set(getProperty("pluginName"))
    type.set(getProperty("platformType"))
    version.set(getProperty("platformVersion"))
    plugins.set(getProperties("platformPlugins"))
}


tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("223")
//        pluginDescription.set(file("src/main/resources/META-INF/description.html").readText())
        pluginDescription.set(
            projectDir.resolve("README.md").readText().lines().run {
                val start = "<!-- Plugin description start -->"
                val end = "<!-- Plugin description end -->"

                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end))
            }.joinToString("\n").let {
                org.jetbrains.changelog.flavours.ChangelogFlavourDescriptor().run {
                    org.intellij.markdown.html.HtmlGenerator(
                        it,
                        org.intellij.markdown.parser.MarkdownParser(this).buildMarkdownTreeFromString(it),
                        this,
                        false
                    ).generateHtml()
                }
            }
        )
    }

    buildSearchableOptions {
        enabled = false
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}

fun getProperty(name: String) = "${project.findProperty(name).toString()}"
fun getProperties(name: String) = getProperty(name).split(",").map { it.trim() }