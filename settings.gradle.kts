rootProject.name = "apollo-kotlin-demo"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
      // Local Apollo repository with custom artifacts
      maven {
        url = uri("$rootDir/repo")
        content {
          includeGroup("com.apollographql.apollo")
        }
      }
        mavenLocal()
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
  plugins {
    kotlin("jvm") version "1.9.22"
  }
}

dependencyResolutionManagement {
    repositories {
      // Local Apollo repository with custom artifacts
      maven {
        url = uri("$rootDir/repo")
        content {
          includeGroup("com.apollographql.apollo")
        }
      }
        mavenLocal()
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":composeApp")
include(":server")
include(":shared")