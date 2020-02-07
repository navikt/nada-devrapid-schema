plugins {
    kotlin("jvm").version("1.3.61")
    kotlin("kapt").version("1.3.61")
    kotlin("plugin.serialization").version("1.3.61")
    `maven-publish`
    id("net.researchgate.release").version("2.6.0")
    id("com.github.breadmoirai.github-release").version("2.2.9")
}

repositories {
    jcenter()
    mavenCentral()
    mavenLocal()
    maven(url = "https://jitpack.io")
}

java {
    sourceCompatibility = org.gradle.api.JavaVersion.VERSION_11
    targetCompatibility = org.gradle.api.JavaVersion.VERSION_11
}
group = "no.nav.nada"
dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("com.sksamuel.avro4k:avro4k-core:0.20.0")
    implementation("com.github.guepardoapps:kulid:1.1.2.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.6.0")
    testImplementation("org.assertj:assertj-core:3.15.0")
}

publishing {
    publications {
        create<MavenPublication>("gpr") {
            components["kotlin"]
        }
    }
    repositories {
        maven {
            name = "GithubPackages"
            url = uri("https://maven.pkg.github.com/navikt/nada-devrapid-schema")
            credentials {
                username = "x-access-token"
                password = System.getProperty("gpr.password") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        showExceptions = true
        showStackTraces = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        events("passed", "skipped", "failed")
    }
}

githubRelease {
    setToken(System.getenv("GITHUB_TOKEN"))
    setOverwrite(true)
    setTargetCommitish("${project.version}")
    setTagName("${project.version}")
    setOwner("navikt")
}
