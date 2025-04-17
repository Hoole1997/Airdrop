plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.web3.airdrop"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.web3.airdrop"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/INDEX.LIST"
            // 你可以根据需要排除其他文件
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE"
            excludes += "/META-INF/NOTICE"
            excludes += "/META-INF/DISCLAIMER"
            excludes += "META-INF/FastDoubleParser-LICENSE"
            excludes += "META-INF/FastDoubleParser-NOTICE"
            excludes += "META-INF/io.netty.versions.properties"
            excludes += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        dataBinding = true
//        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    val room_version = "2.7.0"
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    ksp("androidx.room:room-compiler:$room_version")

//    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.1.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.github.liangjingkanji:Net:3.7.0")
    implementation("com.github.ihsanbal:LoggingInterceptor:3.1.0") {
        exclude(group = "org.json", module = "json")
    }
    implementation("io.github.cymchad:BaseRecyclerViewAdapterHelper4:4.1.4")
    implementation("com.blankj:utilcodex:1.31.1")
    implementation("com.makeramen:roundedimageview:2.3.0")
    implementation("com.google.code.gson:gson:2.12.1")
    implementation("net.sourceforge.jexcelapi:jxl:2.6.12")
    implementation("org.web3j:core:4.13.0")
    implementation ("org.web3j:core:4.12.3-android")
    implementation("io.github.scwang90:refresh-layout-kernel:3.0.0-alpha")
    implementation("io.github.scwang90:refresh-header-classics:3.0.0-alpha")
    implementation("io.github.scwang90:refresh-header-material:3.0.0-alpha")
    implementation("io.github.scwang90:refresh-footer-classics:3.0.0-alpha")
}