# Postman API Examples

## Setup in Postman

1. Create a new collection called "Stitcho Auth API"
2. Create a new environment variable for the base URL:
   - Variable: `baseUrl`
   - Value: `http://localhost:8080`
3. Create another variable for storing JWT token:
   - Variable: `token`
   - Value: (will be set automatically)

## 1. Register User

**Request:**
```
POST {{baseUrl}}/auth/register
Content-Type: application/json

{
  "email": "admin@example.com",
  "password": "Admin@123",
  "role": "ADMIN"
}
```

**Tests Tab (Auto-save token):**
```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    pm.environment.set("token", jsonData.jwt);
}
```

**Expected Response (200 OK):**
```json
{
  "jwt": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbkBleGFtcGxlLmNvbSIsInJvbGUiOiJBRE1JTiIsInVzZXJJZCI6MSwiZXhwIjoxNzAxMzIwOTY3LCJpYXQiOjE3MDEyMzQ1Njd9.abc123...",
  "userId": 1,
  "email": "admin@example.com",
  "role": "ADMIN"
}
```

---

## 2. Register Another User (USER role)

**Request:**
```
POST {{baseUrl}}/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "User@123",
  "role": "USER"
}
```

**Expected Response (200 OK):**
```json
{
  "jwt": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 2,
  "email": "user@example.com",
  "role": "USER"
}
```

---

## 3. Login User

**Request:**
```
POST {{baseUrl}}/auth/login
Content-Type: application/json

{
  "email": "admin@example.com",
  "password": "Admin@123"
}
```

**Tests Tab:**
```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    pm.environment.set("token", jsonData.jwt);
}
```

**Expected Response (200 OK):**
```json
{
  "jwt": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "email": "admin@example.com",
  "role": "ADMIN"
}
```

---

## 4. Login with Wrong Password

**Request:**
```
POST {{baseUrl}}/auth/login
Content-Type: application/json

{
  "email": "admin@example.com",
  "password": "WrongPassword"
}
```

**Expected Response (500 Internal Server Error):**
```json
{
  "timestamp": "2023-11-29T10:30:00.000+00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Invalid credentials"
}
```

---

## 5. Access Public Endpoint (No Auth Required)

**Request:**
```
GET {{baseUrl}}/api/public
```

**Expected Response (200 OK):**
```json
"This is a public endpoint - no authentication required"
```

---

## 6. Access Protected Endpoint (Auth Required)

**Request:**
```
GET {{baseUrl}}/api/protected
Authorization: Bearer {{token}}
```

**Expected Response (200 OK):**
```json
"Hello admin@example.com! This is a protected endpoint"
```

---

## 7. Access Protected Endpoint Without Token

**Request:**
```
GET {{baseUrl}}/api/protected
```

**Expected Response (403 Forbidden):**
```
Access Denied
```

---

## 8. Access Admin-Only Endpoint (ADMIN role required)

**Request:**
```
GET {{baseUrl}}/api/admin-only
Authorization: Bearer {{token}}
```

**Expected Response (200 OK) - If user has ADMIN role:**
```json
"Admin access granted for admin@example.com"
```

**Expected Response (403 Forbidden) - If user has USER role:**
```
Access Denied
```

---

## 9. Get User Information

**Request:**
```
GET {{baseUrl}}/api/user-info
Authorization: Bearer {{token}}
```

**Expected Response (200 OK):**
```json
{
  "email": "admin@example.com",
  "roles": ["ROLE_ADMIN"]
}
```

---

## 10. Test with Expired Token

**Request:**
```
GET {{baseUrl}}/api/protected
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbkBleGFtcGxlLmNvbSIsInJvbGUiOiJBRE1JTiIsInVzZXJJZCI6MSwiZXhwIjoxNzAxMjM0NTY3LCJpYXQiOjE3MDEyMzQ1Njd9.invalidSignature
```

**Expected Response (403 Forbidden):**
```
Access Denied
```

---

## Error Scenarios

### Email Already Exists
**Request:**
```
POST {{baseUrl}}/auth/register
Content-Type: application/json

{
  "email": "admin@example.com",
  "password": "Admin@123",
  "role": "ADMIN"
}
```

**Expected Response (500 Internal Server Error):**
```json
{
  "message": "Email already exists"
}
```

### User Not Found
**Request:**
```
POST {{baseUrl}}/auth/login
Content-Type: application/json

{
  "email": "nonexistent@example.com",
  "password": "Password@123"
}
```

**Expected Response (500 Internal Server Error):**
```json
{
  "message": "User not found"
}
```

### Invalid Token Format
**Request:**
```
GET {{baseUrl}}/api/protected
Authorization: Bearer invalid_token_format
```

**Expected Response (403 Forbidden):**
```
Access Denied
```

---

## Postman Collection JSON

You can import this collection directly into Postman:

```json
{
  "info": {
    "name": "Stitcho Auth API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Auth",
      "item": [
        {
          "name": "Register User",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\"email\":\"admin@example.com\",\"password\":\"Admin@123\",\"role\":\"ADMIN\"}"
            },
            "url": {
              "raw": "{{baseUrl}}/auth/register",
              "host": ["{{baseUrl}}"],
              "path": ["auth", "register"]
            }
          }
        },
        {
          "name": "Login User",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\"email\":\"admin@example.com\",\"password\":\"Admin@123\"}"
            },
            "url": {
              "raw": "{{baseUrl}}/auth/login",
              "host": ["{{baseUrl}}"],
              "path": ["auth", "login"]
            }
          }
        }
      ]
    },
    {
      "name": "API",
      "item": [
        {
          "name": "Public Endpoint",
          "request": {
            "method": "GET",
            "url": {
              "raw": "{{baseUrl}}/api/public",
              "host": ["{{baseUrl}}"],
              "path": ["api", "public"]
            }
          }
        },
        {
          "name": "Protected Endpoint",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/protected",
              "host": ["{{baseUrl}}"],
              "path": ["api", "protected"]
            }
          }
        },
        {
          "name": "Admin Only",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/admin-only",
              "host": ["{{baseUrl}}"],
              "path": ["api", "admin-only"]
            }
          }
        },
        {
          "name": "User Info",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/user-info",
              "host": ["{{baseUrl}}"],
              "path": ["api", "user-info"]
            }
          }
        }
      ]
    }
  ]
}
```

---

## Testing Workflow

1. **Register Admin User** - Get JWT token
2. **Register Regular User** - Get JWT token
3. **Login** - Verify login works and get new token
4. **Access Public Endpoint** - Should work without token
5. **Access Protected Endpoint** - Should work with token
6. **Access Admin Endpoint** - Should work with ADMIN token, fail with USER token
7. **Get User Info** - Verify user details
8. **Test Error Cases** - Invalid credentials, expired token, etc.

---

## Tips

- Use environment variables for `baseUrl` and `token`
- Add tests to automatically save JWT tokens
- Use pre-request scripts to add headers automatically
- Test both success and error scenarios
- Verify response status codes and content
