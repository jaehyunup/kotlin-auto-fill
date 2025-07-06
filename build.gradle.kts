import org.jetbrains.changelog.Changelog.OutputType
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.24"
    // Gradle Changelog Plugin
    id("org.jetbrains.changelog") version "2.2.0"
    id("org.jetbrains.intellij") version "1.17.1"
    id("org.jlleitschuh.gradle.ktlint") version "11.6.0"
}
group = "io.autofill.kotlin"
version = "2.0.0"

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    pluginName.set(getProperty("pluginName"))
    type.set(getProperty("platformType"))
    version.set("2024.1")
    plugins.set(getProperties("platformPlugins"))
}

changelog {
    version.set(project.version.toString())
    path.set(projectDir.resolve("CHANGELOG.md").path)
}

ktlint {
    version.set("1.2.1")
    android.set(false)
    outputToConsole.set(true)
    ignoreFailures.set(false)
    reporters {
        reporter(ReporterType.PLAIN)
        reporter(ReporterType.CHECKSTYLE)
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin {
    jvmToolchain(17)
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
        sinceBuild.set("241")
        untilBuild.set("252.*")

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
                        false,
                    ).generateHtml()
                }
            },
        )

        changeNotes.set(
            changelog.run {
                getOrNull(project.version.toString())?.let { changelog.renderItem(it, OutputType.MARKDOWN) }
                    ?: changelog.renderItem(getLatest(), OutputType.MARKDOWN)
            },
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

fun getProperty(name: String): String =
    project.findProperty(name)?.toString()
        ?: error("Property '$name' not found in gradle.properties")

fun getProperties(name: String) = getProperty(name).split(",").map { it.trim() }
