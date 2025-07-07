plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "com.apps.markdown.sample"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.apps.markdown.sample"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        manifestPlaceholders["deeplink_scheme"] = "markwon"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }

        debug {
            buildConfigField("String", "GIT_REPOSITORY", "\"https://github.com/noties/Markwon\"")
            buildConfigField("String", "DEEPLINK_SCHEME", "\"markwon\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

configurations.all {
    exclude(
        group = "org.jetbrains",
        module = "annotations-java5"
    )
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    implementation(libs.coil)
    implementation(libs.glide)
    implementation(libs.commonmark)
    implementation(libs.commonmark.strikethrough)

    implementation(project(":markwon-core"))
    implementation(project(":markwon-editor"))
    implementation(project(":markwon-ext-latex"))
    implementation(project(":markwon-inline-parser"))
    implementation(project(":markwon-test-span"))
    implementation(project(":markwon-syntax-highlight"))
    implementation(project(":markwon-ext-strikethrough"))
    implementation(project(":markwon-html"))
    implementation(project(":markwon-ext-tables"))
    implementation(project(":markwon-ext-tasklist"))
    implementation(project(":markwon-image"))
    implementation(project(":markwon-image-coil"))
    implementation(project(":markwon-image-glide"))
    implementation(project(":markwon-linkify"))
    implementation(project(":markwon-recycler"))
    implementation(project(":markwon-recycler-table"))
    implementation(project(":markwon-simple-ext"))

    implementation("com.google.code.gson:gson:2.13.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    implementation("io.noties:prism4j:2.0.0")
    implementation("io.noties:adapt:2.2.0")
    implementation("io.noties:debug:5.1.0")

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}