// Shared QuickKart design system: brand colours, typography, responsive metrics, controls and
// logo. Used by the Customer (app), Merchant and Rider apps so all three look identical.
plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.compose)
}

android {
  namespace = "com.example.core.ui"
  compileSdk { version = release(36) { minorApiLevel = 1 } }
  defaultConfig { minSdk = 24 }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures { compose = true }
}

dependencies {
  api(platform(libs.androidx.compose.bom))
  api(libs.androidx.compose.material3)
  api(libs.androidx.compose.ui)
  api(libs.androidx.compose.ui.graphics)
  api(libs.androidx.compose.material.icons.core)
  api(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.core.ktx)
}
