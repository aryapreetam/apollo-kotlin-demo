# Local CI Pipeline Setup & Apollo Repository Optimization

This document provides detailed instructions for setting up local CI pipeline execution and optimizing Apollo Kotlin
dependencies for GitHub Actions deployment.

## 🎯 Overview

This project required a custom Apollo Kotlin build with subscription support (`5.0.0-alpha.local-SNAPSHOT`) that wasn't
available in public Maven repositories. The challenge was to make GitHub Actions CI work with these local artifacts
while keeping the repository size manageable.

## 📋 Problem Statement

1. **Custom Apollo Build**: Using `5.0.0-alpha.local-SNAPSHOT` with subscription support
2. **CI Failure**: GitHub Actions couldn't resolve custom Apollo dependencies
3. **Repository Size**: Initial approach created a 392MB repository
4. **Local Testing**: Need to test CI pipeline locally before pushing

## 🛠️ Solution Architecture

### Local Repository Strategy

Instead of using JAR files in `libs/`, we implemented a **local Maven repository** approach:

- Created `repo/` directory with Maven repository structure
- Updated `settings.gradle.kts` to prioritize local repository
- Optimized to include only essential artifacts

### Repository Configuration

```kotlin
// settings.gradle.kts
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
        // ... other repositories
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
        // ... other repositories
    }
}
```

## 🚀 Step-by-Step Implementation

### Step 1: Analyze Current Dependencies

1. **Check what Apollo dependencies you're using:**
   ```bash
   grep -r "apollo" --include="*.gradle.kts" .
   ```

2. **Review version catalog:**
   ```bash
   cat gradle/libs.versions.toml
   ```

3. **Identify your target platforms** (in our case: WasmJS for web deployment)

### Step 2: Create Local Repository Structure

1. **Create repository directory:**
   ```bash
   mkdir -p repo/com/apollographql/apollo
   ```

2. **Copy only essential artifacts from local Maven repository:**
   ```bash
   # Plugin (required for GraphQL code generation)
   cp -r ~/.m2/repository/com/apollographql/apollo/apollo-gradle-plugin/5.0.0-alpha.local-SNAPSHOT \
     repo/com/apollographql/apollo/

   # Runtime (core multiplatform library)
   cp -r ~/.m2/repository/com/apollographql/apollo/apollo-runtime/5.0.0-alpha.local-SNAPSHOT \
     repo/com/apollographql/apollo/

   # Platform-specific runtime for your target (WasmJS in our case)
   cp -r ~/.m2/repository/com/apollographql/apollo/apollo-runtime-wasm-js/5.0.0-alpha.local-SNAPSHOT \
     repo/com/apollographql/apollo/
   ```

### Step 3: Configure Repository Priority

1. **Update `settings.gradle.kts`** to use local repository first
2. **Use content filtering** to restrict local repo to Apollo artifacts only
3. **Ensure proper fallback** to other repositories for non-Apollo dependencies

### Step 4: Test Local CI Pipeline

1. **Run the exact CI command locally:**
   ```bash
   ./gradlew :composeApp:wasmJsBrowserDistribution
   ```

2. **Simulate deployment artifact creation:**
   ```bash
   mkdir -p github-pages
   cp -r composeApp/build/dist/wasmJs/productionExecutable/* github-pages/
   ```

3. **Verify all required files are generated:**
   ```bash
   ls -la github-pages/
   ```

### Step 5: Repository Optimization

**Initial size analysis:**

```bash
du -sh repo/  # Was 392MB initially
```

**Optimization strategy:**

- Remove unnecessary platform artifacts (android, ios, desktop, etc.)
- Keep only artifacts needed for your target platform
- Remove duplicate dependencies and transitive artifacts

**Final optimized size:**

```bash
du -sh repo/  # Reduced to 1.0MB (99% reduction!)
```

### Step 6: Git Configuration

1. **Update `.gitignore`** to include optimized repository:
   ```gitignore
   # Remove repo/ from gitignore to commit Apollo artifacts
   # repo/  <- Remove this line
   
   # Keep deployment artifacts excluded
   github-pages/
   ```

2. **Commit the optimized repository:**
   ```bash
   git add repo/ .gitignore
   git commit -m "Add optimized Apollo local repository for CI deployment"
   ```

## 📊 Results Achieved

### Size Optimization

- **Before**: 392MB (full Apollo Maven repository)
- **After**: 1.0MB (targeted essential artifacts)
- **Reduction**: 99% size decrease

### Performance Improvements

- **Build Time**: ~1-2 minutes for WasmJS production build
- **Bundle Size**: 548KB JavaScript + 10.8MB WebAssembly
- **Git Operations**: Dramatically faster due to smaller repository

### CI Compatibility

- ✅ **Local CI Simulation**: Identical to GitHub Actions workflow
- ✅ **GitHub Actions Ready**: Custom Apollo artifacts available to CI runners
- ✅ **Cross-platform**: Works on macOS, Linux (GitHub Actions), Windows

## 🔄 Local CI Execution Commands

### Full CI Simulation

```bash
# Clean build (equivalent to fresh CI environment)
./gradlew clean

# Run the exact CI command
./gradlew :composeApp:wasmJsBrowserDistribution

# Create deployment artifacts (equivalent to GitHub Actions deploy step)
mkdir -p github-pages
cp -r composeApp/build/dist/wasmJs/productionExecutable/* github-pages/

# Verify deployment artifacts
ls -la github-pages/
```

### Quick CI Test

```bash
# Quick build test (reuses cache)
./gradlew :composeApp:wasmJsBrowserDistribution
```

### Apollo-specific Tests

```bash
# Test GraphQL code generation
./gradlew :composeApp:generateApollo

# Test Apollo runtime compilation
./gradlew :composeApp:compileKotlinWasmJs
```

## 📁 Repository Structure

```
apollo-kotlin-demo/
├── repo/                                    # Local Apollo Maven repository
│   └── com/apollographql/apollo/
│       ├── apollo-gradle-plugin/
│       │   └── 5.0.0-alpha.local-SNAPSHOT/
│       ├── apollo-runtime/
│       │   └── 5.0.0-alpha.local-SNAPSHOT/
│       └── apollo-runtime-wasm-js/
│           └── 5.0.0-alpha.local-SNAPSHOT/
├── github-pages/                            # CI deployment artifacts (gitignored)
├── settings.gradle.kts                      # Repository configuration
├── .github/workflows/deploy-wasmjs.yml      # GitHub Actions workflow
└── scripts/test-github-actions.sh           # Local CI simulation script
```

## 🐛 Troubleshooting

### Common Issues

1. **"Plugin was not found" Error:**
   - **Root Cause**: Incorrect Maven repository structure - artifacts must be in separate directories
   - **Solution**: Ensure proper directory structure:
     ```
     repo/com/apollographql/apollo/
     ├── apollo-gradle-plugin/5.0.0-alpha.local-SNAPSHOT/
     ├── com.apollographql.apollo.gradle.plugin/5.0.0-alpha.local-SNAPSHOT/  # Plugin marker
     ├── apollo-runtime/5.0.0-alpha.local-SNAPSHOT/
     └── apollo-runtime-wasm-js/5.0.0-alpha.local-SNAPSHOT/
     ```
   - **Fix Command**:
     ```bash
     rm -rf repo/ && mkdir -p repo/com/apollographql/apollo
     mkdir -p repo/com/apollographql/apollo/apollo-gradle-plugin
     cp -r ~/.m2/repository/com/apollographql/apollo/apollo-gradle-plugin/5.0.0-alpha.local-SNAPSHOT repo/com/apollographql/apollo/apollo-gradle-plugin/
     mkdir -p "repo/com/apollographql/apollo/com.apollographql.apollo.gradle.plugin"
     cp -r ~/.m2/repository/com/apollographql/apollo/com.apollographql.apollo.gradle.plugin/5.0.0-alpha.local-SNAPSHOT "repo/com/apollographql/apollo/com.apollographql.apollo.gradle.plugin/"
     mkdir -p repo/com/apollographql/apollo/apollo-runtime
     cp -r ~/.m2/repository/com/apollographql/apollo/apollo-runtime/5.0.0-alpha.local-SNAPSHOT repo/com/apollographql/apollo/apollo-runtime/
     mkdir -p repo/com/apollographql/apollo/apollo-runtime-wasm-js
     cp -r ~/.m2/repository/com/apollographql/apollo/apollo-runtime-wasm-js/5.0.0-alpha.local-SNAPSHOT repo/com/apollographql/apollo/apollo-runtime-wasm-js/
     ```
   - Verify `repo/` directory is committed to Git
   - Check that Apollo version matches across all files

2. **Build Cache Issues:**
    - Ensure `repo/` directory is committed to Git
    - Verify `settings.gradle.kts` points to correct local repository
    - Check that Apollo version matches across all files

2. **Build Cache Issues:**
    - Run `./gradlew clean` to clear cache
    - Delete `.gradle/` folder if persistent issues
    - Use `--no-configuration-cache` flag for debugging

3. **Platform-specific Artifacts Missing:**
    - Copy platform-specific runtime for your target
    - For WasmJS: `apollo-runtime-wasm-js`
    - For Android: `apollo-runtime-android`
    - For iOS: `apollo-runtime-iosX64`, `apollo-runtime-iosArm64`

### Verification Steps

1. **Check repository configuration:**
   ```bash
   ./gradlew dependencies --configuration runtimeClasspath
   ```

2. **Verify Apollo artifacts are resolved locally:**
   ```bash
   ./gradlew :composeApp:dependencies | grep apollo
   ```

3. **Test GraphQL code generation:**
   ```bash
   ./gradlew :composeApp:generateApollo --info
   ```

## 🔮 Future Maintenance

### When Apollo Version Changes

1. **Update version in `gradle/libs.versions.toml`:**
   ```toml
   apollo = "NEW_VERSION"
   ```

2. **Update local repository:**
   ```bash
   rm -rf repo/
   mkdir -p repo/com/apollographql/apollo
   # Copy new version artifacts
   cp -r ~/.m2/repository/com/apollographql/apollo/apollo-gradle-plugin/NEW_VERSION repo/com/apollographql/apollo/
   # ... repeat for other artifacts
   ```

3. **Test and commit:**
   ```bash
   ./gradlew clean :composeApp:wasmJsBrowserDistribution
   git add repo/
   git commit -m "Update Apollo to NEW_VERSION"
   ```

### Adding New Target Platforms

1. **Identify required platform artifacts:**
   ```bash
   find ~/.m2/repository/com/apollographql/apollo -name "*NEW_PLATFORM*"
   ```

2. **Copy platform-specific artifacts:**
   ```bash
   cp -r ~/.m2/repository/com/apollographql/apollo/apollo-runtime-NEW_PLATFORM/VERSION repo/com/apollographql/apollo/
   ```

3. **Test platform-specific build:**
   ```bash
   ./gradlew :composeApp:NEW_PLATFORM_TARGET
   ```

## 📚 References

- **GitHub Actions Workflow**: `.github/workflows/deploy-wasmjs.yml`
- **Local CI Script**: `scripts/test-github-actions.sh`
- **Deployment Guide**: `DEPLOYMENT.md`
- **Apollo Kotlin Documentation**: https://apollographql.com/docs/kotlin
- **Gradle Repository Management**: https://docs.gradle.org/current/userguide/repository_types.html

---

**Created**: January 2025  
**Last Updated**: January 2025  
**Maintained By**: Apollo Kotlin Demo Team

This document ensures that future CI pipeline execution and Apollo dependency management can be performed efficiently
and consistently.