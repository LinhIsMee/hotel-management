# API Authentication Documentation

## 1. Đăng ký tài khoản (Register)

**Endpoint**: `POST /api/v1/register`

**Request Body**:
```json
{
  "username": "string",
  "password": "string",
  "email": "string",
  "fullName": "string",
  "phoneNumber": "string",
  "gender": "string",
  "dateOfBirth": "yyyy-MM-dd",
  "address": "string",
  "nationalId": "string"
}
```

**Response (Success - 201 Created)**:
```json
{
  "id": 1,
  "username": "string",
  "email": "string",
  "fullName": "string",
  "role": "ROLE_USER"
}
```

**Response (Error - 400 Bad Request)**:
```json
{
  "message": "Username already exists"
}
```

## 2. Đăng nhập (Login)

**Endpoint**: `POST /api/v1/login`

**Request Body**:
```json
{
  "username": "string",
  "password": "string"
}
```

**Response (Success - 200 OK)**:
```json
{
  "accessToken": "string",
  "token": "string (refresh token)",
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

**Request Body**:
```json
{
  "token": "string (refresh token)"
}
```

**Response (Success - 200 OK)**:
```json
{
  "accessToken": "string",
  "token": "string (refresh token)",
  "userId": 1,
  "role": "ROLE_USER"
}
```

**Response (Error - 400 Bad Request)**:
```json
{
  "message": "Refresh token is not in database!"
}
```

## 4. Validate Token

**Endpoint**: `POST /api/v1/validate-token`

**Request Body**:
```json
{
  "token": "string (access token)"
}
```

**Response (Success - 200 OK)**:
```json
{
  "valid": true,
  "username": "string"
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

**Request Body**:
```json
{
  "email": "string"
}
```

**Response (Success - 200 OK)**:
```json
{
  "message": "Password reset instructions sent to your email"
}
```

**Response (Error - 400 Bad Request)**:
```json
{
  "message": "Email not found"
}
```

## 6. Đặt lại mật khẩu (Reset Password)

**Endpoint**: `POST /api/v1/reset-password`

**Request Body**:
```json
{
  "token": "string",
  "newPassword": "string"
}
```

**Response (Success - 200 OK)**:
```json
{
  "message": "Password has been reset successfully"
}
```

**Response (Error - 400 Bad Request)**:
```json
{
  "message": "Invalid or expired token"
}
```

## 7. Đăng xuất (Logout)

**Endpoint**: `POST /api/v1/logout`

**Request Header**:
```
Authorization: Bearer {accessToken}
```

**Response (Success - 200 OK)**:
```json
{
  "message": "Logged out successfully"
}
```

## 8. Lấy thông tin người dùng (Get User Profile)

**Endpoint**: `POST /api/v1/user`

**Request Header**:
```
Authorization: Bearer {accessToken}
```

**Response (Success - 200 OK)**:
```json
{
  "id": 1,
  "username": "string",
  "email": "string",
  "fullName": "string",
  "role": "ROLE_USER"
}
```

## 9. Phân quyền và Vai trò

Hệ thống có 3 vai trò (roles):

1. **ROLE_ADMIN**: Có quyền cao nhất, có thể thực hiện tất cả các thao tác
   - Quản lý người dùng (xem, thêm, sửa, xóa)
   - Quản lý đặt phòng
   - Quản lý phòng và loại phòng
   - Quản lý đánh giá
   - Xem báo cáo thống kê

2. **ROLE_STAFF**: Nhân viên khách sạn
   - Xem danh sách người dùng
   - Quản lý đặt phòng (xem, thêm, sửa, không được xóa)
   - Quản lý phòng (xem, cập nhật trạng thái)
   - Xem đánh giá

3. **ROLE_USER**: Người dùng đăng ký thông thường
   - Quản lý thông tin cá nhân
   - Đặt phòng
   - Xem lịch sử đặt phòng cá nhân
   - Đánh giá

### Các API yêu cầu quyền Admin

- `GET /api/v1/users` - Xem danh sách người dùng (Admin, Staff)
- `POST /api/v1/user/create` - Tạo người dùng mới (Admin)
- `DELETE /api/v1/users/{userId}` - Xóa người dùng (Admin)

## 10. Lưu ý về bảo mật

1. Mật khẩu được mã hóa bằng BCryptPasswordEncoder trước khi lưu vào cơ sở dữ liệu
2. Access token có thời hạn 1 giờ
3. Refresh token có thời hạn 10 phút
4. Token đặt lại mật khẩu có thời hạn 24 giờ
5. Tài khoản admin mặc định được tạo trong quá trình khởi tạo hệ thống
