# Local CI Pipeline Setup & Apollo Repository Optimization

This document provides detailed instructions for setting up local CI pipeline execution and using Apollo Kotlin
with WASM support for GitHub Actions deployment.

## 🎯 Overview

This project uses a custom Apollo Kotlin build with WASM support (`5.0.0-alpha.local-SNAPSHOT`) that wasn't
available in public Maven repositories. The solution builds Apollo Kotlin from source in CI to ensure
the latest WASM-compatible version is always available.

## 📋 Problem Statement

1. **Custom Apollo Build**: Using `5.0.0-alpha.local-SNAPSHOT` with WASM support
2. **WASM Support**: Official Apollo releases don't support WASM target yet
3. **CI Compatibility**: Need to make GitHub Actions work with custom Apollo dependencies
4. **Repository Size**: Avoid committing large dependency artifacts

## 🛠️ Solution Architecture

### Build-from-Source Strategy

Instead of committing Apollo artifacts, we implemented a **build-from-source** approach:

- Clone Apollo Kotlin repository in CI
- Set custom version (`5.0.0-alpha.local-SNAPSHOT`)
- Build and publish to local Maven repository
- Use `mavenLocal()` priority in Gradle configuration

### Repository Configuration

```kotlin
// settings.gradle.kts
pluginManagement {
    repositories {
        mavenLocal()  // Prioritize locally built artifacts
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenLocal()  // Prioritize locally built artifacts
        google()
        mavenCentral()
    }
}
```

## 🚀 Step-by-Step Implementation

### Step 1: Configure Repository Priority

1. **Update `settings.gradle.kts`** to use `mavenLocal()` first
2. **Remove custom repository configuration** (no longer needed)
3. **Ensure proper fallback** to other repositories for non-Apollo dependencies

### Step 2: Configure CI Workflow

The GitHub Actions workflow automatically:

```yaml
- name: Build Apollo Kotlin with WASM support
  run: |
    git clone https://github.com/apollographql/apollo-kotlin.git apollo-kotlin
    cd apollo-kotlin
    git checkout main
    sed -i 's/VERSION_NAME=.*/VERSION_NAME=5.0.0-alpha.local-SNAPSHOT/' gradle.properties
    ./gradlew publishToMavenLocal -x test
```

### Step 3: Test Local CI Pipeline

1. **Run the exact CI command locally:**
   ```bash
   ./gradlew :composeApp:wasmJsBrowserDistribution
   ```

2. **Simulate Apollo build locally:**
   ```bash
   git clone https://github.com/apollographql/apollo-kotlin.git apollo-kotlin
   cd apollo-kotlin
   git checkout main
   sed -i 's/VERSION_NAME=.*/VERSION_NAME=5.0.0-alpha.local-SNAPSHOT/' gradle.properties
   ./gradlew publishToMavenLocal -x test
   cd ..
   ./gradlew :composeApp:wasmJsBrowserDistribution
   ```

3. **Verify all required files are generated:**
   ```bash
   ls -la composeApp/build/dist/wasmJs/productionExecutable/
   ```

## 📊 Results Achieved

### Repository Benefits

- **No repository bloat**: Zero committed Apollo artifacts
- **Always fresh**: Uses latest Apollo main branch with WASM support
- **Self-contained CI**: Each CI run builds exactly what it needs
- **Easy maintenance**: No manual dependency management

### Performance

- **Build Time**: ~3-5 minutes for Apollo build + ~1-2 minutes for WasmJS build
- **Bundle Size**: 548KB JavaScript + 10.8MB WebAssembly
- **Git Operations**: Fast due to no large committed artifacts

### CI Compatibility

- ✅ **Fresh Dependencies**: Always uses latest Apollo with WASM support
- ✅ **GitHub Actions Ready**: Builds Apollo from source in CI
- ✅ **Cross-platform**: Works on macOS, Linux (GitHub Actions), Windows

## 🔄 Local CI Execution Commands

### Full CI Simulation

```bash
# Clone and build Apollo (same as CI)
git clone https://github.com/apollographql/apollo-kotlin.git apollo-kotlin
cd apollo-kotlin
git checkout main
sed -i 's/VERSION_NAME=.*/VERSION_NAME=5.0.0-alpha.local-SNAPSHOT/' gradle.properties
./gradlew publishToMavenLocal -x test
cd ..

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

### Quick CI Test (if Apollo already built)

```bash
# Quick build test (reuses Apollo cache)
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
├── .github/workflows/deploy-wasmjs.yml    # GitHub Actions workflow with Apollo build
├── github-pages/                          # CI deployment artifacts (gitignored)
├── settings.gradle.kts                    # Repository configuration (mavenLocal priority)
└── scripts/test-github-actions.sh         # Local CI simulation script
```

## 🐛 Troubleshooting

### Common Issues

1. **"Plugin was not found" Error:**
   - **Root Cause**: Apollo not built or wrong version
   - **Solution**: Ensure Apollo is built with correct version:
     ```bash
     cd apollo-kotlin
     grep VERSION_NAME gradle.properties
     # Should show: VERSION_NAME=5.0.0-alpha.local-SNAPSHOT
     ./gradlew publishToMavenLocal -x test
     ```

2. **WASM Target Not Found:**
   - **Root Cause**: Using official Apollo release instead of source build
   - **Solution**: Verify `mavenLocal()` is first in `settings.gradle.kts`
   - **Check**: Look for "Downloaded from: mavenLocal" in Gradle logs

3. **Build Cache Issues:**
   - Run `./gradlew clean` to clear cache
   - Delete `.gradle/` folder if persistent issues
   - Rebuild Apollo: `cd apollo-kotlin && ./gradlew clean publishToMavenLocal -x test`

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

### When Apollo Main Branch Updates

The CI automatically uses the latest `main` branch, so no manual updates needed unless:

1. **Breaking changes in Apollo main**: Pin to specific commit:
   ```yaml
   git checkout main
   # Change to:
   git checkout SPECIFIC_COMMIT_HASH
   ```

2. **Apollo version scheme changes**: Update version replacement:
   ```yaml
   sed -i 's/VERSION_NAME=.*/VERSION_NAME=NEW_VERSION_PATTERN/' gradle.properties
   ```

### Adding New Target Platforms

1. **Apollo automatically supports new targets** when they're added to main branch
2. **No manual artifact copying needed**
3. **Test new platform builds**:
   ```bash
   ./gradlew :composeApp:NEW_PLATFORM_TARGET
   ```

## 📚 References

- **GitHub Actions Workflow**: `.github/workflows/deploy-wasmjs.yml`
- **Local CI Script**: `scripts/test-github-actions.sh`
- **Deployment Guide**: `DEPLOYMENT.md`
- **Apollo Kotlin Repository**: https://github.com/apollographql/apollo-kotlin
- **Gradle Repository Management**: https://docs.gradle.org/current/userguide/repository_types.html

---

This document ensures that future CI pipeline execution and Apollo dependency management can be performed efficiently
using the latest Apollo Kotlin source with WASM support.