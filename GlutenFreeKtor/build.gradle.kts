plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.serialization") version "2.0.21"
    application
}

group = "com.glufree.ktor"
version = "1.0.0"

application {
    mainClass.set("com.glufree.ktor.ApplicationKt")
}

repositories {
    mavenCentral()
}

dependencies {
    // Ktor Server Core & Netty Engine
    implementation("io.ktor:ktor-server-core-jvm:3.0.3")
    implementation("io.ktor:ktor-server-netty-jvm:3.0.3")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:3.0.3")
    implementation("io.ktor:ktor-server-cors-jvm:3.0.3")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:3.0.3")

    // Koin Dependency Injection
    implementation("io.insert-koin:koin-ktor:4.0.0")
    implementation("io.insert-koin:koin-logger-slf4j:4.0.0")

    // Exposed ORM & Connection Pooling
    implementation("org.jetbrains.exposed:exposed-core:0.57.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.57.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.57.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.57.0")
    implementation("com.zaxxer:HikariCP:6.2.1")
    implementation("org.postgresql:postgresql:42.7.4")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.5.16")

    // Test dependencies
    testImplementation("io.ktor:ktor-server-test-host-jvm:3.0.3")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:2.0.21")
    testImplementation("com.h2database:h2:2.3.232")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
