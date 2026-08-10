
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(ktorLibs.plugins.ktor)
    kotlin("plugin.serialization") version "2.4.0"
    id("org.flywaydb.flyway") version "13.0.0"
}

group = "com.blackneko"
version = "1.0.0-SNAPSHOT"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

val ktorVersion = "3.5.1"
val exposedVersion = "1.3.1"
val koinVersion = "4.2.1"
val flywayVersion = "13.0.0"
val postgresVersion = "42.7.7"
val hikariVersion = "7.0.2"
val jwtVersion = "4.5.0"
val bcryptVersion = "0.4"
val logbackVersion = "1.5.18"


kotlin {
    jvmToolchain(21)
}
dependencies {
    // --------------------------------------------------
    // KTOR
    // --------------------------------------------------

    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")

    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")

    implementation("io.ktor:ktor-server-config-yaml:$ktorVersion")

    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")

    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")

    implementation("io.ktor:ktor-server-call-logging-jvm:$ktorVersion")

    implementation("io.ktor:ktor-server-status-pages-jvm:$ktorVersion")

    implementation("io.ktor:ktor-server-cors-jvm:$ktorVersion")

    implementation("io.ktor:ktor-server-default-headers-jvm:$ktorVersion")

    // Authentication - lo utilizaremos en la siguiente fase
    implementation("io.ktor:ktor-server-auth-jvm:$ktorVersion")

    implementation("io.ktor:ktor-server-auth-jwt-jvm:$ktorVersion")

    // WebSockets - preparado para la fase posterior
    implementation("io.ktor:ktor-server-websockets-jvm:$ktorVersion")

    // OpenAPI
    implementation("io.ktor:ktor-server-openapi-jvm:$ktorVersion")

    implementation("io.ktor:ktor-server-routing-openapi-jvm:$ktorVersion")

    // Swagger UI
    implementation("io.ktor:ktor-server-swagger-jvm:$ktorVersion")


    // --------------------------------------------------
    // KOIN
    // --------------------------------------------------

    implementation(platform("io.insert-koin:koin-bom:$koinVersion"))

    implementation("io.insert-koin:koin-core")

    implementation("io.insert-koin:koin-ktor")

    implementation("io.insert-koin:koin-logger-slf4j")


    // --------------------------------------------------
    // EXPOSED
    // --------------------------------------------------

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")

    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")

    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")


    // --------------------------------------------------
    // DATABASE
    // --------------------------------------------------

    implementation("org.postgresql:postgresql:$postgresVersion")

    implementation("com.zaxxer:HikariCP:$hikariVersion")


    // --------------------------------------------------
    // FLYWAY
    // --------------------------------------------------

    implementation("org.flywaydb:flyway-core:$flywayVersion")

    implementation("org.flywaydb:flyway-database-postgresql:$flywayVersion")


    // --------------------------------------------------
    // JWT
    // --------------------------------------------------

    implementation("com.auth0:java-jwt:$jwtVersion")


    // --------------------------------------------------
    // PASSWORD HASHING
    // --------------------------------------------------

    implementation("org.mindrot:jbcrypt:$bcryptVersion")


    // --------------------------------------------------
    // LOGGING
    // --------------------------------------------------

    implementation("ch.qos.logback:logback-classic:$logbackVersion")


    // --------------------------------------------------
    // TESTING
    // --------------------------------------------------

    testImplementation("io.ktor:ktor-server-test-host-jvm:$ktorVersion")

    testImplementation("org.jetbrains.kotlin:kotlin-test")

    testImplementation("org.junit.jupiter:junit-jupiter:6.0.0")


    implementation(libs.logback.classic)

    testImplementation(kotlin("test"))
    testImplementation(ktorLibs.server.testHost)
}
tasks.test {
    useJUnitPlatform()
}