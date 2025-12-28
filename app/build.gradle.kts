import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.kgapp.kccjapi"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.kgapp.kccjapi"
        minSdk = 26
        targetSdk = 36

        versionName = "v1.0.2"
        versionCode = 102

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            // 读取签名配置（从 local.properties）
            val keystorePropertiesFile = rootProject.file("local.properties")
            if (keystorePropertiesFile.exists()) {
                val props = Properties()
                keystorePropertiesFile.reader().use {
                    props.load(it)
                }
                
                val keystorePath = props.getProperty("keystore.path")
                val keystorePassword = props.getProperty("keystore.password")
                val keyAliasName = props.getProperty("key.alias")
                val keyPassword = props.getProperty("key.password")
                
                if (!keystorePath.isNullOrBlank() && !keystorePassword.isNullOrBlank() 
                    && !keyAliasName.isNullOrBlank() && !keyPassword.isNullOrBlank()) {
                    storeFile = file(keystorePath)
                    storePassword = keystorePassword
                    keyAlias = keyAliasName
                    this.keyPassword = keyPassword
                }
            }
        }
    }





    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
            // 只有在签名配置存在时才使用
            if (signingConfigs.findByName("release")?.storeFile?.exists() == true) {
                signingConfig = signingConfigs.getByName("release")
            }
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
        compose = true
        aidl = true
    }
    }

dependencies {
    // Compose (如果你已经有 BOM/Material3 就不用重复)
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.8.4")

    // Lifecycle / ViewModel Compose
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // Retrofit + OkHttp + Gson
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    implementation("com.google.android.material:material:1.12.0")

    //toast
    implementation ("com.github.Spikeysanju:MotionToast:1.4")
    
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("androidx.activity:activity-compose:1.9.3")

}