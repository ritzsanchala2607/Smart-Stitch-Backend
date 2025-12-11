# Role Setup Guide

## Overview
The system now uses only 3 roles:
- **owner** - Tailor/shop owner
- **worker** - Tailor/shop worker
- **customer** - Customer

## Google OAuth2 Role Assignment
When users sign up via Google OAuth2, they are automatically assigned the **customer** role.

## Database Setup

### Initialize Roles Table
Run the following SQL commands in your database:

```sql
-- Delete existing roles if needed (optional)
DELETE FROM roles;

-- Insert the three roles
INSERT INTO roles (role_name) VALUES ('owner');
INSERT INTO roles (role_name) VALUES ('worker');
INSERT INTO roles (role_name) VALUES ('customer');
```

### Verify Roles
```sql
SELECT * FROM roles;
```

Expected output:
```
id | role_name
---|----------
1  | owner
2  | worker
3  | customer
```

## Registration Endpoint

### Manual Registration (Traditional)
**Endpoint**: `POST /auth/register`

**Request Body**:
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "role": "customer"
}
```

**Allowed Roles**: owner, worker, customer

**Response**:
```json
{
  "message": "User registered successfully",
  "email": "john@example.com",
  "name": "John Doe",
  "role": "customer",
  "jwt": "eyJhbGciOiJIUzI1NiJ9..."
}
```

## Google OAuth2 Registration/Login

### Flow
1. User clicks "Login with Google"
2. Redirected to: `GET /oauth2/authorization/google`
3. User authenticates with Google
4. Backend creates/updates user with **customer** role
5. User is redirected to frontend with JWT token

### Automatic Role Assignment
- New users via Google OAuth2 → **customer** role
- Existing users via Google OAuth2 → Role is preserved (not changed)

## Code Changes

### AuthService.java
- Validates that registration role is one of: owner, worker, customer
- Throws error if role doesn't exist in database

### OAuth2Service.java
- Always assigns **customer** role to new Google OAuth2 users
- Preserves existing role for returning users

### OAuth2AuthenticationSuccessHandler.java
- Always assigns **customer** role to new Google OAuth2 users
- Preserves existing role for returning users

## Testing

### Test Manual Registration
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Owner",
    "email": "owner@example.com",
    "password": "password123",
    "role": "owner"
  }'
```

### Test Invalid Role
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "password": "password123",
    "role": "admin"
  }'
```

Expected error:
```json
{
  "message": "Invalid role. Allowed roles are: owner, worker, customer"
}
```

### Test Google OAuth2
Navigate to: `http://localhost:8080/oauth2/authorization/google`

The user will be created with **customer** role automatically.

## Role-Based Access Control (Future)

To implement role-based access control, use Spring Security annotations:

```java
@PreAuthorize("hasRole('OWNER')")
@PostMapping("/admin/dashboard")
public ResponseEntity<?> ownerDashboard() {
    // Only owners can access
}

@PreAuthorize("hasRole('WORKER')")
@PostMapping("/worker/tasks")
public ResponseEntity<?> workerTasks() {
    // Only workers can access
}

@PreAuthorize("hasRole('CUSTOMER')")
@PostMapping("/customer/orders")
public ResponseEntity<?> customerOrders() {
    // Only customers can access
}
```

## Troubleshooting

### Issue: "Role not found" error during registration
**Solution**: Ensure roles are initialized in database using SQL commands above

### Issue: Google OAuth2 user not getting customer role
**Solution**: Verify `customer` role exists in database and application is restarted

### Issue: Existing user's role changed to customer after Google OAuth2 login
**Solution**: This should not happen - check OAuth2Service.java logic. Existing users should preserve their role.
