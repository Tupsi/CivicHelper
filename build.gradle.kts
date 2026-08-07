plugins {
    id("com.android.application") version "9.3.1" apply false
    id("com.android.library") version "9.3.1" apply false
    id("org.jetbrains.kotlin.android") version "2.4.10" apply false
    id("androidx.navigation.safeargs.kotlin") version "2.9.8" apply false
}

tasks.register<Delete>("clean") {
    delete(project.layout.buildDirectory)
}
