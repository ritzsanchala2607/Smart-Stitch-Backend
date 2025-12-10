# Quick Start Guide - Authentication System

## Prerequisites
- Java 17+
- PostgreSQL database
- Maven
- Postman or curl for testing

## Setup Steps

### 1. Configure Database
Update `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/stitcho_db
    username: postgres
    password: your_password
```

### 2. Build Project
```bash
mvn clean install
```

### 3. Run Application
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## Test the API

### 1. Register a New User
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "Password@123",
    "role": "ADMIN"
  }'
```

**Response:**
```json
{
  "jwt": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "email": "user@example.com",
  "role": "ADMIN"
}
```

### 2. Login
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "Password@123"
  }'
```

### 3. Access Protected Endpoint
Copy the JWT token from the response and use it:

```bash
curl -X GET http://localhost:8080/api/protected \
  -H "Authorization: Bearer <your_jwt_token>"
```

### 4. Access Admin-Only Endpoint
```bash
curl -X GET http://localhost:8080/api/admin-only \
  -H "Authorization: Bearer <your_jwt_token>"
```

### 5. Get User Info
```bash
curl -X GET http://localhost:8080/api/user-info \
  -H "Authorization: Bearer <your_jwt_token>"
```

## File Structure

```
src/main/java/com/stitcho/beta/
├── config/
│   └── SecurityConfig.java          # Spring Security configuration
├── controller/
│   ├── AuthController.java          # Auth endpoints
│   └── SampleController.java        # Sample protected endpoints
├── dto/
│   ├── LoginRequestDto.java
│   ├── LoginResponseDto.java
│   └── RegisterRequestDto.java
├── entity/
│   └── User.java                    # User entity
├── repository/
│   └── UserRepository.java          # Database access
├── security/
│   └── JwtAuthenticationFilter.java # JWT validation filter
├── service/
│   └── AuthService.java             # Business logic
└── util/
    └── JwtUtil.java                 # JWT utilities
```

## Key Features

✅ User Registration with email, password, and role
✅ User Login with JWT token generation
✅ Password hashing using BCrypt
✅ JWT token validation on protected endpoints
✅ Role-based access control
✅ Stateless authentication (no sessions)
✅ CSRF protection disabled for JWT

## Default Roles

You can use any role name. Common examples:
- `ADMIN` - Administrator access
- `USER` - Regular user access
- `MODERATOR` - Moderator access
- `GUEST` - Guest access

## Important Notes

1. **Change JWT Secret**: Update the secret in `application.yml` for production
2. **Database**: Ensure PostgreSQL is running before starting the app
3. **Token Expiration**: Default is 24 hours (86400000 ms)
4. **Password Requirements**: Minimum 6 characters (can be customized in User entity)

## Troubleshooting

### Port Already in Use
Change the port in `application.yml`:
```yaml
server:
  port: 8081
```

### Database Connection Error
- Verify PostgreSQL is running
- Check database credentials in `application.yml`
- Ensure database `stitcho_db` exists

### JWT Token Errors
- Token may be expired (24 hours)
- Verify token format: `Authorization: Bearer <token>`
- Check if secret key matches

## Next Steps

1. Read `AUTH_SETUP.md` for detailed documentation
2. Customize roles and permissions as needed
3. Add more protected endpoints using `@PreAuthorize` annotation
4. Implement refresh tokens for better security
5. Add email verification for registration

## Support

For more details, see:
- `AUTH_SETUP.md` - Complete authentication documentation
- `SampleController.java` - Example protected endpoints
