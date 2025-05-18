plugins {
    id("com.android.application") version "8.10.0" apply false
    id("com.android.library") version "8.10.0" apply false
    id("org.jetbrains.kotlin.android") version "2.1.21" apply false
}

extra["versions"] = mapOf(
    "kotlin" to "2.1.21",
    "annotation" to "1.9.1",
    "appCompat" to "1.7.0",
    "constraintLayout" to "2.2.1",
    "material" to "1.12.0",
    "room" to "2.7.1",
    "navigation" to "2.9.0",
    "recyclerview" to "1.4.0",
    "recyclerviewSelection" to "1.1.0",
    "preference" to "1.2.1",
    "gridLayout" to "1.1.0",
    "lifecycle" to "2.9.0",
    "databindingRuntime" to "8.1.1"
)

tasks.register<Delete>("clean") {
    delete(project.layout.buildDirectory)
}

