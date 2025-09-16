# Mini Banking Development Scripts

## 🚀 **Quick Start**

```bash
# Start development environment (default)
./start-dev.sh

# Or explicitly
./start-dev.sh start
```

## 📋 **Available Commands**

### **1. Start Development Environment**

```bash
./start-dev.sh start
```

- Starts PostgreSQL database in Docker
- Builds the application
- Starts Spring Boot with hot reload
- **Default command** - just run `./start-dev.sh`

### **2. Stop Development Environment**

```bash
./start-dev.sh stop
```

- Stops Spring Boot application
- Stops PostgreSQL database
- Keeps data intact

### **3. Restart Development Environment**

```bash
./start-dev.sh restart
```

- Stops everything
- Starts everything again
- Useful for configuration changes

### **4. Show Status**

```bash
./start-dev.sh status
```

- Shows if database is running
- Shows if application is running
- Shows port usage
- Shows container information

### **5. Reset Everything**

```bash
./start-dev.sh reset
```

- ⚠️ **WARNING**: Deletes all data
- Stops all services
- Cleans up Docker resources
- Cleans up Maven cache
- Rebuilds application
- Asks for confirmation

### **6. Clean Up Resources**

```bash
./start-dev.sh clean
```

- Cleans up Docker containers, volumes, networks
- Cleans up Maven cache
- Cleans up target directory
- **Safe** - doesn't delete data

### **7. Show Help**

```bash
./start-dev.sh help
```

- Shows all available commands
- Shows usage examples

## 🎯 **Common Workflows**

### **Daily Development:**

```bash
# Start everything
./start-dev.sh start

# Make changes to code (hot reload works automatically)
# Test your changes

# Stop when done
./start-dev.sh stop
```

### **Reset Everything:**

```bash
# When you want to start fresh
./start-dev.sh reset

# Then start again
./start-dev.sh start
```

### **Check What's Running:**

```bash
# See current status
./start-dev.sh status
```

### **Clean Up (Keep Data):**

```bash
# Clean up resources but keep data
./start-dev.sh clean
```

## 🔧 **Features**

### **Hot Reload:**

- Spring Boot DevTools automatically restarts when code changes
- No need to rebuild or restart manually
- Fast development cycle

### **Database Management:**

- PostgreSQL running in Docker
- Data persists between restarts
- Easy to reset with `reset` command

### **Port Management:**

- Application: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Database: localhost:5432

### **Error Handling:**

- Checks if Docker is running
- Checks if Maven is installed
- Waits for database to be ready
- Colored output for easy reading

## 🚨 **Troubleshooting**

### **Docker Not Running:**

```bash
# Start Docker Desktop first
# Then run the script
./start-dev.sh start
```

### **Maven Not Found:**

```bash
# Install Maven
brew install maven

# Then run the script
./start-dev.sh start
```

### **Port Already in Use:**

```bash
# Check what's using the port
lsof -i :8080
lsof -i :5432

# Kill the process or use different ports
```

### **Database Issues:**

```bash
# Reset everything
./start-dev.sh reset

# Start fresh
./start-dev.sh start
```

## 📝 **Script Features**

- **Colored Output**: Easy to read status messages
- **Error Handling**: Checks prerequisites before starting
- **Signal Handling**: Graceful shutdown on Ctrl+C
- **Status Checking**: Waits for database to be ready
- **Confirmation**: Asks before destructive operations
- **Help System**: Built-in help and usage examples

## 🎉 **Ready to Go!**

Just run `./start-dev.sh` and start coding! 🚀
