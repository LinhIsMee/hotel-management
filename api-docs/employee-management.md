# API Quản lý nhân viên khách sạn

File tài liệu này mô tả các API dành cho quản lý nhân viên trong hệ thống.

## Yêu cầu xác thực

Tất cả các API trong tài liệu này **đều yêu cầu quyền Admin (`ROLE_ADMIN`)** và cần gửi kèm JWT token hợp lệ của người dùng có quyền Admin trong header `Authorization`.

**Header xác thực**:
```
Authorization: Bearer <your_admin_jwt_token>
```

**Lưu ý quan trọng**: Nếu bạn nhận được lỗi `403 Forbidden` khi gọi các API này, điều đó có nghĩa là JWT token bạn đang sử dụng không thuộc về một người dùng có vai trò `ADMIN` hoặc token không hợp lệ/hết hạn.

## Cấu trúc dữ liệu

### Department (Phòng ban)
```
MANAGEMENT   - Ban quản lý
FRONT_DESK   - Lễ tân
RESTAURANT   - Nhà hàng
HOUSEKEEPING - Dọn phòng
SECURITY     - Bảo vệ
TECHNICAL    - Kỹ thuật
SPA          - Spa
```

### Position (Vị trí)
```
MANAGER      - Quản lý
SUPERVISOR   - Giám sát
RECEPTIONIST - Nhân viên lễ tân
SERVER       - Phục vụ
CHEF         - Đầu bếp
CLEANING     - Dọn phòng
SECURITY     - Bảo vệ
THERAPIST    - Nhân viên spa
```

## 1. Lấy danh sách nhân viên

**Endpoint**: `GET /api/v1/employees`

**Mô tả**: Lấy danh sách tất cả nhân viên trong hệ thống. **Chỉ dành cho Admin**.

**Tham số truy vấn**:
- `department` (tùy chọn): Lọc theo phòng ban (ví dụ: FRONT_DESK, RESTAURANT, ...)
- `position` (tùy chọn): Lọc theo vị trí (ví dụ: MANAGER, RECEPTIONIST, ...)
- `status` (tùy chọn): Lọc theo trạng thái (true: đang làm việc, false: đã nghỉ việc)

**Request Header**:
```
Authorization: Bearer <your_admin_jwt_token>
```

**Response (Success - 200 OK)**:
```json
{
  "data": [
    {
      "id": 1,
      "name": "Nguyễn Thị Anh",
      "email": "nguyenthianh@hotel.com",
      "phone": "0901122334",
      "department": "MANAGEMENT",
      "position": "MANAGER",
      "joinDate": "2022-01-10",
      "status": true
    },
    {
      "id": 2,
      "name": "Trần Văn Bình",
      "email": "tranvanbinh@hotel.com",
      "phone": "0912233445",
      "department": "FRONT_DESK",
      "position": "RECEPTIONIST",
      "joinDate": "2022-02-15",
      "status": true
    }
    // ... thêm nhân viên khác
  ]
}
```

**Response (Error - 403 Forbidden)**: _(Khi token không có quyền Admin)_ 
```json
{
  "message": "Bạn không có quyền truy cập danh sách nhân viên"
}
```

**Response (Error - 400 Bad Request)**: _(Khi tham số không hợp lệ)_ 
```json
{
  "message": "Invalid department: INVALID_DEPARTMENT" 
}
```

**Response (Error - 500 Internal Server Error)**:
```json
{
  "message": "Lỗi khi lấy danh sách nhân viên: [chi tiết lỗi]"
}
```

## 2. Lấy thông tin nhân viên theo ID

**Endpoint**: `GET /api/v1/employees/{employeeId}`

**Mô tả**: Lấy thông tin chi tiết của một nhân viên dựa trên ID. **Chỉ dành cho Admin**.

**Request Header**:
```
Authorization: Bearer <your_admin_jwt_token>
```

**Response (Success - 200 OK)**:
```json
{
  "id": 1,
  "name": "Nguyễn Thị Anh",
  "email": "nguyenthianh@hotel.com",
  "phone": "0901122334",
  "department": "MANAGEMENT",
  "position": "MANAGER",
  "joinDate": "2022-01-10",
  "status": true
}
```

**Response (Error - 404 Not Found)**:
```json
{
  "message": "Không tìm thấy nhân viên với ID: 999"
}
```

**Response (Error - 403 Forbidden)**:
```json
{
  "message": "Bạn không có quyền truy cập thông tin nhân viên"
}
```

**Response (Error - 500 Internal Server Error)**:
```json
{
  "message": "Lỗi khi lấy thông tin nhân viên: [chi tiết lỗi]"
}
```

## 3. Tạo nhân viên mới

**Endpoint**: `POST /api/v1/employees`

**Mô tả**: Tạo một nhân viên mới trong hệ thống. **Chỉ dành cho Admin**.

**Request Header**:
```
Authorization: Bearer <your_admin_jwt_token>
Content-Type: application/json
```

**Request Body**:
```json
{
  "name": "Lê Thị Hương",
  "email": "lethihuong@hotel.com",
  "phone": "0912345678",
  "department": "FRONT_DESK",
  "position": "RECEPTIONIST",
  "joinDate": "2023-01-15",
  "status": true
}
```

**Lưu ý**: 
- `name`, `email`, `department`, `position` và `joinDate` là bắt buộc
- `department` phải là một trong các giá trị đã định nghĩa
- `position` phải là một trong các giá trị đã định nghĩa
- `joinDate` phải có định dạng YYYY-MM-DD
- `status` mặc định là `true` nếu không được cung cấp

**Response (Success - 201 Created)**:
```json
{
  "id": 21,
  "name": "Lê Thị Hương",
  "email": "lethihuong@hotel.com",
  "phone": "0912345678",
  "department": "FRONT_DESK",
  "position": "RECEPTIONIST",
  "joinDate": "2023-01-15",
  "status": true
}
```

**Response (Error - 400 Bad Request)**:
```json
{
  "message": "Email already exists"
}
```

**Response (Error - 403 Forbidden)**:
```json
{
  "message": "Bạn không có quyền tạo nhân viên mới"
}
```

**Response (Error - 500 Internal Server Error)**:
```json
{
  "message": "Lỗi khi tạo nhân viên mới: [chi tiết lỗi]"
}
```

## 4. Cập nhật thông tin nhân viên

**Endpoint**: `PUT /api/v1/employees/{employeeId}`

**Mô tả**: Cập nhật thông tin của một nhân viên dựa trên ID. **Chỉ dành cho Admin**.

**Request Header**:
```
Authorization: Bearer <your_admin_jwt_token>
Content-Type: application/json
```

**Request Body**:
```json
{
  "name": "Lê Thị Hương Updated",
  "email": "lethihuong.updated@hotel.com",
  "phone": "0912345679",
  "department": "RESTAURANT",
  "position": "SERVER",
  "joinDate": "2023-01-20",
  "status": true
}
```

**Lưu ý**: Chỉ cần cung cấp các trường cần cập nhật.

**Response (Success - 200 OK)**:
```json
{
  "id": 21,
  "name": "Lê Thị Hương Updated",
  "email": "lethihuong.updated@hotel.com",
  "phone": "0912345679",
  "department": "RESTAURANT",
  "position": "SERVER",
  "joinDate": "2023-01-20",
  "status": true
}
```

**Response (Error - 404 Not Found)**:
```json
{
  "message": "Không tìm thấy nhân viên với ID: 999"
}
```

**Response (Error - 400 Bad Request)**:
```json
{
  "message": "Email already exists"
}
```

**Response (Error - 403 Forbidden)**:
```json
{
  "message": "Bạn không có quyền cập nhật thông tin nhân viên"
}
```

**Response (Error - 500 Internal Server Error)**:
```json
{
  "message": "Lỗi khi cập nhật thông tin nhân viên: [chi tiết lỗi]"
}
```

## 5. Xóa nhân viên

**Endpoint**: `DELETE /api/v1/employees/{employeeId}`

**Mô tả**: Xóa một nhân viên khỏi hệ thống dựa trên ID. **Chỉ dành cho Admin**.

**Request Header**:
```
Authorization: Bearer <your_admin_jwt_token>
```

**Response (Success - 200 OK)**:
```json
{
  "message": "Xóa nhân viên thành công"
}
```

**Response (Error - 404 Not Found)**:
```json
{
  "message": "Không tìm thấy nhân viên với ID: 999"
}
```

**Response (Error - 403 Forbidden)**:
```json
{
  "message": "Bạn không có quyền xóa nhân viên"
}
```

**Response (Error - 500 Internal Server Error)**:
```json
{
  "message": "Lỗi khi xóa nhân viên: [chi tiết lỗi]"
}
``` 