plugins {
    kotlin("multiplatform") version "1.8.21" apply false
    kotlin("plugin.serialization") version "1.8.21" apply false
}

subprojects {
    repositories {
        google()
        mavenCentral()
    }
}