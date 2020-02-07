plugins {
    kotlin("jvm").version("1.3.61")
    kotlin("kapt").version("1.3.61")
    kotlin("plugin.serialization").version("1.3.61")
    `java-library`
    `maven-publish`
    id("net.researchgate.release").version("2.6.0")
}

repositories {
    jcenter()
    mavenCentral()
    mavenLocal()
    maven(url = "https://jitpack.io")
}
group = "no.nav.nada"
dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("com.sksamuel.avro4k:avro4k-core:0.20.0")
    implementation("com.github.guepardoapps:kulid:1.1.2.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.5.2")
    testImplementation("org.assertj:assertj-core:3.15.0")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["kotlin"])
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
