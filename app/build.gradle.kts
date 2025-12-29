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

        versionName = "v1.0.3"
        versionCode = 103

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // ===== Release 签名配置：CI优先（env + ./release.jks），本地fallback（local.properties）=====
    signingConfigs {
        create("release") {
            var configured = false

            // 1) CI: GitHub Actions 解码到项目根目录的 release.jks + 环境变量密码
            val envStorePwd = System.getenv("SIGNING_STORE_PASSWORD")
            val envAlias = System.getenv("SIGNING_KEY_ALIAS")
            val envKeyPwd = System.getenv("SIGNING_KEY_PASSWORD")
            val ciJks = rootProject.file("release.jks")

            if (!envStorePwd.isNullOrBlank()
                && !envAlias.isNullOrBlank()
                && !envKeyPwd.isNullOrBlank()
                && ciJks.exists()
            ) {
                storeFile = ciJks
                storePassword = envStorePwd
                keyAlias = envAlias
                keyPassword = envKeyPwd
                configured = true
            }

            // 2) 本地：local.properties（你原来的方式）
            if (!configured) {
                val propsFile = rootProject.file("local.properties")
                if (propsFile.exists()) {
                    val props = Properties().apply {
                        propsFile.reader().use { load(it) }
                    }

                    val keystorePath = props.getProperty("keystore.path")
                    val keystorePassword = props.getProperty("keystore.password")
                    val keyAliasName = props.getProperty("key.alias")
                    val keyPwd = props.getProperty("key.password")

                    if (!keystorePath.isNullOrBlank()
                        && !keystorePassword.isNullOrBlank()
                        && !keyAliasName.isNullOrBlank()
                        && !keyPwd.isNullOrBlank()
                    ) {
                        storeFile = file(keystorePath)
                        storePassword = keystorePassword
                        keyAlias = keyAliasName
                        keyPassword = keyPwd
                        configured = true
                    }
                }
            }

            // 3) 没配置到就直接留空，让 release 构建时报错（避免产出“假release/被debug签名”的APK）
            // 这样你能第一时间发现签名没喂进去，而不是装不上才怀疑人生
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // ✅ 重点：release 强制使用 release signingConfig（不要 fallback 到 debug）
            signingConfig = signingConfigs.getByName("release")
        }

        // debug 不动：默认使用 debug keystore
        debug {
            // 你可以留空
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
    // Compose
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

    // toast
    implementation("com.github.Spikeysanju:MotionToast:1.4")

    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("androidx.activity:activity-compose:1.9.3")
}