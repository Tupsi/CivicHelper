val roomVersion: String = "2.7.1"
val lifecycleVersion: String = "2.9.1"
val navigationVersion: String = "2.9.0"

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    //  https://github.com/google/ksp/releases
    id("com.google.devtools.ksp") version "2.1.21-2.0.1"
    id("androidx.room") version "2.7.1"
}

android {
    compileSdk = 35

    defaultConfig {
        applicationId = "org.tesira.civic"
        minSdk = 28
        targetSdk = 35
        versionCode = 44
        versionName = "1.7.01"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
//    tasks.withType<JavaCompile>().configureEach {
//        options.compilerArgs.add("-Xlint:deprecation")
//    }

    buildFeatures {
        viewBinding = true
    }

    namespace = "org.tesira.civic"

    lint {
        baseline = file("lint-baseline.xml")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-Xjvm-default=all",
            "-Xjsr305=strict"
        )
    }

}

dependencies {
//    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.1.21")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.recyclerview:recyclerview-selection:1.2.0")
//    implementation("androidx.databinding:databinding-runtime:8.10.1")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.gridlayout:gridlayout:1.1.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.annotation:annotation:1.9.1")

    implementation("androidx.navigation:navigation-fragment-ktx:$navigationVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navigationVersion")

    // mit plugin version oben gleichhalten!!!
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    implementation("androidx.lifecycle:lifecycle-common:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-core-ktx:$lifecycleVersion")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
//    implementation("androidx.lifecycle:lifecycle-common-java8:$lifecycleVersion")

    // https://maven.google.com/web/index.html#com.google.android.material:material
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.activity:activity-ktx:1.10.1")
//    implementation("androidx.paging:paging-runtime-ktx:3.3.6")
}

room {
    schemaDirectory("$projectDir/schemas")
}