#!/bin/bash

# Mini Banking Development Script
# Usage: ./start-dev.sh [command]
# Commands: start, stop, restart, reset, status, clean

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
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

print_header() {
    echo -e "${PURPLE}[HEADER]${NC} $1"
}

print_command() {
    echo -e "${CYAN}[COMMAND]${NC} $1"
}

# Check if Docker is running
check_docker() {
    print_status "Checking Docker..."
    if ! docker info > /dev/null 2>&1; then
        print_error "Docker is not running. Please start Docker first."
        exit 1
    fi
    print_success "Docker is running"
}

# Check if Maven is installed
check_maven() {
    print_status "Checking Maven..."
    if ! command -v mvn &> /dev/null; then
        print_error "Maven is not installed. Please install Maven first."
        exit 1
    fi
    print_success "Maven is available"
}

# Start PostgreSQL database
start_database() {
    print_status "Starting PostgreSQL database..."
    
    # Check if database is already running
    if docker ps | grep -q "mini-banking-postgres-dev"; then
        print_warning "Database is already running"
    else
        # Start database
        docker-compose -f docker-compose.dev.yml up -d postgres
        
        # Wait for database to be ready
        print_status "Waiting for database to be ready..."
        sleep 5
        
        # Check if database is ready
        max_attempts=30
        attempt=1
        while [ $attempt -le $max_attempts ]; do
            if docker exec mini-banking-postgres-dev pg_isready -U postgres > /dev/null 2>&1; then
                print_success "Database is ready"
                break
            fi
            print_status "Waiting for database... (attempt $attempt/$max_attempts)"
            sleep 2
            attempt=$((attempt + 1))
        done
        
        if [ $attempt -gt $max_attempts ]; then
            print_error "Database failed to start within expected time"
            exit 1
        fi
    fi
}

# Stop PostgreSQL database
stop_database() {
    print_status "Stopping PostgreSQL database..."
    
    if docker ps | grep -q "mini-banking-postgres-dev"; then
        docker-compose -f docker-compose.dev.yml down
        print_success "Database stopped"
    else
        print_warning "Database was not running"
    fi
}

# Stop database with cleanup
stop_database_clean() {
    print_status "Stopping PostgreSQL database and cleaning up data..."
    
    if docker ps | grep -q "mini-banking-postgres-dev"; then
        docker-compose -f docker-compose.dev.yml down -v
        print_success "Database stopped and data cleaned up"
    else
        print_warning "Database was not running"
    fi
}

# Build the application
build_app() {
    print_status "Building application..."
    mvn clean compile -q
    print_success "Application built successfully"
}

# Start the application
start_app() {
    print_status "Starting Spring Boot application with hot reload..."
    print_status "Application will be available at: http://localhost:8080"
    print_status "Swagger UI will be available at: http://localhost:8080/swagger-ui.html"
    print_status "Press Ctrl+C to stop the application"
    echo ""
    
    # Start the application
    mvn spring-boot:run
}

# Stop the application
stop_app() {
    print_status "Stopping Spring Boot application..."
    
    if pgrep -f "spring-boot:run" > /dev/null; then
        pkill -f "spring-boot:run"
        sleep 2
        print_success "Application stopped"
    else
        print_warning "Application was not running"
    fi
}

# Show status
show_status() {
    print_header "Mini Banking Development Status"
    echo "=================================="
    echo ""
    
    # Check if database is running
    if docker ps | grep -q "mini-banking-postgres-dev"; then
        echo "✅ PostgreSQL database is running"
        echo "   - Container: mini-banking-postgres-dev"
        echo "   - Port: 5432"
        echo "   - Database: mini_banking"
    else
        echo "❌ PostgreSQL database is not running"
    fi
    
    echo ""
    
    # Check if Spring Boot is running
    if pgrep -f "spring-boot:run" > /dev/null; then
        echo "✅ Spring Boot application is running"
        echo "   - Port: 8080"
        echo "   - API: http://localhost:8080"
        echo "   - Swagger: http://localhost:8080/swagger-ui.html"
    else
        echo "❌ Spring Boot application is not running"
    fi
    
    echo ""
    
    # Check if ports are in use
    if lsof -i :8080 > /dev/null 2>&1; then
        echo "🔍 Port 8080 is in use"
    fi
    
    if lsof -i :5432 > /dev/null 2>&1; then
        echo "🔍 Port 5432 is in use"
    fi
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

# Reset everything
reset_environment() {
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
    stop_app
    stop_database_clean
    
    # Clean up resources
    cleanup_docker
    cleanup_maven
    cleanup_target
    
    # Rebuild application
    build_app
    
    echo ""
    print_success "Development environment reset completed!"
    print_status "You can now run './start-dev.sh start' to start fresh"
}

# Show help
show_help() {
    print_header "Mini Banking Development Script"
    echo "====================================="
    echo ""
    echo "Usage: ./start-dev.sh [command]"
    echo ""
    echo "Commands:"
    echo "  start     - Start the development environment (default)"
    echo "  stop      - Stop the development environment"
    echo "  restart   - Restart the development environment"
    echo "  reset     - Reset everything (clean up all data)"
    echo "  status    - Show current status"
    echo "  clean     - Clean up Docker resources and Maven cache"
    echo "  help      - Show this help message"
    echo ""
    echo "Examples:"
    echo "  ./start-dev.sh           # Start development environment"
    echo "  ./start-dev.sh start     # Start development environment"
    echo "  ./start-dev.sh stop      # Stop development environment"
    echo "  ./start-dev.sh restart   # Restart development environment"
    echo "  ./start-dev.sh status    # Show current status"
    echo "  ./start-dev.sh reset     # Reset everything"
    echo "  ./start-dev.sh clean     # Clean up resources"
    echo ""
}

# Cleanup function
cleanup() {
    echo ""
    print_warning "Shutting down development environment..."
    stop_app
    print_status "Database will continue running. To stop it, run:"
    print_status "docker-compose -f docker-compose.dev.yml down"
    print_success "Development environment stopped"
}

# Set up signal handlers
trap cleanup SIGINT SIGTERM

# Main execution
main() {
    local command="${1:-start}"
    
    case "$command" in
        "start")
            print_header "Starting Mini Banking Development Environment"
            echo "=================================================="
            echo ""
            
            # Pre-flight checks
            check_docker
            check_maven
            
            # Start services
            start_database
            build_app
            
            echo ""
            print_success "Development environment is ready!"
            echo ""
            
            # Start the application
            start_app
            ;;
        "stop")
            print_header "Stopping Mini Banking Development Environment"
            echo "=================================================="
            echo ""
            
            stop_app
            stop_database
            print_success "Development environment stopped"
            ;;
        "restart")
            print_header "Restarting Mini Banking Development Environment"
            echo "====================================================="
            echo ""
            
            stop_app
            stop_database
            sleep 2
            start_database
            build_app
            start_app
            ;;
        "reset")
            print_header "Resetting Mini Banking Development Environment"
            echo "===================================================="
            echo ""
            
            reset_environment
            ;;
        "status")
            show_status
            ;;
        "clean")
            print_header "Cleaning Up Development Environment"
            echo "======================================="
            echo ""
            
            cleanup_docker
            cleanup_maven
            cleanup_target
            print_success "Cleanup completed"
            ;;
        "help"|"-h"|"--help")
            show_help
            ;;
        *)
            print_error "Unknown command: $command"
            echo ""
            show_help
            exit 1
            ;;
    esac
}

# Run main function
main "$@"