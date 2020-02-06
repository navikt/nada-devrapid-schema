plugins {
    kotlin("jvm").version("1.3.61")
    kotlin("kapt").version("1.3.61")
    kotlin("plugin.serialization").version("1.3.61")
    `java-library`
    `maven-publish`
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
}

