plugins {
    kotlin("multiplatform") version "1.8.22" apply false
    kotlin("plugin.serialization") version "1.8.22" apply false
}

subprojects {
    repositories {
        google()
        mavenCentral()
    }
}