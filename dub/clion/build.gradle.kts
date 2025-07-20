import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java")
    alias(libs.plugins.kotlin)
    alias(libs.plugins.gradleIntelliJModule)
    alias(libs.plugins.kover)
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation (project(":dub"))
    implementation (project(":utils"))
    testImplementation (libs.junit.engine)
    testRuntimeOnly (libs.junit.engine)

    intellijPlatform {
        clion(providers.gradleProperty("ideaVersion").get())
        bundledPlugin(
            "com.intellij.clion"
        )
        plugin("com.intellij.nativeDebug:${providers.gradleProperty("nativeDebugVersion").get()}")
        testFramework(TestFrameworkType.Platform)
    }
}
