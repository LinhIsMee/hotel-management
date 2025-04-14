# API Authentication Documentation

## 1. Đăng ký tài khoản (Register)

**Endpoint**: `POST /api/v1/register`

**Request Body (Mẫu)**:
```json
{
  "username": "johndoe",
  "password": "Password123!",
  "email": "john.doe@example.com",
  "fullName": "John Doe",
  "phoneNumber": "0987654321",
  "gender": "Male",
  "dateOfBirth": "1990-01-01",
  "address": "123 Main Street, City",
  "nationalId": "123456789012"
}
```

**Response (Success - 201 Created)**:
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john.doe@example.com",
  "fullName": "John Doe",
  "role": "ROLE_USER"
}
```

**Lưu ý**: Khi đăng ký tài khoản mới, vai trò mặc định là "ROLE_USER". Chỉ admin mới có quyền tạo tài khoản với vai trò admin hoặc nhân viên.

**Response (Error - 400 Bad Request)**:
```json
{
  "message": "Username already exists"
}
```

## 2. Đăng nhập (Login)

**Endpoint**: `POST /api/v1/login`

**Request Body (Mẫu)**:
```json
{
  "username": "johndoe",
  "password": "Password123!"
}
```

**Response (Success - 200 OK)**:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huZG9lIiwiaWF0IjoxNjgwMTIzNDU2LCJleHAiOjE2ODAxMjcwNTZ9.abcdefghijklmnopqrstuvwxyz",
  "token": "550e8400-e29b-41d4-a716-446655440000",
  "userId": 1,
  "role": "ROLE_USER"
}
```

**Response (Error - 401 Unauthorized)**:
```
Authentication failed
```

## 3. Refresh Token

**Endpoint**: `POST /api/v1/refresh-token`

**Request Body (Mẫu)**:
```json
{
  "token": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response (Success - 200 OK)**:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huZG9lIiwiaWF0IjoxNjgwMTIzNDU2LCJleHAiOjE2ODAxMjcwNTZ9.abcdefghijklmnopqrstuvwxyz",
  "token": "550e8400-e29b-41d4-a716-446655440000",
  "userId": 1,
  "role": "ROLE_USER"
}
```

**Response (Error - 403 Forbidden)**:
```json
{
  "message": "Refresh token không tồn tại trong cơ sở dữ liệu"
}
```

## 4. Validate Token

**Endpoint**: `POST /api/v1/validate-token`

**Request Body (Mẫu)**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huZG9lIiwiaWF0IjoxNjgwMTIzNDU2LCJleHAiOjE2ODAxMjcwNTZ9.abcdefghijklmnopqrstuvwxyz"
}
```

**Response (Success - 200 OK)**:
```json
{
  "valid": true,
  "username": "johndoe"
}
```

**Response (Invalid Token - 200 OK)**:
```json
{
  "valid": false,
  "username": null
}
```

## 5. Quên mật khẩu (Forgot Password)

**Endpoint**: `POST /api/v1/forgot-password`

**Request Body (Mẫu)**:
```json
{
  "email": "john.doe@example.com"
}
```

**Response (Success - 200 OK)**:
```json
{
  "message": "Hướng dẫn đặt lại mật khẩu đã được gửi đến email của bạn"
}
```

**Response (Error - 404 Not Found)**:
```json
{
  "message": "Không tìm thấy email"
}
```

## 6. Đặt lại mật khẩu (Reset Password)

**Endpoint**: `POST /api/v1/reset-password`

**Request Body (Mẫu)**:
```json
{
  "token": "550e8400-e29b-41d4-a716-446655440000",
  "newPassword": "NewPassword456!"
}
```

**Response (Success - 200 OK)**:
```json
{
  "message": "Mật khẩu đã được đặt lại thành công"
}
```

**Response (Error - 403 Forbidden)**:
```json
{
  "message": "Token không hợp lệ hoặc đã hết hạn"
}
```

## 7. Đăng xuất (Logout)

**Endpoint**: `POST /api/v1/logout`

**Request Header**:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huZG9lIiwiaWF0IjoxNjgwMTIzNDU2LCJleHAiOjE2ODAxMjcwNTZ9.abcdefghijklmnopqrstuvwxyz
```

**Response (Success - 200 OK)**:
```json
{
  "message": "Đăng xuất thành công"
}
```

## 8. Lấy thông tin người dùng hiện tại (Get Current User Profile)

**Endpoint**: `GET /api/v1/user/profile`

**Request Header**:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huZG9lIiwiaWF0IjoxNjgwMTIzNDU2LCJleHAiOjE2ODAxMjcwNTZ9.abcdefghijklmnopqrstuvwxyz
```

**Response (Success - 200 OK)**:
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john.doe@example.com",
  "fullName": "John Doe",
  "phoneNumber": "0987654321",
  "gender": "Male",
  "dateOfBirth": "1990-01-01",
  "address": "123 Main Street, City",
  "nationalId": "123456789012",
  "role": "ROLE_USER",
  "profileImage": "https://example.com/images/profile/johndoe.jpg",
  "createdAt": "2023-01-01T10:00:00",
  "updatedAt": "2023-01-15T14:30:00"
}
```

## 9. Đổi mật khẩu (Change Password)

**Endpoint**: `POST /api/v1/user/change-password`

**Request Header**:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huZG9lIiwiaWF0IjoxNjgwMTIzNDU2LCJleHAiOjE2ODAxMjcwNTZ9.abcdefghijklmnopqrstuvwxyz
```

**Request Body (Mẫu)**:
```json
{
  "oldPassword": "Password123!",
  "newPassword": "NewPassword456!"
}
```

**Response (Success - 200 OK)**:
```json
{
  "message": "Đổi mật khẩu thành công"
}
```

**Response (Error - 400 Bad Request)**:
```json
{
  "message": "Mật khẩu cũ không chính xác"
}
```
