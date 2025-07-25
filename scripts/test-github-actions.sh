#!/bin/bash

# Script to test GitHub Actions locally using nektos/act
# Make sure you have act installed: https://github.com/nektos/act

echo "🚀 Testing GitHub Actions workflow locally with act..."

# Check if act is installed
if ! command -v act &> /dev/null; then
    echo "❌ act is not installed. Please install it first:"
    echo "   macOS: brew install act"
    echo "   Linux: Check https://github.com/nektos/act#installation"
    exit 1
fi

echo "✅ act is installed"

# Create .actrc file for configuration if it doesn't exist
if [ ! -f .actrc ]; then
    echo "📝 Creating .actrc configuration file..."
    cat > .actrc << 'EOF'
-P ubuntu-latest=ghcr.io/catthehacker/ubuntu:act-latest
--artifact-server-path /tmp/artifacts
--env-file .env.local
EOF
fi

# Create local environment file for testing
if [ ! -f .env.local ]; then
    echo "📝 Creating .env.local for local testing..."
    cat > .env.local << 'EOF'
# Local environment variables for testing
GITHUB_TOKEN=fake_token_for_local_testing
EOF
fi

echo "🔧 Running GitHub Actions workflow locally..."
echo "Note: This will build the WasmJS target but won't actually deploy to GitHub Pages"

# Run the workflow
act -W .github/workflows/deploy-wasmjs.yml \
    --job build-and-deploy \
    --artifact-server-path /tmp/artifacts \
    -v

echo "✅ Local GitHub Actions test completed!"
echo ""
echo "📋 Next steps:"
echo "1. If the build succeeded, your workflow is ready for GitHub"
echo "2. Push to GitHub to trigger actual deployment to GitHub Pages"
echo "3. Enable GitHub Pages in your repository settings"
echo ""
echo "🌐 GitHub Pages setup:"
echo "   Go to Settings > Pages > Source: GitHub Actions"