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
    "pricePerNight": 500000,
    "images": [
      "https://hotel-images.s3.amazonaws.com/room101-img1.jpg", 
      "https://hotel-images.s3.amazonaws.com/room101-img2.jpg"
    ],
    "services": [
      {
        "id": 1,
        "name": "Wifi",
        "description": "Wifi tốc độ cao",
        "price": 0.0
      },
      {
        "id": 2,
        "name": "Dịch vụ giặt ủi",
        "description": "Giặt ủi trong ngày",
        "price": 150000.0
      }
    ],
    "maxOccupancy": 2,
    "amenities": ["TV", "Điều hòa", "Tủ lạnh", "Ban công"],
    "specialFeatures": "Phòng hướng biển",
    "averageRating": 4.5,
    "totalReviews": 10,
    "recentReviews": [
      {
        "id": 1,
        "bookingId": "BK2023001",
        "guestName": "Nguyễn Văn A",
        "displayName": "Nguyễn Văn A",
        "roomNumber": "101",
        "roomType": "Phòng Đơn Tiêu Chuẩn",
        "rating": 5,
        "cleanliness": 5,
        "service": 4,
        "comfort": 5,
        "location": 4,
        "facilities": 5,
        "valueForMoney": 4,
        "comment": "Phòng rất thoải mái và sạch sẽ",
        "images": [],
        "replyComment": "Cảm ơn quý khách đã đánh giá",
        "replyBy": "Quản lý",
        "replyDate": "30/03/2023",
        "isFeatured": true,
        "isAnonymous": false,
        "status": "REPLIED",
        "createdAt": "29/03/2023",
        "updatedAt": "30/03/2023"
      }
    ]
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
    "pricePerNight": 800000,
    "images": [
      "https://hotel-images.s3.amazonaws.com/room102-img1.jpg"
    ],
    "services": [
      {
        "id": 1,
        "name": "Wifi",
        "description": "Wifi tốc độ cao",
        "price": 0.0
      }
    ],
    "maxOccupancy": 4,
    "amenities": ["TV", "Điều hòa", "Tủ lạnh", "Két sắt"],
    "specialFeatures": "Phòng hướng hồ bơi",
    "averageRating": 4.2,
    "totalReviews": 6,
    "recentReviews": [
      {
        "id": 3,
        "bookingId": "BK2023005",
        "guestName": "Trần Thị B",
        "displayName": "Trần Thị B",
        "roomNumber": "102",
        "roomType": "Phòng Đôi Tiêu Chuẩn",
        "rating": 4,
        "cleanliness": 4,
        "service": 5,
        "comfort": 4,
        "location": 3,
        "facilities": 5,
        "valueForMoney": 4,
        "comment": "Phòng khá đẹp, nhân viên phục vụ tốt",
        "images": [],
        "status": "PENDING",
        "createdAt": "15/03/2023",
        "updatedAt": null
      }
    ]
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

**Endpoint**: `GET /api/v1/admin/rooms/{id}`

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
  "pricePerNight": 500000,
  "images": [
    "https://hotel-images.s3.amazonaws.com/room101-img1.jpg", 
    "https://hotel-images.s3.amazonaws.com/room101-img2.jpg"
  ],
  "services": [
    {
      "id": 1,
      "name": "Wifi",
      "description": "Wifi tốc độ cao",
      "price": 0.0
    },
    {
      "id": 2,
      "name": "Dịch vụ giặt ủi",
      "description": "Giặt ủi trong ngày",
      "price": 150000.0
    }
  ],
  "maxOccupancy": 2,
  "amenities": ["TV", "Điều hòa", "Tủ lạnh", "Ban công"],
  "specialFeatures": "Phòng hướng biển",
  "averageRating": 4.5,
  "totalReviews": 10
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
  "pricePerNight": 500000,
  "images": [
    "https://hotel-images.s3.amazonaws.com/room101-img1.jpg", 
    "https://hotel-images.s3.amazonaws.com/room101-img2.jpg"
  ],
  "services": [
    {
      "id": 1,
      "name": "Wifi",
      "description": "Wifi tốc độ cao",
      "price": 0.0
    }
  ],
  "maxOccupancy": 2,
  "amenities": ["TV", "Điều hòa", "Tủ lạnh", "Ban công"],
  "specialFeatures": "Phòng hướng biển",
  "averageRating": 4.5,
  "totalReviews": 10
}
```

## 4. Tạo phòng mới

**Endpoint**: `POST /api/v1/admin/rooms`

**Mô tả**: Tạo một phòng mới trong hệ thống. **Chỉ dành cho Admin**.

**Request Header**:
```
Authorization: Bearer <your_admin_jwt_token>
```

**Request Body (Mẫu)**:
```json
{
  "roomNumber": "103",
  "roomTypeId": 1,
  "status": "VACANT",
  "floor": "1",
  "isActive": true,
  "notes": "Phòng hướng vườn"
}
```

**Response (Success - 201 Created)**:
```json
{
  "id": 3,
  "roomNumber": "103",
  "roomTypeId": 1,
  "roomTypeName": "Phòng Đơn Tiêu Chuẩn",
  "status": "VACANT",
  "floor": "1",
  "isActive": true,
  "notes": "Phòng hướng vườn",
  "createdAt": "08/04/2025",
  "updatedAt": null,
  "pricePerNight": 500000,
  "images": [],
  "services": [],
  "maxOccupancy": 2,
  "amenities": ["TV", "Điều hòa", "Tủ lạnh"],
  "specialFeatures": "",
  "averageRating": 0,
  "totalReviews": 0
}
```

## 5. Cập nhật thông tin phòng

**Endpoint**: `PUT /api/v1/admin/rooms/{id}`

**Mô tả**: Cập nhật thông tin của một phòng dựa trên ID. **Chỉ dành cho Admin**.

**Request Header**:
```
Authorization: Bearer <your_admin_jwt_token>
```

**Request Body (Mẫu)**:
```json
{
  "roomNumber": "103",
  "roomTypeId": 2,
  "status": "MAINTENANCE",
  "floor": "1",
  "isActive": true,
  "notes": "Phòng đang sửa chữa điều hòa"
}
```

**Response (Success - 200 OK)**:
```json
{
  "id": 3,
  "roomNumber": "103",
  "roomTypeId": 2,
  "roomTypeName": "Phòng Đôi Tiêu Chuẩn",
  "status": "MAINTENANCE",
  "floor": "1",
  "isActive": true,
  "notes": "Phòng đang sửa chữa điều hòa",
  "createdAt": "08/04/2025",
  "updatedAt": "08/04/2025",
  "pricePerNight": 800000,
  "images": [],
  "services": [],
  "maxOccupancy": 4,
  "amenities": ["TV", "Điều hòa", "Tủ lạnh", "Két sắt"],
  "specialFeatures": "",
  "averageRating": 0,
  "totalReviews": 0
}
```

## 6. Xóa phòng

**Endpoint**: `DELETE /api/v1/admin/rooms/{id}`

**Mô tả**: Xóa một phòng khỏi hệ thống (soft delete). **Chỉ dành cho Admin**.

**Request Header**:
```
Authorization: Bearer <your_admin_jwt_token>
```

**Response (Success - 204 No Content)**:
```
Không có nội dung trả về
```

## 7. Lấy danh sách phòng theo loại phòng

**Endpoint**: `GET /api/v1/admin/rooms/room-type/{roomTypeId}`

**Mô tả**: Lấy danh sách phòng theo loại phòng. **Chỉ dành cho Admin**.

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
    "pricePerNight": 500000,
    "images": [
      "https://hotel-images.s3.amazonaws.com/room101-img1.jpg", 
      "https://hotel-images.s3.amazonaws.com/room101-img2.jpg"
    ],
    "services": [
      {
        "id": 1,
        "name": "Wifi",
        "description": "Wifi tốc độ cao",
        "price": 0.0
      }
    ],
    "maxOccupancy": 2,
    "amenities": ["TV", "Điều hòa", "Tủ lạnh", "Ban công"],
    "specialFeatures": "Phòng hướng biển",
    "averageRating": 4.5,
    "totalReviews": 10
  }
]
```

## 8. Lấy danh sách phòng theo trạng thái

**Endpoint**: `GET /api/v1/admin/rooms/status/{status}`

**Mô tả**: Lấy danh sách phòng theo trạng thái. **Chỉ dành cho Admin**.

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
    "pricePerNight": 500000,
    "images": [
      "https://hotel-images.s3.amazonaws.com/room101-img1.jpg", 
      "https://hotel-images.s3.amazonaws.com/room101-img2.jpg"
    ],
    "services": [
      {
        "id": 1,
        "name": "Wifi",
        "description": "Wifi tốc độ cao",
        "price": 0.0
      }
    ],
    "maxOccupancy": 2,
    "amenities": ["TV", "Điều hòa", "Tủ lạnh", "Ban công"],
    "specialFeatures": "Phòng hướng biển",
    "averageRating": 4.5,
    "totalReviews": 10
  }
]
```