import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}


// Release version is supplied by PE-010; no source edits are required to ship.
val appVersion = providers.gradleProperty("appVersion").orElse("0.1.0").get()
val buildNumber = providers.gradleProperty("buildNumber").orElse("1").get()
    .toIntOrNull() ?: error("buildNumber tem de ser um número inteiro.")
require(Regex("""(0|[1-9][0-9]*)[.](0|[1-9][0-9]*)[.](0|[1-9][0-9]*)""").matches(appVersion)) {
    "appVersion deve ter o formato MAJOR.MINOR.PATCH, sem zeros à esquerda."
}
require(buildNumber in 1..2_100_000_000) {
    "buildNumber deve estar entre 1 e 2100000000."
}

// Match the VCCAprender host's existing signing configuration.
// Never put passwords, PKCS12/JKS files or keys in this repository.
val signingProperties = Properties().apply {
    val credentials = rootProject.file("keystore.properties")
    if (credentials.isFile) credentials.reader(Charsets.UTF_8).use { load(it) }
}

fun releaseSetting(environmentVariable: String, property: String): String? =
    providers.environmentVariable(environmentVariable).orNull?.takeIf { it.isNotBlank() }
        ?: signingProperties.getProperty(property)?.takeIf { it.isNotBlank() }

val releaseStoreFile = rootProject.file(
    releaseSetting("VCC_RELEASE_STORE_FILE", "storeFile")
        ?: "${System.getProperty("user.home")}/.keystores/VCCProjects.jks"
)
val releaseStorePassword = releaseSetting("VCC_RELEASE_STORE_PASSWORD", "storePassword")
val releaseKeyAlias = releaseSetting("VCC_RELEASE_KEY_ALIAS", "keyAlias") ?: "vccprojects"
val releaseKeyPassword = releaseSetting("VCC_RELEASE_KEY_PASSWORD", "keyPassword")
    ?: releaseStorePassword

// A debug build uses Android's usual ~/.android/debug.keystore by default.
// Both host and plugin match when built with the same default debug keystore.
// To sign the plugin debug APK with a custom shared certificate, set ALL of
// VCC_DEBUG_STORE_FILE, VCC_DEBUG_STORE_PASSWORD and VCC_DEBUG_KEY_ALIAS;
// VCC_DEBUG_KEY_PASSWORD may be omitted if identical to the store password.
val debugStorePath = providers.environmentVariable("VCC_DEBUG_STORE_FILE").orNull
    ?.takeIf { it.isNotBlank() }
val debugStorePassword = providers.environmentVariable("VCC_DEBUG_STORE_PASSWORD").orNull
    ?.takeIf { it.isNotBlank() }
val debugKeyAlias = providers.environmentVariable("VCC_DEBUG_KEY_ALIAS").orNull
    ?.takeIf { it.isNotBlank() }
val debugKeyPassword = providers.environmentVariable("VCC_DEBUG_KEY_PASSWORD").orNull
    ?.takeIf { it.isNotBlank() } ?: debugStorePassword

val customDebugSigning = listOf(
    debugStorePath, debugStorePassword, debugKeyAlias,
    providers.environmentVariable("VCC_DEBUG_KEY_PASSWORD").orNull?.takeIf { it.isNotBlank() }
).any { it != null }

if (customDebugSigning) {
    require(debugStorePath != null && debugStorePassword != null && debugKeyAlias != null) {
        "Configura VCC_DEBUG_STORE_FILE, VCC_DEBUG_STORE_PASSWORD e VCC_DEBUG_KEY_ALIAS em conjunto."
    }
    require(rootProject.file(debugStorePath).isFile) {
        "Keystore Debug personalizada não encontrada. Verifica VCC_DEBUG_STORE_FILE."
    }
}

android {
    namespace = "com.vcc.aprender.playenglish"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.vcc.aprender.playenglish"
        minSdk = 26
        targetSdk = 35
        versionCode = buildNumber
        versionName = appVersion
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }

    signingConfigs {
        create("release") {
            storeFile = releaseStoreFile
            storeType = "PKCS12"
            storePassword = releaseStorePassword
            keyAlias = releaseKeyAlias
            keyPassword = releaseKeyPassword
        }
        if (customDebugSigning) {
            getByName("debug") {
                storeFile = rootProject.file(debugStorePath!!)
                storePassword = debugStorePassword
                keyAlias = debugKeyAlias
                keyPassword = debugKeyPassword
            }
        }
    }
    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}

// Release signing must fail closed: no unsigned fallback when the key is absent.
tasks.matching { it.name == "preReleaseBuild" }.configureEach {
    doFirst {
        require(releaseStoreFile.isFile) {
            "Keystore Release não encontrada. Define VCC_RELEASE_STORE_FILE ou keystore.properties."
        }
        require(!releaseStorePassword.isNullOrBlank() &&
                !releaseKeyPassword.isNullOrBlank() &&
                releaseKeyAlias.isNotBlank()) {
            "Credenciais Release em falta: configura VCC_RELEASE_* ou keystore.properties."
        }
    }
}


dependencies {
    implementation(project(":game-plugin-sdk"))
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.navigation:navigation-compose:2.8.5")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.12.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
}
