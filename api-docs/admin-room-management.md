# API Quản lý phòng khách sạn

File tài liệu này mô tả các API dành cho quản lý phòng trong hệ thống khách sạn.

## Yêu cầu xác thực

Tất cả các API trong tài liệu này **đều yêu cầu quyền Admin (`ROLE_ADMIN`)** và cần gửi kèm JWT token hợp lệ của người dùng có quyền Admin trong header `Authorization`.

**Header xác thực**:
```
Authorization: Bearer <your_admin_jwt_token>
```

**Lưu ý quan trọng**: Nếu bạn nhận được lỗi `403 Forbidden` khi gọi các API này, điều đó có nghĩa là JWT token bạn đang sử dụng không thuộc về một người dùng có vai trò `ADMIN` hoặc token không hợp lệ/hết hạn.

## Cấu trúc dữ liệu

### Trạng thái phòng (Room Status)
```
VACANT      - Phòng trống (sẵn sàng sử dụng)
OCCUPIED    - Đang có khách
MAINTENANCE - Đang bảo trì
CLEANING    - Đang dọn dẹp
```

## 1. Lấy danh sách phòng

**Endpoint**: `GET /api/v1/admin/rooms`

**Mô tả**: Lấy danh sách tất cả phòng trong hệ thống. **Chỉ dành cho Admin**.

**Request Header**:
```
Authorization: Bearer <your_admin_jwt_token>
```

**Response (Success - 200 OK)**:
```json
[
  {
    "id": 1,
    "roomNumber": "101",
    "roomTypeId": 1,
    "roomTypeName": "Phòng Đơn Tiêu Chuẩn",
    "status": "VACANT",
    "floor": "1",
    "isActive": true,
    "notes": "Phòng hướng biển",
    "createdAt": "20/03/2023",
    "updatedAt": null,
    "pricePerNight": 500000
  },
  {
    "id": 2,
    "roomNumber": "102",
    "roomTypeId": 2,
    "roomTypeName": "Phòng Đôi Tiêu Chuẩn",
    "status": "OCCUPIED",
    "floor": "1",
    "isActive": true,
    "notes": "Phòng hướng hồ bơi",
    "createdAt": "20/03/2023",
    "updatedAt": "25/03/2023",
    "pricePerNight": 800000
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
  "path": "/api/v1/admin/rooms"
}
```

## 2. Lấy thông tin phòng theo ID

**Endpoint**: `GET /api/v1/admin/rooms/{roomId}`

**Mô tả**: Lấy thông tin chi tiết của một phòng dựa trên ID. **Chỉ dành cho Admin**.

**Request Header**:
```
Authorization: Bearer <your_admin_jwt_token>
```

**Response (Success - 200 OK)**:
```json
{
  "id": 1,
  "roomNumber": "101",
  "roomTypeId": 1,
  "roomTypeName": "Phòng Đơn Tiêu Chuẩn",
  "status": "VACANT",
  "floor": "1",
  "isActive": true,
  "notes": "Phòng hướng biển",
  "createdAt": "20/03/2023",
  "updatedAt": null,
  "pricePerNight": 500000
}
```

**Response (Error - 404 Not Found)**:
```json
{
  "message": "Không tìm thấy phòng với ID: 999"
}
```

## 3. Lấy thông tin phòng theo số phòng

**Endpoint**: `GET /api/v1/admin/rooms/room-number/{roomNumber}`

**Mô tả**: Lấy thông tin chi tiết của một phòng dựa trên số phòng. **Chỉ dành cho Admin**.

**Request Header**:
```
Authorization: Bearer <your_admin_jwt_token>
```

**Response (Success - 200 OK)**:
```json
{
  "id": 1,
  "roomNumber": "101",
  "roomTypeId": 1,
  "roomTypeName": "Phòng Đơn Tiêu Chuẩn",
  "status": "VACANT",
  "floor": "1",
  "isActive": true,
  "notes": "Phòng hướng biển",
  "createdAt": "20/03/2023",
  "updatedAt": null,
  "pricePerNight": 500000
}
```

**Response (Error - 404 Not Found)**:
```json
{
  "message": "Không tìm thấy phòng với số phòng: 999"
}
```

## 4. Tạo phòng mới

**Endpoint**: `POST /api/v1/admin/rooms`

**Mô tả**: Tạo một phòng mới trong hệ thống. **Chỉ dành cho Admin**.

**Request Header**:
```
Authorization: Bearer <your_admin_jwt_token>
Content-Type: application/json
```

**Request Body**:
```json
{
  "roomNumber": "201",
  "roomTypeId": 3,
  "status": "VACANT",
  "floor": "2",
  "isActive": true,
  "notes": "Phòng gia đình rộng rãi"
}
```

**Lưu ý**:
- `roomNumber` và `roomTypeId` là bắt buộc
- `roomNumber` phải là duy nhất trong hệ thống

**Response (Success - 201 Created)**:
```json
{
  "id": 3,
  "roomNumber": "201",
  "roomTypeId": 3,
  "roomTypeName": "Phòng Gia Đình",
  "status": "VACANT",
  "floor": "2",
  "isActive": true,
  "notes": "Phòng gia đình rộng rãi",
  "createdAt": "29/03/2023",
  "updatedAt": null,
  "pricePerNight": 1200000
}
```

**Response (Error - 400 Bad Request)**: _(Khi số phòng đã tồn tại)_
```json
{
  "message": "Số phòng đã tồn tại: 201"
}
```

**Response (Error - 404 Not Found)**: _(Khi loại phòng không tồn tại)_
```json
{
  "message": "Không tìm thấy loại phòng với ID: 999"
}
```

## 5. Cập nhật thông tin phòng

**Endpoint**: `PUT /api/v1/admin/rooms/{roomId}`

**Mô tả**: Cập nhật thông tin của một phòng dựa trên ID. **Chỉ dành cho Admin**.

**Request Header**:
```
Authorization: Bearer <your_admin_jwt_token>
Content-Type: application/json
```

**Request Body**:
```json
{
  "roomNumber": "201",
  "roomTypeId": 4,
  "status": "MAINTENANCE",
  "floor": "2",
  "isActive": true,
  "notes": "Phòng đang được nâng cấp thiết bị"
}
```

**Response (Success - 200 OK)**:
```json
{
  "id": 3,
  "roomNumber": "201",
  "roomTypeId": 4,
  "roomTypeName": "Phòng Hạng Sang",
  "status": "MAINTENANCE",
  "floor": "2",
  "isActive": true,
  "notes": "Phòng đang được nâng cấp thiết bị",
  "createdAt": "29/03/2023",
  "updatedAt": "29/03/2023",
  "pricePerNight": 1800000
}
```

**Response (Error - 404 Not Found)**:
```json
{
  "message": "Không tìm thấy phòng với ID: 999"
}
```

**Response (Error - 400 Bad Request)**: _(Khi số phòng đã tồn tại)_
```json
{
  "message": "Số phòng đã tồn tại: 201"
}
```

## 6. Xóa phòng

**Endpoint**: `DELETE /api/v1/admin/rooms/{roomId}`

**Mô tả**: Xóa (soft delete) một phòng khỏi hệ thống dựa trên ID. Thực tế là đánh dấu phòng không hoạt động (isActive = false). **Chỉ dành cho Admin**.

**Request Header**:
```
Authorization: Bearer <your_admin_jwt_token>
```

**Response (Success - 204 No Content)**:
_Không có nội dung phản hồi_

**Response (Error - 404 Not Found)**:
```json
{
  "message": "Không tìm thấy phòng với ID: 999"
}
```

## 7. Lấy danh sách phòng theo loại phòng

**Endpoint**: `GET /api/v1/admin/rooms/room-type/{roomTypeId}`

**Mô tả**: Lấy danh sách các phòng thuộc về một loại phòng cụ thể. **Chỉ dành cho Admin**.

**Request Header**:
```
Authorization: Bearer <your_admin_jwt_token>
```

**Response (Success - 200 OK)**:
```json
[
  {
    "id": 1,
    "roomNumber": "101",
    "roomTypeId": 1,
    "roomTypeName": "Phòng Đơn Tiêu Chuẩn",
    "status": "VACANT",
    "floor": "1",
    "isActive": true,
    "notes": "Phòng hướng biển",
    "createdAt": "20/03/2023",
    "updatedAt": null,
    "pricePerNight": 500000
  },
  {
    "id": 4,
    "roomNumber": "102",
    "roomTypeId": 1,
    "roomTypeName": "Phòng Đơn Tiêu Chuẩn",
    "status": "CLEANING",
    "floor": "1",
    "isActive": true,
    "notes": "Phòng góc",
    "createdAt": "20/03/2023",
    "updatedAt": "25/03/2023",
    "pricePerNight": 500000
  }
]
```

## 8. Lấy danh sách phòng theo trạng thái

**Endpoint**: `GET /api/v1/admin/rooms/status/{status}`

**Mô tả**: Lấy danh sách các phòng có cùng trạng thái. **Chỉ dành cho Admin**.

**Request Header**:
```
Authorization: Bearer <your_admin_jwt_token>
```

**Tham số đường dẫn**:
- `status`: VACANT, OCCUPIED, MAINTENANCE, CLEANING

**Response (Success - 200 OK)**:
```json
[
  {
    "id": 1,
    "roomNumber": "101",
    "roomTypeId": 1,
    "roomTypeName": "Phòng Đơn Tiêu Chuẩn",
    "status": "VACANT",
    "floor": "1",
    "isActive": true,
    "notes": "Phòng hướng biển",
    "createdAt": "20/03/2023",
    "updatedAt": null,
    "pricePerNight": 500000
  },
  {
    "id": 5,
    "roomNumber": "103",
    "roomTypeId": 2,
    "roomTypeName": "Phòng Đôi Tiêu Chuẩn",
    "status": "VACANT",
    "floor": "1",
    "isActive": true,
    "notes": "Phòng góc",
    "createdAt": "20/03/2023",
    "updatedAt": null,
    "pricePerNight": 800000
  }
]
```

## 9. Khởi tạo dữ liệu phòng từ file JSON

**Endpoint**: `POST /api/v1/admin/rooms/init`

**Mô tả**: Khởi tạo dữ liệu phòng từ file JSON. Chỉ khởi tạo nếu không có dữ liệu trong cơ sở dữ liệu. **Chỉ dành cho Admin**.

**Request Header**:
```
Authorization: Bearer <your_admin_jwt_token>
```

**Response (Success - 200 OK)**:
```json
"Khởi tạo dữ liệu phòng thành công"
```

**Lưu ý**:
- API này chỉ thêm dữ liệu từ file JSON nếu cơ sở dữ liệu chưa có dữ liệu phòng nào
- Nếu đã có dữ liệu phòng trong cơ sở dữ liệu, API vẫn trả về thành công nhưng không thực hiện thêm mới dữ liệu
- API này phụ thuộc vào dữ liệu loại phòng đã tồn tại trong cơ sở dữ liệu (cần phải chạy API khởi tạo loại phòng trước)