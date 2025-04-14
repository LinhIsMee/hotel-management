# API Quản lý phòng

API này cung cấp các endpoint để quản lý phòng khách sạn, bao gồm lấy danh sách phòng, chi tiết phòng, phòng theo loại, phòng theo sức chứa, và phòng trống trong khoảng thời gian.

## GET /api/v1/rooms

Lấy danh sách tất cả các phòng đang hoạt động.

### Quyền truy cập
- Không yêu cầu xác thực

### Tham số
Không có

### Phản hồi

```json
[
  {
    "id": 32,
    "roomNumber": "324234",
    "roomTypeId": 3,
    "roomTypeName": "Phòng Gia Đình",
    "status": "MAINTENANCE",
    "floor": "1434",
    "isActive": true,
    "notes": "",
    "createdAt": "29/03/2025",
    "updatedAt": null,
    "pricePerNight": 1200000.0,
    "images": [],
    "services": [],
    "maxOccupancy": 4,
    "amenities": ["TV", "Điều hòa", "Wifi", "Minibar", "Bồn tắm", "Tủ lạnh"],
    "specialFeatures": "",
    "averageRating": 4.5,
    "totalReviews": 10,
    "recentReviews": [
      {
        "id": 1,
        "bookingId": "BK2023001",
        "guestName": "Nguyễn Văn A",
        "displayName": "Nguyễn Văn A",
        "roomNumber": "324234",
        "roomType": "Phòng Gia Đình",
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
        "replyDate": "30/03/2025",
        "isFeatured": true,
        "isAnonymous": false,
        "status": "REPLIED",
        "createdAt": "29/03/2025",
        "updatedAt": "30/03/2025"
      }
    ]
  }
]
```

## GET /api/v1/rooms/{id}

Lấy thông tin chi tiết của một phòng cụ thể.

### Quyền truy cập
- Không yêu cầu xác thực

### Tham số
- **id**: ID của phòng cần lấy thông tin

### Phản hồi

```json
{
  "id": 32,
  "roomNumber": "324234",
  "roomTypeId": 3,
  "roomTypeName": "Phòng Gia Đình",
  "status": "MAINTENANCE",
  "floor": "1434",
  "isActive": true,
  "notes": "",
  "createdAt": "29/03/2025",
  "updatedAt": null,
  "pricePerNight": 1200000.0,
  "images": [],
  "services": [],
  "maxOccupancy": 4,
  "amenities": ["TV", "Điều hòa", "Wifi", "Minibar", "Bồn tắm", "Tủ lạnh"],
  "specialFeatures": "",
  "averageRating": 4.5,
  "totalReviews": 10,
  "recentReviews": [
    {
      "id": 1,
      "bookingId": "BK2023001",
      "guestName": "Nguyễn Văn A",
      "displayName": "Nguyễn Văn A",
      "roomNumber": "324234",
      "roomType": "Phòng Gia Đình",
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
      "replyDate": "30/03/2025",
      "isFeatured": true,
      "isAnonymous": false,
      "status": "REPLIED",
      "createdAt": "29/03/2025",
      "updatedAt": "30/03/2025"
    }
  ]
}
```

## GET /api/v1/rooms/room-type/{roomTypeId}

Lấy danh sách phòng theo loại phòng.

### Quyền truy cập
- Không yêu cầu xác thực

### Tham số
- **roomTypeId**: ID của loại phòng

### Phản hồi

```json
[
  {
    "id": 32,
    "roomNumber": "324234",
    "roomTypeId": 3,
    "roomTypeName": "Phòng Gia Đình",
    "status": "MAINTENANCE",
    "floor": "1434",
    "isActive": true,
    "notes": "",
    "createdAt": "29/03/2025",
    "updatedAt": null,
    "pricePerNight": 1200000.0,
    "images": [],
    "services": [],
    "maxOccupancy": 4,
    "amenities": ["TV", "Điều hòa", "Wifi", "Minibar", "Bồn tắm", "Tủ lạnh"],
    "specialFeatures": "",
    "averageRating": 4.5,
    "totalReviews": 10,
    "recentReviews": [...]
  }
]
```

## GET /api/v1/rooms/occupancy/{maxOccupancy}

Lấy danh sách phòng theo sức chứa tối đa.

### Quyền truy cập
- Không yêu cầu xác thực

### Tham số
- **maxOccupancy**: Số người tối đa có thể ở trong phòng

### Phản hồi

```json
[
  {
    "id": 32,
    "roomNumber": "324234",
    "roomTypeId": 3,
    "roomTypeName": "Phòng Gia Đình",
    "status": "MAINTENANCE",
    "floor": "1434",
    "isActive": true,
    "notes": "",
    "createdAt": "29/03/2025",
    "updatedAt": null,
    "pricePerNight": 1200000.0,
    "images": [],
    "services": [],
    "maxOccupancy": 4,
    "amenities": ["TV", "Điều hòa", "Wifi", "Minibar", "Bồn tắm", "Tủ lạnh"],
    "specialFeatures": "",
    "averageRating": 4.5,
    "totalReviews": 10,
    "recentReviews": [...]
  }
]
```

## GET /api/v1/rooms/available

Lấy danh sách phòng trống trong khoảng thời gian.

### Quyền truy cập
- Không yêu cầu xác thực

### Tham số
- **checkInDate**: Ngày nhận phòng (định dạng ISO, ví dụ: 2025-05-01)
- **checkOutDate**: Ngày trả phòng (định dạng ISO, ví dụ: 2025-05-03)

### Phản hồi

```json
[
  {
    "id": 32,
    "roomNumber": "324234",
    "roomTypeId": 3,
    "roomTypeName": "Phòng Gia Đình",
    "status": "AVAILABLE",
    "floor": "1434",
    "isActive": true,
    "notes": "",
    "createdAt": "29/03/2025",
    "updatedAt": null,
    "pricePerNight": 1200000.0,
    "images": [],
    "services": [],
    "maxOccupancy": 4,
    "amenities": ["TV", "Điều hòa", "Wifi", "Minibar", "Bồn tắm", "Tủ lạnh"],
    "specialFeatures": "",
    "averageRating": 4.5,
    "totalReviews": 10,
    "recentReviews": [...]
  }
]
```

## GET /api/v1/room-types

Lấy danh sách tất cả các loại phòng.

### Quyền truy cập
- Không yêu cầu xác thực

### Tham số
Không có

### Phản hồi

```json
[
  {
    "id": 1,
    "name": "Phòng Đơn Tiêu Chuẩn",
    "code": "SINGLE",
    "description": "Phòng đơn với 1 giường đơn, phù hợp cho 1 người.",
    "pricePerNight": 500000.0,
    "maxOccupancy": 1,
    "amenities": ["TV", "Điều hòa", "Wifi", "Minibar"],
    "imageUrl": "https://example.com/images/single-room.jpg",
    "isActive": true,
    "createdAt": "10/01/2023"
  },
  {
    "id": 2,
    "name": "Phòng Đôi Tiêu Chuẩn",
    "code": "DOUBLE",
    "description": "Phòng với 1 giường đôi, phù hợp cho 2 người.",
    "pricePerNight": 800000.0,
    "maxOccupancy": 2,
    "amenities": ["TV", "Điều hòa", "Wifi", "Minibar", "Bồn tắm"],
    "imageUrl": "https://example.com/images/double-room.jpg",
    "isActive": true,
    "createdAt": "15/01/2023"
  }
]
```

## GET /api/v1/room-types/{id}

Lấy thông tin chi tiết của một loại phòng cụ thể.

### Quyền truy cập
- Không yêu cầu xác thực

### Tham số
- **id**: ID của loại phòng

### Phản hồi

```json
{
  "id": 3,
  "name": "Phòng Gia Đình",
  "code": "FAMILY",
  "description": "Phòng rộng với 1 giường đôi và 2 giường đơn, phù hợp cho gia đình 4 người.",
  "pricePerNight": 1200000.0,
  "maxOccupancy": 4,
  "amenities": ["TV", "Điều hòa", "Wifi", "Minibar", "Bồn tắm", "Tủ lạnh"],
  "imageUrl": "https://example.com/images/family-room.jpg",
  "isActive": true,
  "createdAt": "01/02/2023"
}
```

## GET /api/v1/room-types/{id}/rooms

Lấy danh sách phòng thuộc một loại phòng cụ thể.

### Quyền truy cập
- Không yêu cầu xác thực

### Tham số
- **id**: ID của loại phòng

### Phản hồi

```json
[
  {
    "id": 32,
    "roomNumber": "324234",
    "roomTypeId": 3,
    "roomTypeName": "Phòng Gia Đình",
    "status": "MAINTENANCE",
    "floor": "1434",
    "isActive": true,
    "notes": "",
    "createdAt": "29/03/2025",
    "updatedAt": null,
    "pricePerNight": 1200000.0,
    "images": [],
    "services": [],
    "maxOccupancy": 4,
    "amenities": ["TV", "Điều hòa", "Wifi", "Minibar", "Bồn tắm", "Tủ lạnh"],
    "specialFeatures": "",
    "averageRating": 4.5,
    "totalReviews": 10,
    "recentReviews": [...]
  }
]
``` 