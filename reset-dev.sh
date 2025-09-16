#!/bin/bash

# Mini Banking Development Reset Script
# This script resets the development environment completely

set -e

echo "🔄 Resetting Mini Banking Development Environment..."
echo "=================================================="

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

# Stop all services
stop_services() {
    print_status "Stopping all services..."
    
    # Stop Spring Boot if running
    if pgrep -f "spring-boot:run" > /dev/null; then
        print_status "Stopping Spring Boot application..."
        pkill -f "spring-boot:run" || true
        sleep 2
    fi
    
    # Stop database
    if docker ps | grep -q "mini-banking-postgres-dev"; then
        print_status "Stopping PostgreSQL database..."
        docker-compose -f docker-compose.dev.yml down -v
    fi
    
    print_success "All services stopped"
}

# Clean up Docker resources
cleanup_docker() {
    print_status "Cleaning up Docker resources..."
    
    # Remove stopped containers
    docker container prune -f
    
    # Remove unused volumes
    docker volume prune -f
    
    # Remove unused networks
    docker network prune -f
    
    print_success "Docker resources cleaned up"
}

# Clean up Maven cache
cleanup_maven() {
    print_status "Cleaning up Maven cache..."
    
    if command -v mvn &> /dev/null; then
        mvn clean -q
        print_success "Maven cache cleaned up"
    else
        print_warning "Maven not found, skipping Maven cleanup"
    fi
}

# Clean up target directory
cleanup_target() {
    print_status "Cleaning up target directory..."
    
    if [ -d "target" ]; then
        rm -rf target
        print_success "Target directory cleaned up"
    fi
}

# Rebuild application
rebuild_app() {
    print_status "Rebuilding application..."
    
    if command -v mvn &> /dev/null; then
        mvn clean compile -q
        print_success "Application rebuilt successfully"
    else
        print_error "Maven not found, cannot rebuild application"
        exit 1
    fi
}

# Main execution
main() {
    print_warning "This will completely reset the development environment."
    print_warning "All data will be lost!"
    echo ""
    
    # Ask for confirmation
    read -p "Are you sure you want to continue? (y/N): " -n 1 -r
    echo ""
    
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        print_status "Reset cancelled"
        exit 0
    fi
    
    echo ""
    print_status "Starting reset process..."
    echo ""
    
    # Stop all services
    stop_services
    
    # Clean up resources
    cleanup_docker
    cleanup_maven
    cleanup_target
    
    # Rebuild application
    rebuild_app
    
    echo ""
    print_success "Development environment reset completed!"
    print_status "You can now run './start-dev.sh' to start fresh"
}

# Run main function
main "$@"
