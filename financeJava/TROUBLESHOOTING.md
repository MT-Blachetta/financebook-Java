# FinanceBook Java - Troubleshooting Guide

## 🐛 Compilation Errors

### Issue: "cannot find symbol" for getters/setters/builders

**Symptom**:
```
[ERROR] cannot find symbol
  symbol:   method builder()
  location: class com.financebook.entity.User
```

**Root Cause**: Lombok annotations not being processed during compilation

**Diagnosis**:
1. Check Java version: `java -version`
2. Expected: Java 17 LTS
3. If you see Java 25.x → **THIS IS THE PROBLEM**

**Why It Happens**:
- Project targets Java 17 LTS (stable, enterprise-standard)
- Lombok 1.18.30 doesn't support Java 25+ yet
- Java 25 has internal API changes that break Lombok

---

## ✅ Solution 1: Install Java 17 LTS (Recommended)

### Option A: SDKMAN (Linux/Mac)
```bash
# Install SDKMAN if not installed
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Install Java 17 (Temurin/Eclipse Adoptium recommended)
sdk install java 17.0.10-tem

# Set as default
sdk default java 17.0.10-tem

# Verify
java -version
# Should show: openjdk version "17.0.10" 2024-01-16 LTS
```

### Option B: Manual Download
1. Download Eclipse Temurin JDK 17: https://adoptium.net/
2. Install for your platform
3. Set JAVA_HOME environment variable
4. Update PATH to include Java 17 bin directory

### Option C: Update Alternatives (Linux)
```bash
# List installed Java versions
update-alternatives --config java

# Select Java 17 from the list
```

### Verify Installation
```bash
java -version
# Should output Java 17.x.x

mvn -version
# Should show Java version: 17.x.x
```

---

## ✅ Solution 2: Upgrade Lombok (Not Recommended)

**Warning**: Lombok snapshot/edge versions may be unstable.

```xml
<!-- In pom.xml -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>edge-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>

<!-- Add snapshot repository -->
<repositories>
    <repository>
        <id>projectlombok.org</id>
        <url>https://projectlombok.org/edge-releases</url>
    </repository>
</repositories>
```

**Risks**:
- Unstable edge releases
- May break in future builds
- Not production-ready

---

## 🔧 After Fixing Java Version

### Clean and Rebuild
```bash
cd financeJava/backend

# Clean previous build artifacts
mvn clean

# Compile (should succeed now)
mvn compile

# Full build with tests
mvn clean install

# Run application
mvn spring-boot:run
```

### Verify Lombok Works
Look for these in compilation output:
```
[INFO] Compiling 64 source files...
[INFO] BUILD SUCCESS
```

**No** "cannot find symbol" errors = Lombok is working!

---

## 🚨 Other Common Issues

### Issue: Flyway migration fails

**Symptom**: `FlywayException: Unable to obtain connection`

**Solution**:
1. Ensure PostgreSQL is running:
   ```bash
   docker ps | grep financebook-db
   ```

2. If not running, start it:
   ```bash
   docker start financebook-db
   ```

3. Or create new container:
   ```bash
   cd financebookNeo-example
   docker run -d --name financebook-db \
     -p 5432:5432 \
     -v postgres_data:/var/lib/postgresql/data \
     financebook-postgres
   ```

### Issue: Port 8080 already in use

**Symptom**: `Port 8080 is already in use`

**Solution**:
```bash
# Find process using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>

# Or change port in application.yml
server:
  port: 8081
```

### Issue: Lombok not recognized in IDE

**Symptom**: Red underlines in IDE but Maven compiles fine

**Solution for VS Code**:
1. Install "Language Support for Java" extension
2. Reload VS Code
3. May need to configure Java runtime in settings

**Solution for IntelliJ IDEA**:
1. Install Lombok plugin
2. Enable annotation processing:
   - Settings → Build → Compiler → Annotation Processors
   - Check "Enable annotation processing"

**Solution for Eclipse**:
1. Install Lombok into Eclipse:
   ```bash
   java -jar ~/.m2/repository/org/projectlombok/lombok/1.18.30/lombok-1.18.30.jar
   ```
2. Restart Eclipse

---

## 📋 System Requirements Checklist

✅ **Java 17 LTS** (not Java 8, 11, 21, or 25!)  
✅ **Maven 3.8+**  
✅ **PostgreSQL 15** (via Docker)  
✅ **Git**  
✅ **Docker** (for PostgreSQL)

Optional:
- Node.js 18+ (for Vue.js frontend)
- Docker Compose (for full-stack deployment)

---

## 🔍 Diagnostic Commands

```bash
# Check Java version
java -version

# Check Maven version
mvn -version

# Check if Docker is running
docker ps

# Check PostgreSQL connection
psql -h localhost -U yourself -d financebook

# Test backend compilation only
cd financeJava/backend
mvn clean compile

# Run with debug logging
mvn spring-boot:run -X
```

---

## 💡 Quick Fix Summary

**For "cannot find symbol" errors**:
1. **CHECK**: `java -version` → Must be Java 17.x
2. **FIX**: Install Java 17 LTS if needed
3. **CLEAN**: `mvn clean`
4. **BUILD**: `mvn compile`
5. **VERIFY**: No compilation errors

**For JJWT errors**:
- Already fixed in code (using `.parser()` instead of `.parserBuilder()`)

**For Flyway errors**:
- Start PostgreSQL Docker container
- Check connection string in application.yml

---

## 📞 Support

If issues persist:
1. Check this troubleshooting guide
2. Review error messages carefully
3. Ensure all system requirements are met
4. Try `mvn clean install` after each fix

---

**Last Updated**: 2026-02-16  
**Java Version Required**: 17.x LTS  
**Status**: Java 25 incompatibility documented
