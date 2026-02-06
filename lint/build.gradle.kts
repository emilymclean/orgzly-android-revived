plugins {
    id("java-library")
    kotlin("jvm")
    id("com.android.lint")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

lint {
    htmlReport = true
    textReport = true
    absolutePaths = false
    ignoreTestSources = true
}

dependencies {
    val versions: Map<Any,Any> by rootProject.ext

    // For a description of the below dependencies, see the main project README
    compileOnly("com.android.tools.lint:lint-api:${versions["lint"]}")
    compileOnly("com.android.tools.lint:lint-checks:${versions["lint"]}")

    testImplementation("junit:junit:${versions["junit"]}")
    testImplementation("com.android.tools.lint:lint:${versions["lint"]}")
    testImplementation("com.android.tools.lint:lint-tests:${versions["lint"]}")
    testImplementation("com.android.tools:testutils:${versions["lint"]}")
}

tasks.jar {
    manifest {
        attributes["Lint-Registry-v2"] = "com.orgzly.lint.OrgzlyIssueRegistry"
    }
}