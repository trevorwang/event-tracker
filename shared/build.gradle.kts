import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    iosArm64()
    iosSimulatorArm64()
    
    jvm()
    
    js {
        browser()
    }
    
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }
    
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serializationJson)
            implementation(libs.kotlinx.coroutinesCore)
            implementation(libs.ktor.clientCore)
            implementation(libs.ktor.clientWebsockets)
            implementation(libs.ktor.clientContentNegotiation)
            implementation(libs.ktor.serializationKotlinxJsonClient)
        }
        androidMain.dependencies {
            implementation(libs.ktor.clientAndroid)
        }
        iosMain.dependencies {
            implementation(libs.ktor.clientDarwin)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.clientCio)
        }
        jsMain.dependencies {
            implementation(libs.ktor.clientJs)
        }
        wasmJsMain.dependencies {
            implementation(libs.ktor.clientWasm)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "mingsin.event.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
