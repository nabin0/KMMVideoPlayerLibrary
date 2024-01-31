import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsCompose)
}

kotlin {
    androidTarget {
        publishLibraryVariants("release")
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    val xcf = XCFramework()
    listOf(
        iosX64(),
//        iosArm64(),
//        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "sharedVideoPlayer"
            xcf.add(this)
            isStatic = true
        }
    }


    sourceSets {
        commonMain.dependencies {
            api(compose.runtime)
//            implementation ("androidx.compose.runtime:runtime:1.6.0")
//            implementation(compose.foundation)
            api(compose.material)
            api(compose.materialIconsExtended)
//            implementation(compose.ui)
//            @OptIn(ExperimentalComposeLibrary::class)
//            implementation(compose.components.resources)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        androidMain.dependencies {
            api(libs.androidx.activity.compose)

            api (libs.androidx.media3.exoplayer)
            api (libs.androidx.media3.ui)
            api (libs.androidx.media3.common)
            api (libs.androidx.media3.exoplayer.hls)
            api(libs.androidx.media3.exoplayer.dash)
            api(libs.androidx.media3.datasource.cronet)
        }
    }

    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        compilations["main"].compilerOptions.options.freeCompilerArgs.add("-Xexport-kdoc")
    }
}

android {
    namespace = "com.github.nabin0.kmmvideoplayer"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}