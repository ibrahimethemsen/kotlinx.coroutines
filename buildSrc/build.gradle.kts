/*
 * Copyright 2016-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

import java.util.*

plugins {
    `kotlin-dsl`
}

val cacheRedirectorEnabled = System.getenv("CACHE_REDIRECTOR")?.toBoolean() == true
val buildSnapshotTrain = properties["build_snapshot_train"]?.toString()?.toBoolean() == true
val kotlinDevUrl = project.rootProject.properties["kotlin_repo_url"] as? String

repositories {
    mavenCentral()
    if (cacheRedirectorEnabled) {
        maven("https://cache-redirector.jetbrains.com/plugins.gradle.org/m2")
    } else {
        maven("https://plugins.gradle.org/m2")
    }
    if (!kotlinDevUrl.isNullOrEmpty()) {
        maven(kotlinDevUrl)
    }
    if (buildSnapshotTrain) {
        mavenLocal()
    }
}

val gradleProperties = Properties().apply {
    file("../gradle.properties").inputStream().use { load(it) }
}

fun version(target: String): String {
    // Intercept reading from properties file
    if (target == "kotlin") {
        val snapshotVersion = properties["kotlin_snapshot_version"]
        if (snapshotVersion != null) return snapshotVersion.toString()
    }
    val version = "${target}_version"
    // Read from CLI first, used in aggregate builds
    return properties[version]?.let{"$it"} ?: gradleProperties.getProperty(version)
}

dependencies {
    implementation(kotlin("gradle-plugin", version("kotlin")))
    /*
     * Dokka is compiled with language level = 1.4, but depends on Kotlin 1.6.0, while
     * our version of Gradle bundles Kotlin 1.4.x and can read metadata only up to 1.5.x,
     * thus we're excluding stdlib compiled with 1.6.0 from dependencies.
     */
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:${version("dokka")}") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk7")
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
    }
    implementation("org.jetbrains.dokka:dokka-core:${version("dokka")}") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk7")
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
    }
    implementation("ru.vyarus:gradle-animalsniffer-plugin:${version("animalsniffer")}") // Android API check
    implementation("org.jetbrains.kotlinx:kover-gradle-plugin:${version("kover")}") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk7")
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
    }

    implementation("org.jetbrains.kotlinx:kotlinx-knit:${version("knit")}")
}
