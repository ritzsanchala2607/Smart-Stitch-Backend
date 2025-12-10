# Authentication API Documentation

## Overview
This Spring Boot application provides a complete authentication system with user registration and login functionality using JWT tokens.

## Project Structure

```
src/main/java/com/stitcho/beta/
├── config/
│   └── SecurityConfig.java          # Security and CORS configuration
├── controller/
│   └── AuthController.java          # REST endpoints for auth
├── dto/
│   ├── RegisterRequest.java         # Registration request DTO
│   ├── LoginRequest.java            # Login request DTO
│   ├── JwtResponse.java             # JWT response DTO
│   └── AuthResponse.java            # Generic auth response DTO
├── model/
│   └── User.java                    # User entity
├── repository/
│   └── UserRepository.java          # User data access
├── security/
│   └── JwtTokenProvider.java        # JWT token generation and validation
└── service/
    └── AuthService.java             # Business logic for auth
```

## API Endpoints

### 1. Register User
**Endpoint:** `POST /api/auth/register`

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123",
  "role": "ADMIN"
}
```

**Response (Success - 201):**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": null
}
```

**Response (Error - 400):**
```json
{
  "success": false,
  "message": "Email already registered",
  "data": null
}
```

### 2. Login User
**Endpoint:** `POST /api/auth/login`

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123",
  "role": "ADMIN"
}
```

**Response (Success - 200):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "type": "Bearer",
    "id": 1,
    "email": "user@example.com",
    "role": "ADMIN"
  }
}
```

**Response (Error - 401):**
```json
{
  "success": false,
  "message": "Invalid password",
  "data": null
}
```

### 3. Validate Token
**Endpoint:** `GET /api/auth/validate`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Response (Success - 200):**
```json
{
  "success": true,
  "message": "Token is valid",
  "data": null
}
```

**Response (Error - 401):**
```json
{
  "success": false,
  "message": "Token is invalid or expired",
  "data": null
}
```

### 4. Get User Info from Token
**Endpoint:** `GET /api/auth/user-info`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Response (Success - 200):**
```json
{
  "success": true,
  "message": "User info retrieved",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "type": "Bearer",
    "id": 1,
    "email": "user@example.com",
    "role": "ADMIN"
  }
}
```

## Configuration

### Database Setup
Update `application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/stitcho_db
spring.datasource.username=postgres
spring.datasource.password=postgres
```

### JWT Configuration
```properties
app.jwtSecret=mySecretKeyForJWTTokenGenerationAndValidationPurposeWithMinimumLength32Characters
app.jwtExpirationMs=86400000  # 24 hours in milliseconds
```

## Features

- ✅ User Registration with email validation
- ✅ User Login with email, password, and role
- ✅ JWT Token generation and validation
- ✅ Password encryption using BCrypt
- ✅ CORS support for frontend integration
- ✅ Input validation using Jakarta Validation
- ✅ Comprehensive error handling
- ✅ Logging with SLF4J

## Dependencies

- Spring Boot 4.0.0
- Spring Data JPA
- Spring Security
- PostgreSQL Driver
- JWT (JJWT 0.12.3)
- Lombok
- Jakarta Validation

## Running the Application

1. **Create PostgreSQL Database:**
```sql
CREATE DATABASE stitcho_db;
```

2. **Update Database Credentials:**
Edit `src/main/resources/application.properties` with your database credentials.

3. **Build and Run:**
```bash
mvn clean install
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## Frontend Integration Example

### Register
```javascript
const registerUser = async (email, password, role) => {
  const response = await fetch('http://localhost:8080/api/auth/register', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ email, password, role })
  });
  return await response.json();
};
```

### Login
```javascript
const loginUser = async (email, password, role) => {
  const response = await fetch('http://localhost:8080/api/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ email, password, role })
  });
  const data = await response.json();
  if (data.success) {
    localStorage.setItem('token', data.data.token);
  }
  return data;
};
```

### Use Token in Requests
```javascript
const fetchWithAuth = async (url, options = {}) => {
  const token = localStorage.getItem('token');
  const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`,
    ...options.headers
  };
  
  return fetch(url, { ...options, headers });
};
```

## Database Schema

### Users Table
```sql
CREATE TABLE users (
  id BIGSERIAL PRIMARY KEY,
  email VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  role VARCHAR(50) NOT NULL,
  created_at BIGINT NOT NULL
);
```

## Security Considerations

1. **JWT Secret:** Change the default JWT secret in production
2. **Password Encoding:** Passwords are encrypted using BCrypt
3. **CORS:** Configure allowed origins in `SecurityConfig.java` for production
4. **Token Expiration:** Default is 24 hours, adjust as needed
5. **HTTPS:** Use HTTPS in production

## Error Handling

All endpoints return a standardized response format:
```json
{
  "success": boolean,
  "message": "string",
  "data": object | null
}
```

## Validation Rules

### Register Request
- Email: Required, must be valid email format
- Password: Required, minimum 6 characters
- Role: Required, non-blank string

### Login Request
- Email: Required, must be valid email format
- Password: Required, non-blank
- Role: Required, must match registered role

## Testing with cURL

### Register
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123","role":"ADMIN"}'
```

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123","role":"ADMIN"}'
```

### Validate Token
```bash
curl -X GET http://localhost:8080/api/auth/validate \
  -H "Authorization: Bearer <your_token>"
```

## Troubleshooting

1. **Database Connection Error:** Ensure PostgreSQL is running and credentials are correct
2. **JWT Validation Error:** Check that the token hasn't expired and secret key matches
3. **CORS Error:** Verify CORS configuration in `SecurityConfig.java`
4. **Validation Error:** Check request body matches the required format

## Future Enhancements

- [ ] Refresh token implementation
- [ ] Email verification
- [ ] Password reset functionality
- [ ] Two-factor authentication
- [ ] Role-based access control (RBAC)
- [ ] User profile management
- [ ] Logout functionality with token blacklist
