#!/bin/bash

# Mini Banking Development Stop Script
# This script stops the development environment

set -e

echo "🛑 Stopping Mini Banking Development Environment..."
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

# Stop with data cleanup
stop_with_cleanup() {
    print_status "Stopping PostgreSQL database and cleaning up data..."
    
    if docker ps | grep -q "mini-banking-postgres-dev"; then
        docker-compose -f docker-compose.dev.yml down -v
        print_success "Database stopped and data cleaned up"
    else
        print_warning "Database was not running"
    fi
}

# Show running processes
show_status() {
    print_status "Current status:"
    echo ""
    
    # Check if database is running
    if docker ps | grep -q "mini-banking-postgres-dev"; then
        echo "✅ PostgreSQL database is running"
    else
        echo "❌ PostgreSQL database is not running"
    fi
    
    # Check if Spring Boot is running
    if pgrep -f "spring-boot:run" > /dev/null; then
        echo "✅ Spring Boot application is running"
    else
        echo "❌ Spring Boot application is not running"
    fi
}

# Main execution
main() {
    case "${1:-stop}" in
        "stop")
            stop_database
            print_success "Development environment stopped"
            ;;
        "clean")
            stop_with_cleanup
            print_success "Development environment stopped and cleaned up"
            ;;
        "status")
            show_status
            ;;
        *)
            echo "Usage: $0 {stop|clean|status}"
            echo ""
            echo "Commands:"
            echo "  stop    - Stop the development environment (default)"
            echo "  clean   - Stop and clean up all data"
            echo "  status  - Show current status"
            exit 1
            ;;
    esac
}

# Run main function
main "$@"
