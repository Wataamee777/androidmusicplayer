import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "me.wataame.player"
    compileSdk = 35

    // keystore.properties（CI等で生成されるファイル）の読み込み処理
    val keystorePropertiesFile = rootProject.file("app/keystore.properties")
    val keystoreProperties = Properties()
    if (keystorePropertiesFile.exists()) {
        keystoreProperties.load(FileInputStream(keystorePropertiesFile))
    }

    defaultConfig {
        applicationId = "me.wataame.player"
        minSdk = 26
        targetSdk = 35 // Android 14以降のメディア動作安定化のため35に推奨変更
        versionCode = 3
        versionName = "1.2"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            if (!keystoreProperties.isEmpty) {
                // keystore.properties が存在する場合はリリース署名を適用
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
                
                // Android 11以降の端末で必須となる署名スキームを明示
                isV2SigningEnabled = true
                isV3SigningEnabled = true
            } else {
                // ローカル環境などファイルがない場合は、ビルドエラーを防ぐためデバッグ署名で代用
                signingConfig = signingConfigs.getByName("debug")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            
            // リリースビルドに上記の署名設定を紐付け
            signingConfig = signingConfigs.getByName("release")
            
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions { 
        jvmTarget = "21" 
    }

    buildFeatures { 
        compose = true 
    }

    applicationVariants.all {
        outputs.all {
            val output = this as? com.android.build.gradle.internal.api.BaseVariantOutputImpl
            val fileName = "Android-Music-Player-v${versionName}-${buildType.name}.apk"
            output?.outputFileName = fileName
        }
    }
}

kapt { 
    correctErrorTypes = true 
}

val media3Version = "1.6.1"
val roomVersion = "2.7.1"

dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.05.01"))
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")

    implementation("androidx.media3:media3-exoplayer:$media3Version")
    implementation("androidx.media3:media3-session:$media3Version")
    implementation("androidx.media3:media3-ui:$media3Version")

    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")

    implementation("com.google.dagger:hilt-android:2.56.2")
    kapt("com.google.dagger:hilt-android-compiler:2.56.2")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-guava:1.10.2")
    implementation("com.google.guava:guava:33.4.8-android")
}
