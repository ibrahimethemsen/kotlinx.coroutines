/*
 * Copyright 2016-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

// Platform-specific configuration to compile JVM modules

import org.gradle.api.*
import org.jetbrains.kotlin.gradle.dsl.*

plugins {
    kotlin("jvm")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_1_8
    }
    jvmToolchain(jdkToolchainVersion)
}

dependencies {
    testImplementation(kotlin("test"))
    // Workaround to make addSuppressed work in tests
    testImplementation(kotlin("reflect"))
    testImplementation(kotlin("stdlib-jdk7"))
    testImplementation(kotlin("test-junit"))
    testImplementation("junit:junit:${version("junit")}")
}

tasks.withType<Test> {
    testLogging {
        showStandardStreams = true
        events("passed", "failed")
    }
    val stressTest = project.properties["stressTest"]
    if (stressTest != null) systemProperties["stressTest"] = stressTest
}
