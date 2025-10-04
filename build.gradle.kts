plugins {
    id("com.android.application") version "8.11.2" apply false
    id("com.android.library") version "8.11.2" apply false
    id("org.jetbrains.kotlin.android") version "2.2.20" apply false
}

tasks.register<Delete>("clean") {
    delete(project.layout.buildDirectory)
}

