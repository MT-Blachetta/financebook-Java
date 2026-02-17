# Java Version Management - Keep Multiple Versions

## ✅ Recommended Approach: Keep Both Java 17 and Java 25

You **DO NOT** need to uninstall Java 25. Modern development often requires multiple Java versions for different projects.

---

## 🎯 Solution: Install Java 17 Alongside Java 25

### Option 1: SDKMAN (Easiest - Recommended)

SDKMAN lets you install and switch between multiple Java versions easily.

```bash
# 1. Install SDKMAN (if not installed)
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# 2. List available Java 17 versions
sdk list java | grep "17\."

# 3. Install Java 17 LTS (Temurin recommended for enterprise)
sdk install java 17.0.10-tem

# 4. Use Java 17 for current terminal session
sdk use java 17.0.10-tem

# 5. Verify (should show Java 17)
java -version

# 6. Build the project
cd ~/Projects/javabook/financebook-Java/financeJava/backend
mvn clean install
```

**Benefits**:
- No uninstallation needed
- Easy switching: `sdk use java <version>`
- Per-project Java version: create `.sdkmanrc` file
- Java 25 remains available for other projects

**Set Java 17 as default** (optional):
```bash
spr
```

**Switch back to Java 25** later:
```bash
sdk use java 25.0.2-tem
```

---

### Option 2: Update Alternatives (Linux)

If you prefer system-level management without SDKMAN:

```bash
# 1. Download Java 17 manually
# Visit: https://adoptium.net/temurin/releases/?version=17
# Download for Linux x64 (.tar.gz)

# 2. Extract to /opt
sudo tar -xzf OpenJDK17U-jdk_x64_linux_hotspot_17.0.10_7.tar.gz -C /opt/

# 3. Register with update-alternatives
sudo update-alternatives --install /usr/bin/java java \
  /opt/jdk-17.0.10+7/bin/java 1

sudo update-alternatives --install /usr/bin/javac javac \
  /opt/jdk-17.0.10+7/bin/javac 1

# 4. Switch to Java 17
sudo update-alternatives --config java
# Select the Java 17 option from the list

sudo update-alternatives --config javac
# Select the Java 17 option from the list

# 5. Verify
java -version
javac -version
```

**Switch back to Java 25**:
```bash
sudo update-alternatives --config java
# Select Java 25 from the list
```

---

### Option 3: Environment Variable Per Project

Keep both installed, use JAVA_HOME per project:

```bash
# 1. Install Java 17 to a location (e.g., /opt/jdk-17)

# 2. For THIS PROJECT ONLY, set JAVA_HOME before building
export JAVA_HOME=/opt/jdk-17.0.10+7
export PATH=$JAVA_HOME/bin:$PATH

# 3. Verify
java -version  # Should show Java 17

# 4. Build
cd financeJava/backend
mvn clean install

# 5. Run
mvn spring-boot:run
```

**Create a helper script** (`run-with-java17.sh`):
```bash
#!/bin/bash
export JAVA_HOME=/opt/jdk-17.0.10+7
export PATH=$JAVA_HOME/bin:$PATH
cd financeJava/backend
mvn spring-boot:run
```

---

## 🎯 Per-Project Java Version with SDKMAN (Best Practice)

Create a `.sdkmanrc` file in the project root:

```bash
cd ~/Projects/javabook/financebook-Java/financeJava
echo "java=17.0.10-tem" > .sdkmanrc
```

Now whenever you `cd` into this directory:
```bash
cd financeJava
sdk env  # Automatically switches to Java 17!
```

---

## 📋 Summary: You Have 3 Options

| Option | Java 25 Status | Complexity | Best For |
|--------|----------------|------------|----------|
| **SDKMAN** | ✅ Keep | Easy | Developers with multiple projects |
| **update-alternatives** | ✅ Keep | Medium | System-wide management |
| **JAVA_HOME per project** | ✅ Keep | Manual | Quick testing |

---

## ✅ Verification After Setup

```bash
# 1. Check Java version
java -version
# Must show: openjdk version "17.0.x"

# 2. Check Maven uses correct Java
mvn -version
# Must show: Java version: 17.0.x

# 3. Clean and build
cd financeJava/backend
mvn clean compile

# 4. Look for SUCCESS
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------

# 5. Run the application
mvn spring-boot:run

# 6. Test the API
curl http://localhost:8080/auth/login -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'
```

---

## 🚀 Quick Start (SDKMAN Recommended)

```bash
# One-time setup
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 17.0.10-tem

# Every time you work on this project
cd ~/Projects/javabook/financebook-Java/financeJava/backend
sdk use java 17.0.10-tem
mvn clean install
mvn spring-boot:run
```

---

## ❓ FAQ

**Q: Will this break my Java 25 projects?**  
A: No! You can switch between versions anytime with `sdk use` or `update-alternatives --config`.

**Q: Can I set different Java versions for different projects?**  
A: Yes! Use `.sdkmanrc` files or project-specific scripts.

**Q: Do I need to restart my computer?**  
A: No! Just reload your terminal: `source ~/.bashrc` or open a new terminal.

**Q: What if I want Java 17 as system default?**  
A: Run `sdk default java 17.0.10-tem` or use `update-alternatives`.

---

**Bottom Line**: Install Java 17, keep Java 25, switch as needed. No uninstallation required!
