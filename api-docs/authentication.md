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

**Response (Error - 400 Bad Request)**:
```json
{
  "message": "Refresh token is not in database!"
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
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huZG9lIiwiaWF0IjoxNjgwMTIzNDU2LCJleHAiOjE2ODAxMjcwNTZ9.abcdefghijklmnopqrstuvwxyz
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
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huZG9lIiwiaWF0IjoxNjgwMTIzNDU2LCJleHAiOjE2ODAxMjcwNTZ9.abcdefghijklmnopqrstuvwxyz
```

**Response (Success - 200 OK)**:
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john.doe@example.com", 
  "fullName": "John Doe",
  "role": "ROLE_USER"
}
```

## 9. Tạo người dùng mới (Quyền Admin)

**Endpoint**: `POST /api/v1/user/create`

**Request Header**:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTY4MDEyMzQ1NiwiZXhwIjoxNjgwMTI3MDU2fQ.abcdefghijklmnopqrstuvwxyz
```

**Request Body (Mẫu User)**:
```json
{
  "username": "user1",
  "password": "Password123!",
  "email": "user1@example.com",
  "fullName": "Regular User",
  "phone": "0987654321",
  "gender": "Female",
  "dateOfBirth": "1995-05-15",
  "address": "456 User Street, City",
  "nationalId": "987654321098",
  "role": {
    "id": 3,
    "name": "ROLE_USER",
    "description": "Regular user role"
  }
}
```

**Request Body (Mẫu Staff)**:
```json
{
  "username": "staff1",
  "password": "StaffPass123!",
  "email": "staff1@example.com",
  "fullName": "Hotel Staff",
  "phone": "0912345678",
  "gender": "Male",
  "dateOfBirth": "1992-08-20",
  "address": "789 Staff Road, City",
  "nationalId": "456789012345",
  "role": {
    "id": 2,
    "name": "ROLE_STAFF",
    "description": "Hotel staff role"
  }
}
```

**Response (Success - 201 Created)**:
```json
{
  "id": 2,
  "username": "staff1",
  "fullName": "Hotel Staff",
  "email": "staff1@example.com",
  "phone": "0912345678",
  "address": "789 Staff Road, City",
  "gender": "Male",
  "dateOfBirth": "1992-08-20",
  "nationalId": "456789012345",
  "createdAt": "15:30:22 - 27/03/2025",
  "updatedAt": null,
  "role": "ROLE_STAFF"
}
```

## 10. Cập nhật thông tin người dùng

**Endpoint**: `PUT /api/v1/user/update/{userId}`

**Request Header**:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huZG9lIiwiaWF0IjoxNjgwMTIzNDU2LCJleHAiOjE2ODAxMjcwNTZ9.abcdefghijklmnopqrstuvwxyz
```

**Request Body (Mẫu)**:
```json
{
  "fullName": "John Smith Doe",
  "email": "john.smith@example.com",
  "phone": "0987123456",
  "gender": "Male",
  "dateOfBirth": "1990-01-15",
  "address": "123 Updated Street, City",
  "nationalId": "123456789012"
}
```

**Response (Success - 201 Created)**:
```json
{
  "id": 1,
  "username": "johndoe",
  "fullName": "John Smith Doe",
  "email": "john.smith@example.com",
  "phone": "0987123456",
  "address": "123 Updated Street, City",
  "gender": "Male",
  "dateOfBirth": "1990-01-15",
  "nationalId": "123456789012",
  "createdAt": "10:15:30 - 26/03/2025",
  "updatedAt": "16:45:22 - 27/03/2025",
  "role": "ROLE_USER"
}
```

## 11. Xóa người dùng (Quyền Admin)

**Endpoint**: `DELETE /api/v1/users/{userId}`

**Request Header**:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTY4MDEyMzQ1NiwiZXhwIjoxNjgwMTI3MDU2fQ.abcdefghijklmnopqrstuvwxyz
```

**Response (Success - 200 OK)**:
```json
{
  "message": "User deleted successfully"
}
```

**Response (Error - 400 Bad Request)**:
```json
{
  "message": "User not found with id: 999"
}
```

## 12. Lấy danh sách người dùng (Quyền Admin hoặc Staff)

**Endpoint**: `GET /api/v1/users`

**Request Header**:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTY4MDEyMzQ1NiwiZXhwIjoxNjgwMTI3MDU2fQ.abcdefghijklmnopqrstuvwxyz
```

**Response (Success - 200 OK)**:
```json
[
  {
    "id": 1,
    "username": "johndoe",
    "fullName": "John Smith Doe",
    "email": "john.smith@example.com",
    "phone": "0987123456",
    "address": "123 Updated Street, City",
    "gender": "Male",
    "dateOfBirth": "1990-01-15",
    "nationalId": "123456789012",
    "createdAt": "10:15:30 - 26/03/2025",
    "updatedAt": "16:45:22 - 27/03/2025",
    "role": "ROLE_USER"
  },
  {
    "id": 2,
    "username": "staff1",
    "fullName": "Hotel Staff",
    "email": "staff1@example.com",
    "phone": "0912345678",
    "address": "789 Staff Road, City",
    "gender": "Male",
    "dateOfBirth": "1992-08-20",
    "nationalId": "456789012345",
    "createdAt": "15:30:22 - 27/03/2025",
    "updatedAt": null,
    "role": "ROLE_STAFF"
  }
]
```

## 13. Phân quyền và Vai trò

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

## 14. Dữ liệu khởi tạo

### Vai trò (Roles)
```sql
INSERT INTO ROLES (id, name, description) VALUES (1, 'ROLE_ADMIN', 'Administrator role');
INSERT INTO ROLES (id, name, description) VALUES (2, 'ROLE_STAFF', 'Hotel staff role');
INSERT INTO ROLES (id, name, description) VALUES (3, 'ROLE_USER', 'Regular user role');
```

### Tài khoản Admin mặc định
```sql
-- Mật khẩu: Admin123! (đã được mã hóa)
INSERT INTO USERS (username, password_hash, email, full_name, phone_number, role_id, created_at) 
VALUES ('admin', '$2a$10$X7DvfcJ8vZVwI7ZvZgVGvOaDgxpCmLJA7tNI1zK.f2bRUGrpFZkYe', 'admin@hotel.com', 'Hotel Admin', '0123456789', 1, NOW());
```

## 15. Lưu ý về bảo mật

1. Mật khẩu được mã hóa bằng BCryptPasswordEncoder trước khi lưu vào cơ sở dữ liệu
2. Access token có thời hạn 1 giờ
3. Refresh token có thời hạn 10 phút
4. Token đặt lại mật khẩu có thời hạn 24 giờ
5. Tài khoản admin mặc định được tạo trong quá trình khởi tạo hệ thống

## 16. Cấu hình Email cho quên mật khẩu

Để chức năng quên mật khẩu hoạt động, cần cấu hình SMTP trong file `application.properties`:

```properties
# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

**Lưu ý**: Khi sử dụng Gmail, bạn cần tạo "App Password" từ tài khoản Google.
