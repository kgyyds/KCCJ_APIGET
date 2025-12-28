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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    implementation("androidx.compose.material:material-icons-extended")

    // Material3 核心库（Kotlin DSL 必须用双引号）
    implementation("com.google.android.material:material:1.11.0")


    //toast
    implementation ("com.github.Spikeysanju:MotionToast:1.4")
}