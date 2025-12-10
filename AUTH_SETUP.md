# Spring Boot Authentication with JWT & Password Hashing

This document explains the authentication system implemented in your Spring Boot application.

## Overview

The authentication system includes:
- **User Registration** with email, password, and role
- **User Login** with email and password
- **JWT Token Generation** for stateless authentication
- **Password Hashing** using BCrypt
- **JWT Validation Filter** for protected endpoints

## Architecture

### Components Created

1. **Entity**
   - `User.java` - User entity with email, hashed password, and role

2. **Repository**
   - `UserRepository.java` - JPA repository for database operations

3. **DTOs (Data Transfer Objects)**
   - `LoginRequestDto.java` - Email and password for login
   - `RegisterRequestDto.java` - Email, password, and role for registration
   - `LoginResponseDto.java` - JWT token, userId, email, and role response

4. **Service**
   - `AuthService.java` - Business logic for login and registration

5. **Controller**
   - `AuthController.java` - REST endpoints for auth operations

6. **Security**
   - `JwtAuthenticationFilter.java` - Filter to validate JWT tokens on each request
   - `SecurityConfig.java` - Spring Security configuration

7. **Utility**
   - `JwtUtil.java` - JWT token generation and validation

## API Endpoints

### 1. Register User
**POST** `/auth/register`

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123",
  "role": "ADMIN"
}
```

**Response (200 OK):**
```json
{
  "jwt": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "email": "user@example.com",
  "role": "ADMIN"
}
```

**Error Cases:**
- `400 Bad Request` - Invalid input
- `409 Conflict` - Email already exists

---

### 2. Login User
**POST** `/auth/login`

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "jwt": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "email": "user@example.com",
  "role": "ADMIN"
}
```

**Error Cases:**
- `404 Not Found` - User not found
- `401 Unauthorized` - Invalid password

---

### 3. Access Protected Endpoints

Add the JWT token to the Authorization header:

```
Authorization: Bearer <your_jwt_token>
```

Example using curl:
```bash
curl -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  http://localhost:8080/api/protected-endpoint
```

## Configuration

Edit `application.yml` to configure:

```yaml
jwt:
  secret: your-secret-key-change-this-in-production-with-at-least-32-characters
  expiration: 86400000  # 24 hours in milliseconds

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/stitcho_db
    username: postgres
    password: your_password
```

### Important: Change JWT Secret in Production

The default secret is for development only. In production, use a strong secret:
- At least 32 characters
- Mix of uppercase, lowercase, numbers, and special characters
- Store in environment variables, not in code

Example:
```yaml
jwt:
  secret: ${JWT_SECRET}  # Load from environment variable
```

## How It Works

### 1. Registration Flow
1. User sends email, password, and role to `/auth/register`
2. System checks if email already exists
3. Password is hashed using BCrypt
4. User is saved to database
5. JWT token is generated and returned

### 2. Login Flow
1. User sends email and password to `/auth/login`
2. System finds user by email
3. Password is verified against hashed password
4. JWT token is generated with user info (email, role, userId)
5. Token is returned to client

### 3. Protected Request Flow
1. Client sends request with `Authorization: Bearer <token>` header
2. `JwtAuthenticationFilter` intercepts the request
3. Token is validated and user info is extracted
4. User is authenticated in Spring Security context
5. Request proceeds to the controller

## JWT Token Structure

The JWT token contains:
- **Subject (sub)**: User email
- **Role (role)**: User role (e.g., ADMIN, USER)
- **UserId (userId)**: User ID
- **Issued At (iat)**: Token creation time
- **Expiration (exp)**: Token expiration time

Example decoded token:
```json
{
  "sub": "user@example.com",
  "role": "ADMIN",
  "userId": 1,
  "iat": 1701234567,
  "exp": 1701320967
}
```

## Password Security

- Passwords are hashed using **BCrypt** algorithm
- Plain text passwords are never stored
- Each password hash is unique (salted)
- Even if database is compromised, passwords are safe

## Security Features

1. **CSRF Protection**: Disabled for stateless JWT authentication
2. **Session Management**: Stateless (no server-side sessions)
3. **Password Encoding**: BCrypt with salt
4. **Token Validation**: Signature verification on every request
5. **Role-Based Access**: Roles are embedded in JWT and can be used for authorization

## Testing with Postman

### 1. Register
```
POST http://localhost:8080/auth/register
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "Test@123",
  "role": "USER"
}
```

### 2. Login
```
POST http://localhost:8080/auth/login
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "Test@123"
}
```

### 3. Use Token
Copy the JWT from response and use in Authorization header:
```
GET http://localhost:8080/api/any-protected-endpoint
Authorization: Bearer <paste_jwt_here>
```

## Database Schema

The `users` table will be created automatically with:
- `id` (BIGINT, Primary Key)
- `email` (VARCHAR, Unique, Not Null)
- `password` (VARCHAR, Not Null) - Hashed password
- `role` (VARCHAR, Not Null) - User role

## Troubleshooting

### "User not found" error
- Check if user exists in database
- Verify email spelling

### "Invalid credentials" error
- Password is incorrect
- Ensure password is typed correctly

### "Missing mandatory Classpath entries"
- Run `mvn clean install` to download dependencies
- Rebuild the project

### JWT validation fails
- Token may be expired
- Token signature may be invalid
- Secret key may have changed

## Next Steps

1. Add more endpoints and protect them with roles:
```java
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/admin-only")
public ResponseEntity<?> adminEndpoint() {
    return ResponseEntity.ok("Admin access granted");
}
```

2. Add custom exception handling for better error messages

3. Implement refresh tokens for better security

4. Add email verification for registration

5. Add password reset functionality
