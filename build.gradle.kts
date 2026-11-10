plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android) apply false
    id("maven-publish")
}

android {
    namespace = "com.umirtech.playkit"
    compileSdk {
        version = release(37)
    }
    defaultConfig {
        minSdk = 25
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    api(libs.play.review)
    api(libs.play.app.update)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}

publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = "com.umirtech"
            artifactId = "playkit"
            version = "1.0.0"
            afterEvaluate { from(components["release"]) }
        }
    }
}
