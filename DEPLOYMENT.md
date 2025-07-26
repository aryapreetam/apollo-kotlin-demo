# 🚀 Deployment Guide

This guide explains how to deploy the Apollo Kotlin GraphQL Subscriptions Demo to production.

## 📋 Overview

- **WasmJS Client**: Deployed to GitHub Pages using GitHub Actions
- **GraphQL Server**: Deployed to Render.com using Docker

## 🌐 WasmJS Deployment to GitHub Pages

### Prerequisites

- GitHub repository with GitHub Pages enabled
- `act` installed for local testing (optional)

### Setup Steps

1. **Enable GitHub Pages**:
    - Go to your repository settings
    - Navigate to **Pages** section
    - Set **Source** to "GitHub Actions"

2. **Test Locally** (Optional):
   ```bash
   # Install act if not already installed
   brew install act  # macOS
   
   # Test the workflow locally
   ./scripts/test-github-actions.sh
   ```

3. **Deploy**:
    - Push changes to the `main` branch
    - GitHub Actions will automatically build and deploy
    - Access your app at: `https://your-username.github.io/apollo-kotlin-demo`

### What Gets Deployed

- Production-optimized WasmJS build with WASM support
- Direct access to the Compose Multiplatform app
- No landing page - users see the app immediately

### Workflow Details

The GitHub Actions workflow (`.github/workflows/deploy-wasmjs.yml`):

- ✅ Builds Apollo Kotlin from source with WASM support
- ✅ Builds WasmJS production bundle
- ✅ Deploys directly to GitHub Pages
- ✅ Triggered on push to main branch

### Apollo Kotlin WASM Support

This project uses a custom Apollo Kotlin build (`5.0.0-alpha.local-SNAPSHOT`) with WASM support that isn't available in
public Maven repositories. The CI automatically:

1. **Clones** the latest Apollo Kotlin repository
2. **Sets version** to `5.0.0-alpha.local-SNAPSHOT`
3. **Builds and publishes** to local Maven repository
4. **Uses the fresh build** for the WasmJS compilation

This ensures the latest WASM-compatible Apollo version is always used without repository bloat.

## 🖥️ Server Deployment to Render.com

### Prerequisites

- Render.com account (free tier available)
- GitHub repository connected to Render.com

### Setup Steps

1. **Connect Repository**:
    - Go to [Render.com](https://render.com)
    - Connect your GitHub account
    - Select this repository

2. **Automatic Deployment**:
    - Render.com will detect the `render.yaml` configuration
    - The service will be created automatically
    - Docker build will start on first deploy

3. **Manual Setup** (Alternative):
    - Create a new "Web Service"
    - Choose "Deploy from Git"
    - Select your repository
    - Use these settings:
      ```
      Runtime: Docker  
      Dockerfile Path: ./server/Dockerfile
      Branch: main
      ```

### Environment Variables

The following are automatically configured:

- `PORT`: Provided by Render.com
- `JAVA_OPTS`: Memory optimization for free tier

### What Gets Deployed

- Ktor GraphQL server with subscriptions
- CORS configured for cross-origin requests- GraphiQL playground at `/graphiql`

## 🔧 Configuration Files

### 📁 `.github/workflows/deploy-wasmjs.yml`

GitHub Actions workflow for WasmJS deployment:

- Builds production WasmJS bundle
- Deploys directly to GitHub Pages

### 📁 `server/Dockerfile`

Multi-stage Docker build:

- Build stage: Compiles Kotlin/JVM server
- Runtime stage: Minimal JRE container
- Optimized for Render.com deployment

### 📁 `render.yaml`

Render.com service configuration:

- Automatic deployments from GitHub
- Free tier optimized settings
- Health check configuration

## 🧪 Testing Deployments

### Local Testing

1. **Test WasmJS Build**:
   ```bash
   ./gradlew :composeApp:wasmJsBrowserDistribution
   ```

2. **Test Server Docker Build**:
   ```bash
   cd server
   docker build -t apollo-kotlin-demo .
   docker run -p 8080:8080 apollo-kotlin-demo
   ```

3. **Test GitHub Actions Locally**:
   ```bash
   ./scripts/test-github-actions.sh
   ```

### Production Verification

1. **WasmJS App**: Visit `https://your-username.github.io/apollo-kotlin-demo`
2. **GraphQL Server**: Visit your Render.com URL + `/graphiql`
3. **Test Subscriptions**: Use GraphiQL to test real-time subscriptions

## 🔄 Continuous Deployment

Both deployments are automated:

- **GitHub Pages**: Deploys on every push to `main`
- **Render.com**: Deploys on every push to `main`

## 📊 Monitoring

### GitHub Pages

- Check deployment status in the "Actions" tab
- View deployment history in repository settings

### Render.com

- Monitor deployments in Render.com dashboard
- View logs and metrics
- Set up alerts for downtime

## 🐛 Troubleshooting

### WasmJS Deployment Issues

- Check GitHub Actions logs
- Ensure GitHub Pages is enabled
- Verify build artifacts are generated

### Server Deployment Issues

- Check Render.com build logs
- Verify Dockerfile builds locally
- Check environment variables

### Common Solutions

```bash
# Rebuild and test locally
./gradlew clean
./gradlew :composeApp:wasmJsBrowserDistribution
./gradlew :server:buildFatJar

# Test server locally
./gradlew :server:run
```

## 🎯 Next Steps

After successful deployment:

1. **Update README.md** with your live URLs
2. **Test all platforms** against the deployed server
3. **Share your demo** with the community!

## 📝 URLs to Update

Replace these placeholders in `README.md`:

- `[Your Render.com URL]` → Your actual Render.com service URL
- `[Your GitHub Pages URL]` → `https://your-username.github.io/apollo-kotlin-demo`
- `your-username` → Your actual GitHub username