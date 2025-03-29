# API Quản lý người dùng dành cho Admin

File tài liệu này mô tả các API dành riêng cho Admin để quản lý người dùng trong hệ thống.

## Yêu cầu xác thực

Tất cả các API trong tài liệu này **đều yêu cầu quyền Admin (`ROLE_ADMIN`)** và cần gửi kèm JWT token hợp lệ của người dùng có quyền Admin trong header `Authorization`.

**Header xác thực**:
```
Authorization: Bearer <your_admin_jwt_token>
```

**Lưu ý quan trọng**: Nếu bạn nhận được lỗi `403 Forbidden` khi gọi các API này, điều đó có nghĩa là JWT token bạn đang sử dụng không thuộc về một người dùng có vai trò `ADMIN` hoặc token không hợp lệ/hết hạn.

## 1. Lấy danh sách người dùng

**Endpoint**: `GET /api/v1/users`

**Mô tả**: Lấy danh sách tất cả người dùng trong hệ thống. **Chỉ dành cho Admin**.

**Request Header**:
```
Authorization: Bearer <your_admin_jwt_token>
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

**Response (Error - 403 Forbidden)**: _(Khi token không có quyền Admin)_ 
```json
{
  "timestamp": "2023-10-27T10:30:00.123+00:00", // Ví dụ
  "status": 403,
  "error": "Forbidden",
  "message": "Forbidden", // Hoặc thông báo cụ thể hơn tùy cấu hình
  "path": "/api/v1/users"
}
```

**Response (Error - 401 Unauthorized)**: _(Khi token không hợp lệ hoặc thiếu)_ 
```json
{
  "timestamp": "2023-10-27T10:30:00.123+00:00", // Ví dụ
  "status": 401,
  "error": "Unauthorized",
  "message": "Unauthorized", // Hoặc thông báo cụ thể hơn
  "path": "/api/v1/users"
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

**Mô tả**: Lấy thông tin chi tiết của một người dùng dựa trên ID. **Chỉ dành cho Admin**.

**Request Header**:
```
Authorization: Bearer <your_admin_jwt_token>
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

**Response (Error - 403 Forbidden)**: _(Khi token không có quyền Admin)_ 
```json
{
  "timestamp": "2023-10-27T10:31:00.456+00:00", // Ví dụ
  "status": 403,
  "error": "Forbidden",
  "message": "Forbidden",
  "path": "/api/v1/users/999"
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

**Mô tả**: Tạo một người dùng mới trong hệ thống. **Chỉ dành cho Admin**.

**Request Header**:
```
Authorization: Bearer <your_admin_jwt_token>
Content-Type: application/json
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
  "dateOfBirth": "1992-03-15", // Định dạng YYYY-MM-DD
  "address": "789 New Street, City",
  "nationalId": "456789123012",
  "role": {
    "id": 3, // ID của Role (ví dụ: 1: ADMIN, 2: USER, 3: STAFF)
    "name": "ROLE_STAFF" // Tên Role
  }
}
```

**Lưu ý**: 
*   Trường `role` trong request body là một object chứa `id` và `name` của Role muốn gán. Bạn cần đảm bảo Role này tồn tại trong database.
*   Nếu không cung cấp trường `role`, người dùng sẽ được tạo với vai trò mặc định là `ROLE_USER`.

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
  "createdAt": "14:25:10 - 29/03/2023", // Ví dụ
  "updatedAt": null,
  "role": "ROLE_STAFF"
}
```

**Response (Error - 400 Bad Request)**: _(Ví dụ: username/email đã tồn tại, sai định dạng ngày sinh, role không tồn tại)_ 
```json
{
  "message": "Username already exists" 
  // Hoặc "Email already exists"
  // Hoặc "Invalid date format. Please use YYYY-MM-DD format"
  // Hoặc "Role not found: ROLE_INVALID"
}
```

**Response (Error - 403 Forbidden)**: _(Khi token không có quyền Admin)_ 
```json
{
  "timestamp": "2023-10-27T10:32:00.789+00:00", // Ví dụ
  "status": 403,
  "error": "Forbidden",
  "message": "Forbidden",
  "path": "/api/v1/user/create"
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

**Mô tả**: Cập nhật thông tin của một người dùng dựa trên ID. **Chỉ dành cho Admin**.

**Request Header**:
```
Authorization: Bearer <your_admin_jwt_token>
Content-Type: application/json
```

**Request Body**: _(Chỉ chứa các trường cần cập nhật)_ 
```json
{
  "fullName": "John Smith Doe Updated",
  "email": "john.smith.updated@example.com",
  "phone": "0987123456",
  "gender": "Male",
  "dateOfBirth": "1990-01-15", // Định dạng YYYY-MM-DD
  "address": "123 Updated Street, City",
  "nationalId": "123456789012",
  "role": {
      "id": 2, 
      "name": "ROLE_USER"
  } // Có thể cập nhật cả Role
}
```

**Lưu ý**: 
*   Bạn không thể cập nhật `username`.
*   Trường `role` nếu có sẽ cập nhật vai trò của người dùng.

**Response (Success - 200 OK)**:
```json
{
  "id": 2,
  "username": "johndoe",
  "fullName": "John Smith Doe Updated",
  "email": "john.smith.updated@example.com",
  "phone": "0987123456",
  "address": "123 Updated Street, City",
  "gender": "Male",
  "dateOfBirth": "1990-01-15",
  "nationalId": "123456789012",
  "createdAt": "15:30:45 - 27/03/2023",
  "updatedAt": "16:45:22 - 29/03/2023", // Ví dụ
  "role": "ROLE_USER"
}
```

**Response (Error - 404 Not Found)**:
```json
{
  "message": "User not found with id: 999"
}
```

**Response (Error - 400 Bad Request)**: _(Ví dụ: sai định dạng dữ liệu)_ 
```json
{
  "message": "Invalid date format. Please use YYYY-MM-DD format"
}
```

**Response (Error - 403 Forbidden)**: _(Khi token không có quyền Admin)_ 
```json
{
  "timestamp": "2023-10-27T10:33:00.111+00:00", // Ví dụ
  "status": 403,
  "error": "Forbidden",
  "message": "Forbidden",
  "path": "/api/v1/user/update/2"
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

**Mô tả**: Xóa một người dùng khỏi hệ thống dựa trên ID. **Chỉ dành cho Admin**.

**Request Header**:
```
Authorization: Bearer <your_admin_jwt_token>
```

**Response (Success - 200 OK)**:
```json
{
  "message": "Xóa người dùng thành công"
}
```

**Response (Error - 404 Not Found)**:
```json
{
  "message": "User not found with id: 999"
}
```

**Response (Error - 403 Forbidden)**: _(Khi token không có quyền Admin)_ 
```json
{
  "timestamp": "2023-10-27T10:34:00.222+00:00", // Ví dụ
  "status": 403,
  "error": "Forbidden",
  "message": "Forbidden",
  "path": "/api/v1/users/999"
}
```

**Response (Error - 500 Internal Server Error)**:
```json
{
  "message": "Lỗi khi xóa người dùng: [chi tiết lỗi]"
}
```

**Lưu ý chung**: Khi xóa người dùng, hệ thống sẽ tự động xóa các refresh token và password reset token liên quan đến người dùng đó. 