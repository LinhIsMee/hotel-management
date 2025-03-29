# API Quản lý loại phòng khách sạn

File tài liệu này mô tả các API dành cho quản lý loại phòng trong hệ thống khách sạn.

## Yêu cầu xác thực

Tất cả các API trong tài liệu này **đều yêu cầu quyền Admin (`ROLE_ADMIN`)** và cần gửi kèm JWT token hợp lệ của người dùng có quyền Admin trong header `Authorization`.

**Header xác thực**:
```
Authorization: Bearer <your_admin_jwt_token>
```

**Lưu ý quan trọng**: Nếu bạn nhận được lỗi `403 Forbidden` khi gọi các API này, điều đó có nghĩa là JWT token bạn đang sử dụng không thuộc về một người dùng có vai trò `ADMIN` hoặc token không hợp lệ/hết hạn.

## 1. Lấy danh sách loại phòng

**Endpoint**: `GET /api/v1/admin/room-types`

**Mô tả**: Lấy danh sách tất cả loại phòng trong hệ thống. **Chỉ dành cho Admin**.

**Request Header**:
```
Authorization: Bearer <your_admin_jwt_token>
```

**Response (Success - 200 OK)**:
```json
[
  {
    "id": 1,
    "name": "Phòng Đơn Tiêu Chuẩn",
    "code": "SINGLE",
    "description": "Phòng đơn với 1 giường đơn, phù hợp cho 1 người.",
    "pricePerNight": 500000,
    "maxOccupancy": 1,
    "amenities": ["TV", "Điều hòa", "Wifi", "Minibar"],
    "imageUrl": "https://minio.fares.vn/mixivivu-dev/tour/du-thuyen-heritage-binh-chuan-cat-ba/Ph%C3%B2ng%20Delta%20Suite/b1zy0kd45oky2b4k.webp",
    "isActive": true,
    "createdAt": "10/01/2023"
  },
  {
    "id": 2,
    "name": "Phòng Đôi Tiêu Chuẩn",
    "code": "DOUBLE",
    "description": "Phòng với 1 giường đôi, phù hợp cho 2 người.",
    "pricePerNight": 800000,
    "maxOccupancy": 2,
    "amenities": ["TV", "Điều hòa", "Wifi", "Minibar", "Bồn tắm"],
    "imageUrl": "https://minio.fares.vn/mixivivu-dev/tour/du-thuyen-heritage-binh-chuan-cat-ba/Ph%C3%B2ng%20Delta%20Suite/e1ozdho5a3a8iuom.webp",
    "isActive": true,
    "createdAt": "15/01/2023"
  }
]
```

**Response (Error - 403 Forbidden)**: _(Khi token không có quyền Admin)_ 
```json
{
  "timestamp": "2023-10-27T10:30:00.123+00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied",
  "path": "/api/v1/admin/room-types"
}
```

## 2. Lấy thông tin loại phòng theo ID

**Endpoint**: `GET /api/v1/admin/room-types/{id}`

**Mô tả**: Lấy thông tin chi tiết của một loại phòng dựa trên ID. **Chỉ dành cho Admin**.

**Request Header**:
```
Authorization: Bearer <your_admin_jwt_token>
```

**Response (Success - 200 OK)**:
```json
{
  "id": 1,
  "name": "Phòng Đơn Tiêu Chuẩn",
  "code": "SINGLE",
  "description": "Phòng đơn với 1 giường đơn, phù hợp cho 1 người.",
  "pricePerNight": 500000,
  "maxOccupancy": 1,
  "amenities": ["TV", "Điều hòa", "Wifi", "Minibar"],
  "imageUrl": "https://minio.fares.vn/mixivivu-dev/tour/du-thuyen-heritage-binh-chuan-cat-ba/Ph%C3%B2ng%20Delta%20Suite/b1zy0kd45oky2b4k.webp",
  "isActive": true,
  "createdAt": "10/01/2023"
}
```

**Response (Error - 404 Not Found)**:
```json
{
  "message": "Không tìm thấy loại phòng với ID: 999"
}
```

## 3. Tạo loại phòng mới

**Endpoint**: `POST /api/v1/admin/room-types`

**Mô tả**: Tạo một loại phòng mới trong hệ thống. **Chỉ dành cho Admin**.

**Request Header**:
```
Authorization: Bearer <your_admin_jwt_token>
Content-Type: application/json
```

**Request Body**:
```json
{
  "name": "Phòng Studio",
  "code": "STUDIO",
  "description": "Phòng phong cách studio hiện đại, phù hợp cho 2 người.",
  "pricePerNight": 950000,
  "maxOccupancy": 2,
  "amenities": ["TV", "Điều hòa", "Wifi", "Minibar", "Bếp nhỏ", "Bàn làm việc"],
  "imageUrl": "https://minio.fares.vn/mixivivu-dev/tour/du-thuyen-heritage-binh-chuan-cat-ba/Ph%C3%B2ng%20Ocean%20Suite/ceb6gpnbn7ujv921.webp",
  "isActive": true
}
```

**Lưu ý**:
- `name` và `code` là bắt buộc
- `name` và `code` phải là duy nhất trong hệ thống

**Response (Success - 201 Created)**:
```json
{
  "id": 6,
  "name": "Phòng Studio",
  "code": "STUDIO",
  "description": "Phòng phong cách studio hiện đại, phù hợp cho 2 người.",
  "pricePerNight": 950000,
  "maxOccupancy": 2,
  "amenities": ["TV", "Điều hòa", "Wifi", "Minibar", "Bếp nhỏ", "Bàn làm việc"],
  "imageUrl": "https://minio.fares.vn/mixivivu-dev/tour/du-thuyen-heritage-binh-chuan-cat-ba/Ph%C3%B2ng%20Ocean%20Suite/ceb6gpnbn7ujv921.webp",
  "isActive": true,
  "createdAt": "29/03/2023"
}
```

**Response (Error - 400 Bad Request)**: _(Khi tên hoặc mã đã tồn tại)_
```json
{
  "message": "Tên loại phòng đã tồn tại: Phòng Studio"
}
```

hoặc

```json
{
  "message": "Mã loại phòng đã tồn tại: STUDIO"
}
```

## 4. Cập nhật thông tin loại phòng

**Endpoint**: `PUT /api/v1/admin/room-types/{id}`

**Mô tả**: Cập nhật thông tin của một loại phòng dựa trên ID. **Chỉ dành cho Admin**.

**Request Header**:
```
Authorization: Bearer <your_admin_jwt_token>
Content-Type: application/json
```

**Request Body**:
```json
{
  "name": "Phòng Studio Cao Cấp",
  "code": "STUDIO_PLUS",
  "description": "Phòng phong cách studio hiện đại và sang trọng, phù hợp cho 2 người.",
  "pricePerNight": 1200000,
  "maxOccupancy": 2,
  "amenities": ["TV 4K", "Điều hòa", "Wifi tốc độ cao", "Minibar", "Bếp nhỏ", "Bàn làm việc"],
  "imageUrl": "https://minio.fares.vn/mixivivu-dev/tour/du-thuyen-heritage-binh-chuan-cat-ba/Ph%C3%B2ng%20Ocean%20Suite/ceb6gpnbn7ujv921.webp",
  "isActive": true
}
```

**Response (Success - 200 OK)**:
```json
{
  "id": 6,
  "name": "Phòng Studio Cao Cấp",
  "code": "STUDIO_PLUS",
  "description": "Phòng phong cách studio hiện đại và sang trọng, phù hợp cho 2 người.",
  "pricePerNight": 1200000,
  "maxOccupancy": 2,
  "amenities": ["TV 4K", "Điều hòa", "Wifi tốc độ cao", "Minibar", "Bếp nhỏ", "Bàn làm việc"],
  "imageUrl": "https://minio.fares.vn/mixivivu-dev/tour/du-thuyen-heritage-binh-chuan-cat-ba/Ph%C3%B2ng%20Ocean%20Suite/ceb6gpnbn7ujv921.webp",
  "isActive": true,
  "createdAt": "29/03/2023"
}
```

**Response (Error - 404 Not Found)**:
```json
{
  "message": "Không tìm thấy loại phòng với ID: 999"
}
```

**Response (Error - 400 Bad Request)**: _(Khi tên hoặc mã đã tồn tại)_
```json
{
  "message": "Tên loại phòng đã tồn tại: Phòng Studio Cao Cấp"
}
```

## 5. Xóa (Vô hiệu hóa) loại phòng

**Endpoint**: `DELETE /api/v1/admin/room-types/{id}`

**Mô tả**: Vô hiệu hóa một loại phòng dựa trên ID (soft delete). **Chỉ dành cho Admin**.

**Request Header**:
```
Authorization: Bearer <your_admin_jwt_token>
```

**Response (Success - 200 OK)**:
```json
{
  "id": 6,
  "name": "Phòng Studio Cao Cấp",
  "code": "STUDIO_PLUS",
  "description": "Phòng phong cách studio hiện đại và sang trọng, phù hợp cho 2 người.",
  "pricePerNight": 1200000,
  "maxOccupancy": 2,
  "amenities": ["TV 4K", "Điều hòa", "Wifi tốc độ cao", "Minibar", "Bếp nhỏ", "Bàn làm việc"],
  "imageUrl": "https://minio.fares.vn/mixivivu-dev/tour/du-thuyen-heritage-binh-chuan-cat-ba/Ph%C3%B2ng%20Ocean%20Suite/ceb6gpnbn7ujv921.webp",
  "isActive": false,
  "createdAt": "29/03/2023"
}
```

**Response (Error - 404 Not Found)**:
```json
{
  "message": "Không tìm thấy loại phòng với ID: 999"
}
```

## 6. Khởi tạo dữ liệu loại phòng từ file JSON

**Endpoint**: `POST /api/v1/admin/room-types/init`

**Mô tả**: Khởi tạo dữ liệu loại phòng từ file JSON. Chỉ khởi tạo nếu không có dữ liệu trong cơ sở dữ liệu. **Chỉ dành cho Admin**.

**Request Header**:
```
Authorization: Bearer <your_admin_jwt_token>
```

**Response (Success - 200 OK)**:
```json
"Khởi tạo dữ liệu loại phòng thành công"
```

**Lưu ý**:
- API này chỉ thêm dữ liệu từ file JSON nếu cơ sở dữ liệu chưa có dữ liệu loại phòng nào
- Nếu đã có dữ liệu loại phòng trong cơ sở dữ liệu, API vẫn trả về thành công nhưng không thực hiện thêm mới dữ liệu 