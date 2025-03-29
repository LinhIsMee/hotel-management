# API Quản lý người dùng dành cho Admin

File tài liệu này mô tả các API dành riêng cho Admin để quản lý người dùng trong hệ thống.

## Yêu cầu xác thực

Tất cả các API trong tài liệu này đều yêu cầu quyền Admin và cần gửi kèm JWT token trong header.

**Header xác thực**:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

## 1. Lấy danh sách người dùng

**Endpoint**: `GET /api/v1/users`

**Mô tả**: Lấy danh sách tất cả người dùng trong hệ thống.

**Request Header**:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response (Success - 200 OK)**:
```json
[
  {
    "id": 1,
    "username": "admin",
    "fullName": "Hotel Admin",
    "email": "admin@hotel.com",
    "phone": "0123456789",
    "address": "123 Admin Street, City",
    "gender": "Male",
    "dateOfBirth": "1990-01-01",
    "nationalId": "123456789012",
    "createdAt": "10:15:30 - 26/03/2023",
    "updatedAt": null,
    "role": "ROLE_ADMIN"
  },
  {
    "id": 2,
    "username": "johndoe",
    "fullName": "John Doe",
    "email": "john.doe@example.com",
    "phone": "0987654321",
    "address": "456 User Street, City",
    "gender": "Male",
    "dateOfBirth": "1995-05-10",
    "nationalId": "987654321012",
    "createdAt": "15:30:45 - 27/03/2023",
    "updatedAt": "09:20:15 - 28/03/2023",
    "role": "ROLE_USER"
  }
]
```

**Response (Error - 403 Forbidden)**:
```json
{
  "message": "Bạn không có quyền truy cập danh sách người dùng"
}
```

**Response (Error - 500 Internal Server Error)**:
```json
{
  "message": "Lỗi khi lấy danh sách người dùng: [chi tiết lỗi]"
}
```

## 2. Lấy thông tin người dùng theo ID

**Endpoint**: `GET /api/v1/users/{userId}`

**Mô tả**: Lấy thông tin chi tiết của một người dùng dựa trên ID.

**Request Header**:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response (Success - 200 OK)**:
```json
{
  "id": 2,
  "username": "johndoe",
  "fullName": "John Doe",
  "email": "john.doe@example.com",
  "phone": "0987654321",
  "address": "456 User Street, City",
  "gender": "Male",
  "dateOfBirth": "1995-05-10",
  "nationalId": "987654321012",
  "createdAt": "15:30:45 - 27/03/2023",
  "updatedAt": "09:20:15 - 28/03/2023",
  "role": "ROLE_USER"
}
```

**Response (Error - 404 Not Found)**:
```json
{
  "message": "Không tìm thấy người dùng với ID: 999"
}
```

**Response (Error - 403 Forbidden)**:
```json
{
  "message": "Bạn không có quyền truy cập thông tin người dùng"
}
```

**Response (Error - 500 Internal Server Error)**:
```json
{
  "message": "Lỗi khi lấy thông tin người dùng: [chi tiết lỗi]"
}
```

## 3. Tạo người dùng mới

**Endpoint**: `POST /api/v1/user/create`

**Mô tả**: Tạo một người dùng mới trong hệ thống.

**Request Header**:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Request Body**:
```json
{
  "username": "newuser",
  "password": "Password123!",
  "fullName": "New User",
  "email": "new.user@example.com",
  "phone": "0123456789",
  "gender": "Female",
  "dateOfBirth": "1992-03-15",
  "address": "789 New Street, City",
  "nationalId": "456789123012",
  "role": {
    "name": "ROLE_STAFF"
  }
}
```

**Lưu ý**: Tham số `role` là một object với thuộc tính name có thể là "ROLE_ADMIN", "ROLE_STAFF" hoặc "ROLE_USER". Nếu không cung cấp, mặc định sẽ là "ROLE_USER".

**Response (Success - 201 Created)**:
```json
{
  "id": 3,
  "username": "newuser",
  "fullName": "New User",
  "email": "new.user@example.com",
  "phone": "0123456789",
  "address": "789 New Street, City",
  "gender": "Female",
  "dateOfBirth": "1992-03-15",
  "nationalId": "456789123012",
  "createdAt": "14:25:10 - 29/03/2023",
  "updatedAt": null,
  "role": "ROLE_STAFF"
}
```

**Response (Error - 500 Internal Server Error)**:
```json
{
  "message": "Lỗi khi tạo người dùng mới: [chi tiết lỗi]"
}
```

## 4. Cập nhật thông tin người dùng

**Endpoint**: `PUT /api/v1/user/update/{userId}`

**Mô tả**: Cập nhật thông tin của một người dùng dựa trên ID.

**Request Header**:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Request Body**:
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

**Response (Success - 200 OK)**:
```json
{
  "id": 2,
  "username": "johndoe",
  "fullName": "John Smith Doe",
  "email": "john.smith@example.com",
  "phone": "0987123456",
  "address": "123 Updated Street, City",
  "gender": "Male",
  "dateOfBirth": "1990-01-15",
  "nationalId": "123456789012",
  "createdAt": "15:30:45 - 27/03/2023",
  "updatedAt": "16:45:22 - 29/03/2023",
  "role": "ROLE_USER"
}
```

**Response (Error - 500 Internal Server Error)**:
```json
{
  "message": "Lỗi khi cập nhật thông tin người dùng: [chi tiết lỗi]"
}
```

## 5. Xóa người dùng

**Endpoint**: `DELETE /api/v1/users/{userId}`

**Mô tả**: Xóa một người dùng khỏi hệ thống dựa trên ID.

**Request Header**:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response (Success - 200 OK)**:
```json
{
  "message": "Xóa người dùng thành công"
}
```

**Response (Error - 500 Internal Server Error)**:
```json
{
  "message": "Lỗi khi xóa người dùng: [chi tiết lỗi]"
}
```

**Lưu ý**: Khi xóa người dùng, hệ thống sẽ tự động xóa các refresh token và password reset token liên quan đến người dùng đó để tránh lỗi foreign key constraint. 