plugins {
    id("com.android.application") version "8.13.0" apply false
    id("com.android.library") version "8.13.0" apply false
    id("org.jetbrains.kotlin.android") version "2.2.20" apply false
    id("androidx.navigation.safeargs.kotlin") version "2.9.5" apply false
}

tasks.register<Delete>("clean") {
    delete(project.layout.buildDirectory)
}
