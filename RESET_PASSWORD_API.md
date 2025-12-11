# Reset Password API - Simple Implementation

## Overview
Direct password reset endpoint. Frontend sends email and new password, backend validates email exists and updates password.

## API Endpoint

### Reset Password
**POST** `/auth/reset-password`

**Request:**
```json
{
  "email": "john@example.com",
  "newPassword": "newPassword123"
}
```

**Response (200 OK):**
```json
{
  "message": "Password reset successfully. You can now login with your new password.",
  "email": "john@example.com",
  "name": null,
  "role": null,
  "jwt": null
}
```

**Error Response (400 Bad Request):**
```json
{
  "message": "Email not found."
}
```

## Validation Rules

- **Email**: Must be valid email format and exist in database
- **New Password**: Minimum 6 characters, required field

## Password Reset Flow

```
User Submits Email + New Password
    ↓
System Validates Email Exists
    ↓
System Encodes Password (BCrypt)
    ↓
System Updates Password in Database
    ↓
Success Response
    ↓
User Can Login with New Password
```

## cURL Example

```bash
curl -X POST http://localhost:8080/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "newPassword": "newPassword123"
  }'
```

## Frontend Integration

```javascript
// Reset password function
async function resetPassword(email, newPassword) {
  try {
    const response = await fetch('http://localhost:8080/auth/reset-password', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        email: email,
        newPassword: newPassword
      })
    });

    const data = await response.json();
    
    if (response.ok) {
      console.log('Password reset successful:', data.message);
      // Redirect to login page
      window.location.href = '/login';
    } else {
      console.error('Error:', data.message);
    }
  } catch (error) {
    console.error('Request failed:', error);
  }
}
```

## Error Handling

| Error | Cause | Solution |
|-------|-------|----------|
| Email not found | User doesn't exist | Check email address |
| Invalid email format | Bad email format | Enter valid email |
| Password too short | < 6 characters | Use 6+ character password |

## Security Features

✅ Email validation
✅ Password encryption (BCrypt)
✅ Input validation
✅ Error handling

## Testing

1. **Request reset with valid email:**
```bash
curl -X POST http://localhost:8080/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{"email": "john@example.com", "newPassword": "newPassword123"}'
```

2. **Login with new password:**
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "newPassword123",
    "role": "USER"
  }'
```

## Files Modified

- `dto/ResetPasswordRequest.java` - New DTO with email and newPassword
- `service/AuthService.java` - Added resetPassword() method
- `controller/AuthController.java` - Added /reset-password endpoint
