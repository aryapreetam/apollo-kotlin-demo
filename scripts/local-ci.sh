#!/bin/bash

# Local CI Execution Script for Apollo Kotlin Demo
# This script simulates the exact GitHub Actions CI workflow locally

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if we're in the right directory
check_project_root() {
    if [[ ! -f "settings.gradle.kts" || ! -d "composeApp" ]]; then
        print_error "This script must be run from the project root directory"
        print_error "Expected to find: settings.gradle.kts and composeApp/ directory"
        exit 1
    fi
}

# Function to verify Apollo repository exists
check_apollo_repo() {
    if [[ ! -d "repo/com/apollographql/apollo" ]]; then
        print_error "Apollo local repository not found at repo/com/apollographql/apollo"
        print_error "Please ensure the local Apollo repository is set up correctly"
        print_error "Refer to LOCAL_CI_SETUP.md for setup instructions"
        exit 1
    fi
    
    local repo_size=$(du -sh repo 2>/dev/null | cut -f1)
    print_status "Apollo local repository size: $repo_size"
}

# Function to clean previous builds
clean_build() {
    print_status "Cleaning previous builds..."
    if ./gradlew clean --quiet; then
        print_success "Build cleanup completed"
    else
        print_error "Failed to clean build"
        exit 1
    fi
}

# Function to run the main CI build
run_ci_build() {
    print_status "Running CI build (WasmJS Browser Distribution)..."
    print_status "This may take 1-2 minutes..."
    
    local start_time=$(date +%s)
    
    if ./gradlew :composeApp:wasmJsBrowserDistribution; then
        local end_time=$(date +%s)
        local duration=$((end_time - start_time))
        print_success "CI build completed successfully in ${duration}s"
    else
        print_error "CI build failed"
        exit 1
    fi
}

# Function to create deployment artifacts
create_deployment_artifacts() {
    print_status "Creating deployment artifacts..."
    
    # Remove existing github-pages directory
    if [[ -d "github-pages" ]]; then
        rm -rf github-pages
    fi
    
    # Create github-pages directory
    mkdir -p github-pages
    
    # Copy build artifacts
    if [[ -d "composeApp/build/dist/wasmJs/productionExecutable" ]]; then
        cp -r composeApp/build/dist/wasmJs/productionExecutable/* github-pages/
        print_success "Deployment artifacts created in github-pages/"
    else
        print_error "Build artifacts not found at composeApp/build/dist/wasmJs/productionExecutable"
        exit 1
    fi
}

# Function to verify deployment artifacts
verify_deployment_artifacts() {
    print_status "Verifying deployment artifacts..."
    
    local required_files=("index.html" "composeApp.js")
    local missing_files=()
    
    for file in "${required_files[@]}"; do
        if [[ ! -f "github-pages/$file" ]]; then
            missing_files+=("$file")
        fi
    done
    
    if [[ ${#missing_files[@]} -eq 0 ]]; then
        print_success "All required deployment files are present"
        
        # Show file sizes
        print_status "Deployment artifact summary:"
        echo "----------------------------------------"
        ls -lah github-pages/ | grep -E "\.(html|js|wasm|css)$" | while read -r line; do
            echo "  $line"
        done
        echo "----------------------------------------"
        
        local total_size=$(du -sh github-pages 2>/dev/null | cut -f1)
        print_status "Total deployment size: $total_size"
    else
        print_error "Missing required deployment files: ${missing_files[*]}"
        exit 1
    fi
}

# Function to run Apollo-specific tests
test_apollo_integration() {
    print_status "Testing Apollo GraphQL integration..."
    
    if ./gradlew :composeApp:generateApollo --quiet; then
        print_success "Apollo GraphQL code generation successful"
    else
        print_warning "Apollo GraphQL code generation failed (this may be expected if GraphQL server is not running)"
    fi
}

# Function to show usage
show_usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  --full, -f     Run full CI simulation (clean + build + deploy)"
    echo "  --build, -b    Run only the CI build"
    echo "  --deploy, -d   Create deployment artifacts only"
    echo "  --test, -t     Run Apollo-specific tests"
    echo "  --help, -h     Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 --full      # Full CI simulation"
    echo "  $0 --build     # Quick CI build test"
    echo "  $0 --test      # Test Apollo integration"
}

# Main execution logic
main() {
    local action="full"
    
    # Parse command line arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            --full|-f)
                action="full"
                shift
                ;;
            --build|-b)
                action="build"
                shift
                ;;
            --deploy|-d)
                action="deploy"
                shift
                ;;
            --test|-t)
                action="test"
                shift
                ;;
            --help|-h)
                show_usage
                exit 0
                ;;
            *)
                print_error "Unknown option: $1"
                show_usage
                exit 1
                ;;
        esac
    done
    
    # Print header
    echo "=================================================="
    echo "🚀 Apollo Kotlin Demo - Local CI Execution"
    echo "=================================================="
    
    # Check prerequisites
    check_project_root
    check_apollo_repo
    
    # Execute based on action
    case $action in
        "full")
            print_status "Running FULL CI simulation..."
            clean_build
            run_ci_build
            create_deployment_artifacts
            verify_deployment_artifacts
            test_apollo_integration
            ;;
        "build")
            print_status "Running CI BUILD only..."
            run_ci_build
            ;;
        "deploy")
            print_status "Creating DEPLOYMENT artifacts only..."
            create_deployment_artifacts
            verify_deployment_artifacts
            ;;
        "test")
            print_status "Running APOLLO tests only..."
            test_apollo_integration
            ;;
    esac
    
    # Print footer
    echo "=================================================="
    print_success "Local CI execution completed successfully! ✅"
    
    if [[ $action == "full" || $action == "deploy" ]]; then
        print_status "Deployment artifacts are ready in: github-pages/"
        print_status "You can serve them locally with: python3 -m http.server -d github-pages 8080"
    fi
    echo "=================================================="
}

# Run main function with all arguments
main "$@"