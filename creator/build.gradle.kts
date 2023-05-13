import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

val kotlinSerializationVersion:String by project
val benasherUuidVersion:String by project
val kotlinDateTimeVersion:String by project

val ktorVersion:String by project
val kotlinWrapperVersion:String by project

val vertxVersion:String by project
val jacksonVersion:String by project
val kotlinCoroutinesVersion:String by project
val nettyIoUringVersion:String by project
val kotlinLogginVersion:String by project
val log4j2Version:String by project

val postgresJdbcVersion:String by project
val scramClientVersion:String by project
val liquibaseVersion:String by project


val kotestVersion:String by project
val mockkVersion:String by project
val testcontainersVersion:String by project
val postgresVersion:String by project


fun vertx(module: String): String = "io.vertx:vertx-$module"

kotlin {
    js(IR) {
        browser {
            testTask {
                testLogging.showStandardStreams = true
                useKarma {
                    useChromeHeadless()
                    useFirefox()
                }
            }
        }
        binaries.executable()
    }
    jvm {
        withJava()
        compilations.all {
            compilerOptions.configure {
                jvmTarget.set(JvmTarget.JVM_11)
                freeCompilerArgs.set(freeCompilerArgs.get() + "-opt-in=kotlin.contracts.ExperimentalContracts")
            }
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(enforcedPlatform("org.jetbrains.kotlinx:kotlinx-serialization-bom:$kotlinSerializationVersion"))

                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinDateTimeVersion")
                implementation("com.benasher44:uuid:$benasherUuidVersion")
            }
        }
        val commonTest by getting {
            dependencies {
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(enforcedPlatform("org.jetbrains.kotlin-wrappers:kotlin-wrappers-bom:$kotlinWrapperVersion"))
                implementation(enforcedPlatform("org.jetbrains.kotlinx:kotlinx-serialization-bom:$kotlinSerializationVersion"))

                implementation("org.jetbrains.kotlin-wrappers:kotlin-react")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-router-dom")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-emotion")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-mui")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-mui-icons")

                implementation("io.github.microutils:kotlin-logging-js:$kotlinLogginVersion")

                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
                implementation("io.ktor:ktor-client-js:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json-js:$ktorVersion")
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(enforcedPlatform("io.vertx:vertx-dependencies:$vertxVersion"))
                implementation(enforcedPlatform("com.fasterxml.jackson:jackson-bom:$jacksonVersion"))
                implementation(enforcedPlatform("org.apache.logging.log4j:log4j-bom:$log4j2Version"))
                implementation(enforcedPlatform("org.jetbrains.kotlinx:kotlinx-serialization-bom:$kotlinSerializationVersion"))

                implementation(vertx("core"))
                implementation(vertx("web"))
                implementation(vertx("lang-kotlin"))
                implementation(vertx("lang-kotlin-coroutines"))
                implementation(vertx("health-check"))
                implementation(vertx("config"))
                implementation(vertx("config-yaml"))
                implementation(vertx("io_uring-incubator"))
                runtimeOnly("io.netty.incubator:netty-incubator-transport-native-io_uring:$nettyIoUringVersion:linux-x86_64")

                implementation(vertx("pg-client"))
                implementation(vertx("sql-client-templates"))
                implementation("org.postgresql:postgresql:$postgresJdbcVersion")
                implementation("com.ongres.scram:client:$scramClientVersion")
                implementation("org.liquibase:liquibase-core:$liquibaseVersion")

                implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
                implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
                implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
                implementation("com.fasterxml.jackson.module:jackson-module-blackbird")

                implementation("org.apache.logging.log4j:log4j-api")
                implementation("org.apache.logging.log4j:log4j-core")
                implementation("org.apache.logging.log4j:log4j-slf4j-impl")
                implementation("io.github.microutils:kotlin-logging:$kotlinLogginVersion")

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$kotlinCoroutinesVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:$kotlinCoroutinesVersion")

                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(enforcedPlatform("org.testcontainers:testcontainers-bom:$testcontainersVersion"))

                implementation(vertx("junit5"))
                implementation("io.mockk:mockk:$mockkVersion")
                implementation(vertx("web-client"))
                implementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
                implementation("org.testcontainers:postgresql")
            }
        }
    }
}

tasks {
    getByName("jvmTest", Test::class) {
        useJUnitPlatform()

        testLogging.exceptionFormat = TestExceptionFormat.FULL
        testLogging.showCauses = true
        testLogging.showExceptions = true
        testLogging.showStackTraces = true

        environment(
            "vertx.logger-delegate-factory-class-name" to "io.vertx.core.logging.SLF4JLogDelegateFactory",
            "POSTGRES_VERSION" to postgresVersion,
        )
    }
}