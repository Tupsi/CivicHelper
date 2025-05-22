plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = 35

    defaultConfig {
        applicationId = "org.tesira.civic"
        minSdk = 28
        targetSdk = 35
        versionCode = 7
        versionName = "1.1.5"
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

    buildFeatures {
        viewBinding = true
        dataBinding = true
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
    }
}

dependencies {
    val versions: Map<String, String> by rootProject.extra

    implementation("org.jetbrains.kotlin:kotlin-stdlib:${versions["kotlin"]}")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")

    implementation("androidx.recyclerview:recyclerview:${versions["recyclerview"]}")
    implementation("androidx.recyclerview:recyclerview-selection:${versions["recyclerviewSelection"]}")

    implementation("androidx.databinding:databinding-runtime:${versions["databindingRuntime"]}")

    implementation("androidx.preference:preference-ktx:${versions["preference"]}")
    implementation("androidx.gridlayout:gridlayout:${versions["gridLayout"]}")

    implementation("androidx.appcompat:appcompat:${versions["appCompat"]}")

    implementation("androidx.navigation:navigation-fragment-ktx:${versions["navigation"]}")
    implementation("androidx.navigation:navigation-ui-ktx:${versions["navigation"]}")

    implementation("androidx.room:room-runtime:${versions["room"]}")
    annotationProcessor("androidx.room:room-compiler:${versions["room"]}")

    implementation("androidx.annotation:annotation:${versions["annotation"]}")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:${versions["lifecycle"]}")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:${versions["lifecycle"]}")

    implementation("androidx.constraintlayout:constraintlayout:${versions["constraintLayout"]}")
    implementation("com.google.android.material:material:${versions["material"]}")
}
