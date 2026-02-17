# Admin Panel Login Instructions

## Fixed Admin Login Issue

The admin user password conflict has been resolved.

### Default Admin Credentials

- **Username:** `admin`
- **Password:** `admin`

### What Was Fixed

1. **Removed conflicting Flyway migration** (`V2__seed_admin_user.sql`)
   - This migration was creating the admin user with a dummy password
   - It prevented the DatabaseInitializer from setting the correct password

2. **Updated DatabaseInitializer** to:
   - Create admin user if it doesn't exist
   - Update the password if the user already exists (handles migration cleanup)
   - Always ensure the password matches the configuration

### How to Apply the Fix

#### Option 1: Drop and Recreate Database (Clean Slate)

```bash
# Connect to PostgreSQL
psql -U yourself -d postgres

# Drop the existing database
DROP DATABASE financebook;

# Create a new database
CREATE DATABASE financebook;

# Exit psql
\q

# Restart the Spring Boot application
cd financeJava/backend
mvn spring-boot:run
```

#### Option 2: Update Existing Database (Keeps Data)

Just restart the Spring Boot application. The DatabaseInitializer will now automatically update the admin password on startup:

```bash
cd financeJava/backend
mvn spring-boot:run
```

Check the logs for:
```
Admin user created successfully with password from configuration
```
or
```
Updating admin user password from configuration
Admin password updated successfully
```

### Accessing the Admin Panel

1. Start the backend: `mvn spring-boot:run`
2. Navigate to: http://localhost:8080/admin/login
3. Login with:
   - Username: `admin`
   - Password: `admin`

### Changing the Default Password

You can change the default admin password by setting an environment variable:

```bash
export ADMIN_DEFAULT_PASSWORD=your-secure-password
mvn spring-boot:run
```

Or in `application.yml`:
```yaml
admin:
  default-password: your-secure-password
```

### Admin Panel Features

Once logged in, you can access:

- **Dashboard** (`/admin/dashboard`) - System statistics
- **Users** (`/admin/users`) - User management
- **Fees** (`/admin/fees`) - Transaction fee configuration
- **API Docs** (`/docs`) - Swagger UI

### Troubleshooting

If you still get "invalid username or password":

1. Check the application logs for admin user creation messages
2. Verify the database has the admin user:
   ```sql
   SELECT username, is_admin, is_active FROM "user" WHERE username = 'admin';
   ```
3. Reset the database using Option 1 above
4. Ensure Spring Boot application restarted after fixing the files
