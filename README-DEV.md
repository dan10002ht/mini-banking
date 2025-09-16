# Mini Banking - Development Setup

## 🚀 **Recommended: Local Development**

### **1. Start Database (Docker):**

```bash
# Start PostgreSQL in Docker
docker-compose -f docker-compose.dev.yml up -d

# Check if database is running
docker ps
```

### **2. Run Application (Local):**

```bash
# Run with hot reload
mvn spring-boot:run

# Or with debug mode
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

### **3. Access Application:**

- **API:** http://localhost:8080
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **H2 Console:** http://localhost:8080/h2-console (if using H2)
- **PostgreSQL:** localhost:5432

## 🔧 **Development Features:**

### **Hot Reload:**

- Spring Boot DevTools automatically restarts when code changes
- No need to rebuild or restart manually

### **Debug Mode:**

- Set breakpoints in IDE
- Step through code
- Inspect variables

### **Database:**

- PostgreSQL running in Docker
- Data persists between restarts
- Easy to reset: `docker-compose -f docker-compose.dev.yml down -v`

## 🐳 **Alternative: Full Docker (Not Recommended for Dev)**

```bash
# Build and run everything in Docker
docker-compose up --build

# This is slower and has no hot reload
```

## 📝 **Commands:**

```bash
# Start database only
docker-compose -f docker-compose.dev.yml up -d

# Stop database
docker-compose -f docker-compose.dev.yml down

# Reset database (delete all data)
docker-compose -f docker-compose.dev.yml down -v

# Run app locally
mvn spring-boot:run

# Run tests
mvn test

# Build JAR
mvn clean package
```

## 🎯 **Why Local Development?**

1. **Fast iteration** - Hot reload, no rebuild
2. **Better debugging** - IDE integration
3. **Faster compilation** - Only changed files
4. **Better tooling** - IntelliJ/Eclipse features
5. **Easier testing** - Quick test runs

## 🚀 **Production Deployment:**

For production, use the full Docker setup:

```bash
docker-compose up --build
```
