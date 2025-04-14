# API Công Khai Về Phòng Khách Sạn

File tài liệu này mô tả các API công khai dành cho việc lấy thông tin phòng trong hệ thống khách sạn.

## 1. Lấy danh sách tất cả phòng

**Endpoint**: `GET /api/v1/rooms`

**Mô tả**: Lấy danh sách tất cả phòng đang hoạt động trong hệ thống.

**Response (Success - 200 OK)**:
```json
[
  {
    "id": 1,
    "roomNumber": "101",
    "roomTypeId": 1,
    "roomTypeName": "Phòng Deluxe Đơn",
    "status": "VACANT",
    "floor": "1",
    "isActive": true,
    "notes": "Phòng sang trọng với view thành phố, phù hợp cho doanh nhân và du khách đơn.",
    "createdAt": "20/03/2023",
    "updatedAt": null,
    "pricePerNight": 1200000,
    "images": [
      "https://hotel-images.s3.amazonaws.com/room101-img1.jpg", 
      "https://hotel-images.s3.amazonaws.com/room101-img2.jpg"
    ],
    "services": [
      {
        "id": 1,
        "name": "Wifi miễn phí",
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
    "maxOccupancy": 1,
    "amenities": ["TV màn hình phẳng", "Điều hòa", "Tủ lạnh", "Ban công"],
    "specialFeatures": "Phòng sang trọng với view thành phố, phù hợp cho doanh nhân và du khách đơn."
  },
  {
    "id": 2,
    "roomNumber": "201",
    "roomTypeId": 2,
    "roomTypeName": "Phòng Superior Đôi",
    "status": "VACANT",
    "floor": "2",
    "isActive": true,
    "notes": "Phòng rộng rãi với giường đôi thoải mái, thích hợp cho cặp đôi.",
    "createdAt": "20/03/2023",
    "updatedAt": "25/03/2023",
    "pricePerNight": 1500000,
    "images": [
      "https://hotel-images.s3.amazonaws.com/room201-img1.jpg"
    ],
    "services": [
      {
        "id": 1,
        "name": "Wifi miễn phí",
        "description": "Wifi tốc độ cao",
        "price": 0.0
      },
      {
        "id": 3,
        "name": "Buổi ăn sáng",
        "description": "Bữa ăn sáng buffet",
        "price": 120000.0
      }
    ],
    "maxOccupancy": 2,
    "amenities": ["TV màn hình phẳng", "Điều hòa", "Tủ lạnh", "Ban công"],
    "specialFeatures": "Phòng rộng rãi với giường đôi thoải mái, thích hợp cho cặp đôi."
  },
  {
    "id": 3,
    "roomNumber": "301",
    "roomTypeId": 3,
    "roomTypeName": "Phòng Gia Đình",
    "status": "VACANT",
    "floor": "3",
    "isActive": true,
    "notes": "Phòng rộng rãi với 2 giường đôi, thích hợp cho gia đình có con nhỏ.",
    "createdAt": "20/03/2023",
    "updatedAt": null,
    "pricePerNight": 2500000,
    "images": [
      "https://hotel-images.s3.amazonaws.com/room301-img1.jpg",
      "https://hotel-images.s3.amazonaws.com/room301-img2.jpg"
    ],
    "services": [
      {
        "id": 1,
        "name": "Wifi miễn phí",
        "description": "Wifi tốc độ cao",
        "price": 0.0
      },
      {
        "id": 3,
        "name": "Buổi ăn sáng",
        "description": "Bữa ăn sáng buffet",
        "price": 120000.0
      }
    ],
    "maxOccupancy": 4,
    "amenities": ["TV màn hình phẳng", "Điều hòa", "Tủ lạnh", "Ban công", "Bồn tắm"],
    "specialFeatures": "Phòng rộng rãi với 2 giường đôi, thích hợp cho gia đình có con nhỏ."
  }
]
```

## 2. Lấy thông tin phòng theo ID

**Endpoint**: `GET /api/v1/rooms/{roomId}`

**Mô tả**: Lấy thông tin chi tiết của một phòng dựa trên ID.

**Tham số đường dẫn:**
- `roomId` (Integer): ID của phòng cần lấy thông tin

**Response (Success - 200 OK)**:
```json
{
  "id": 1,
  "roomNumber": "101",
  "roomTypeId": 1,
  "roomTypeName": "Phòng Deluxe Đơn",
  "status": "VACANT",
  "floor": "1",
  "isActive": true,
  "notes": "Phòng sang trọng với view thành phố, phù hợp cho doanh nhân và du khách đơn.",
  "createdAt": "20/03/2023",
  "updatedAt": null,
  "pricePerNight": 1200000,
  "images": [
    "https://hotel-images.s3.amazonaws.com/room101-img1.jpg", 
    "https://hotel-images.s3.amazonaws.com/room101-img2.jpg"
  ],
  "services": [
    {
      "id": 1,
      "name": "Wifi miễn phí",
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
  "maxOccupancy": 1,
  "amenities": ["TV màn hình phẳng", "Điều hòa", "Tủ lạnh", "Ban công"],
  "specialFeatures": "Phòng sang trọng với view thành phố, phù hợp cho doanh nhân và du khách đơn."
}
```

**Response (Error - 404 Not Found)**:
```json
{
  "message": "Không tìm thấy phòng với ID: 999"
}
```

## 3. Lấy danh sách phòng theo loại phòng

**Endpoint**: `GET /api/v1/rooms/room-type/{roomTypeId}`

**Mô tả**: Lấy danh sách các phòng thuộc về một loại phòng cụ thể.

**Tham số đường dẫn:**
- `roomTypeId` (Integer): ID của loại phòng

**Response (Success - 200 OK)**:
```json
[
  {
    "id": 1,
    "roomNumber": "101",
    "roomTypeId": 1,
    "roomTypeName": "Phòng Deluxe Đơn",
    "status": "VACANT",
    "floor": "1",
    "isActive": true,
    "notes": "Phòng sang trọng với view thành phố, phù hợp cho doanh nhân và du khách đơn.",
    "createdAt": "20/03/2023",
    "updatedAt": null,
    "pricePerNight": 1200000,
    "images": [
      "https://hotel-images.s3.amazonaws.com/room101-img1.jpg", 
      "https://hotel-images.s3.amazonaws.com/room101-img2.jpg"
    ],
    "services": [
      {
        "id": 1,
        "name": "Wifi miễn phí",
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
    "maxOccupancy": 1,
    "amenities": ["TV màn hình phẳng", "Điều hòa", "Tủ lạnh", "Ban công"],
    "specialFeatures": "Phòng sang trọng với view thành phố, phù hợp cho doanh nhân và du khách đơn."
  },
  {
    "id": 4,
    "roomNumber": "102",
    "roomTypeId": 1,
    "roomTypeName": "Phòng Deluxe Đơn",
    "status": "VACANT",
    "floor": "1",
    "isActive": true,
    "notes": "Phòng sang trọng với view thành phố, phù hợp cho doanh nhân và du khách đơn.",
    "createdAt": "20/03/2023",
    "updatedAt": "25/03/2023",
    "pricePerNight": 1200000,
    "images": [
      "https://hotel-images.s3.amazonaws.com/room102-img1.jpg"
    ],
    "services": [
      {
        "id": 1,
        "name": "Wifi miễn phí",
        "description": "Wifi tốc độ cao",
        "price": 0.0
      }
    ],
    "maxOccupancy": 1,
    "amenities": ["TV màn hình phẳng", "Điều hòa", "Tủ lạnh", "Ban công"],
    "specialFeatures": "Phòng sang trọng với view thành phố, phù hợp cho doanh nhân và du khách đơn."
  }
]
```

## 4. Lấy danh sách phòng trống trong khoảng thời gian

**Endpoint**: `GET /api/v1/rooms/available`

**Mô tả**: Lấy danh sách các phòng còn trống trong một khoảng thời gian cụ thể.

**Tham số query:**
- `checkInDate` (Date, định dạng ISO): Ngày nhận phòng (format: YYYY-MM-DD)
- `checkOutDate` (Date, định dạng ISO): Ngày trả phòng (format: YYYY-MM-DD)

**Ví dụ**: `GET /api/v1/rooms/available?checkInDate=2023-05-01&checkOutDate=2023-05-03`

**Response (Success - 200 OK)**:
```json
[
  {
    "id": 1,
    "roomNumber": "101",
    "roomTypeId": 1,
    "roomTypeName": "Phòng Deluxe Đơn",
    "status": "VACANT",
    "floor": "1",
    "isActive": true,
    "notes": "Phòng sang trọng với view thành phố, phù hợp cho doanh nhân và du khách đơn.",
    "createdAt": "20/03/2023",
    "updatedAt": null,
    "pricePerNight": 1200000,
    "images": [
      "https://hotel-images.s3.amazonaws.com/room101-img1.jpg", 
      "https://hotel-images.s3.amazonaws.com/room101-img2.jpg"
    ],
    "services": [
      {
        "id": 1,
        "name": "Wifi miễn phí",
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
    "maxOccupancy": 1,
    "amenities": ["TV màn hình phẳng", "Điều hòa", "Tủ lạnh", "Ban công"],
    "specialFeatures": "Phòng sang trọng với view thành phố, phù hợp cho doanh nhân và du khách đơn."
  },
  {
    "id": 3,
    "roomNumber": "301",
    "roomTypeId": 3,
    "roomTypeName": "Phòng Gia Đình",
    "status": "VACANT",
    "floor": "3",
    "isActive": true,
    "notes": "Phòng rộng rãi với 2 giường đôi, thích hợp cho gia đình có con nhỏ.",
    "createdAt": "20/03/2023",
    "updatedAt": null,
    "pricePerNight": 2500000,
    "images": [
      "https://hotel-images.s3.amazonaws.com/room301-img1.jpg",
      "https://hotel-images.s3.amazonaws.com/room301-img2.jpg"
    ],
    "services": [
      {
        "id": 1,
        "name": "Wifi miễn phí",
        "description": "Wifi tốc độ cao",
        "price": 0.0
      },
      {
        "id": 3,
        "name": "Buổi ăn sáng",
        "description": "Bữa ăn sáng buffet",
        "price": 120000.0
      }
    ],
    "maxOccupancy": 4,
    "amenities": ["TV màn hình phẳng", "Điều hòa", "Tủ lạnh", "Ban công", "Bồn tắm"],
    "specialFeatures": "Phòng rộng rãi với 2 giường đôi, thích hợp cho gia đình có con nhỏ."
  }
]
```

## 5. Lấy danh sách phòng theo số người lớn

**Endpoint**: `GET /api/v1/rooms/occupancy/{maxOccupancy}`

**Mô tả**: Lấy danh sách các phòng có sức chứa ít nhất số người được chỉ định.

**Tham số đường dẫn:**
- `maxOccupancy` (Integer): Số lượng người tối thiểu mà phòng có thể chứa

**Response (Success - 200 OK)**:
```json
[
  {
    "id": 2,
    "roomNumber": "201",
    "roomTypeId": 2,
    "roomTypeName": "Phòng Superior Đôi",
    "status": "VACANT",
    "floor": "2",
    "isActive": true,
    "notes": "Phòng rộng rãi với giường đôi thoải mái, thích hợp cho cặp đôi.",
    "createdAt": "20/03/2023",
    "updatedAt": "25/03/2023",
    "pricePerNight": 1500000,
    "images": [
      "https://hotel-images.s3.amazonaws.com/room201-img1.jpg"
    ],
    "services": [
      {
        "id": 1,
        "name": "Wifi miễn phí",
        "description": "Wifi tốc độ cao",
        "price": 0.0
      },
      {
        "id": 3,
        "name": "Buổi ăn sáng",
        "description": "Bữa ăn sáng buffet",
        "price": 120000.0
      }
    ],
    "maxOccupancy": 2,
    "amenities": ["TV màn hình phẳng", "Điều hòa", "Tủ lạnh", "Ban công"],
    "specialFeatures": "Phòng rộng rãi với giường đôi thoải mái, thích hợp cho cặp đôi."
  },
  {
    "id": 3,
    "roomNumber": "301",
    "roomTypeId": 3,
    "roomTypeName": "Phòng Gia Đình",
    "status": "VACANT",
    "floor": "3",
    "isActive": true,
    "notes": "Phòng rộng rãi với 2 giường đôi, thích hợp cho gia đình có con nhỏ.",
    "createdAt": "20/03/2023",
    "updatedAt": null,
    "pricePerNight": 2500000,
    "images": [
      "https://hotel-images.s3.amazonaws.com/room301-img1.jpg",
      "https://hotel-images.s3.amazonaws.com/room301-img2.jpg"
    ],
    "services": [
      {
        "id": 1,
        "name": "Wifi miễn phí",
        "description": "Wifi tốc độ cao",
        "price": 0.0
      },
      {
        "id": 3,
        "name": "Buổi ăn sáng",
        "description": "Bữa ăn sáng buffet",
        "price": 120000.0
      }
    ],
    "maxOccupancy": 4,
    "amenities": ["TV màn hình phẳng", "Điều hòa", "Tủ lạnh", "Ban công", "Bồn tắm"],
    "specialFeatures": "Phòng rộng rãi với 2 giường đôi, thích hợp cho gia đình có con nhỏ."
  }
]
``` 