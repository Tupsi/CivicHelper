val roomVersion: String = "2.7.2"
val lifecycleVersion: String = "2.9.2"
val navigationVersion: String = "2.9.2"

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    //  https://github.com/google/ksp/releases
    id("com.google.devtools.ksp") version "2.2.0-2.0.2"
    id("androidx.room") version "2.7.2"
}

android {
    compileSdk = 36

    defaultConfig {
        applicationId = "org.tesira.civic"
        minSdk = 28
        //noinspection OldTargetApi
        targetSdk = 35
        versionCode = 58
        versionName = "1.11.01"
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

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
//            freeCompilerArgs.addAll(
//                "-Xjvm-default=all",
//                "-Xjsr305=strict"
//            )
        }
    }
}



dependencies {
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.recyclerview:recyclerview-selection:1.2.0")
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

    // https://maven.google.com/web/index.html#com.google.android.material:material
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.activity:activity-ktx:1.10.1")

    implementation("it.xabaras.android:recyclerview-swipedecorator:1.4")
}

room {
    schemaDirectory("$projectDir/schemas")
}