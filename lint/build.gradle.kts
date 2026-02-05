plugins {
    id("java-library")
    kotlin("jvm")
    id("com.android.lint")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

lint {
    htmlReport = true
    htmlOutput = file("lint-report.html")
    textReport = true
    absolutePaths = false
    ignoreTestSources = true
}

dependencies {
    val versions: Map<Any,Any> by rootProject.ext

    // For a description of the below dependencies, see the main project README
    compileOnly("com.android.tools.lint:lint-api:${versions["lint"]}")
}