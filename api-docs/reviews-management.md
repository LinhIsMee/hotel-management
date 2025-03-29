# Hotel Management API - Quản lý Đánh giá (Reviews)

API này cung cấp các endpoint để quản lý đánh giá của khách hàng về trải nghiệm tại khách sạn.

## Mô hình dữ liệu

### Review

```json
{
  "id": 1,
  "bookingId": "BK-2023090501",
  "guestName": "Nguyễn Văn An",
  "displayName": "Nguyễn V.",
  "roomNumber": "101",
  "roomType": "Deluxe",
  "rating": 5,
  "cleanliness": 5,
  "service": 5,
  "comfort": 5,
  "location": 4,
  "facilities": 5,
  "valueForMoney": 4,
  "comment": "Phòng rất sạch sẽ và thoải mái. Nhân viên thân thiện và phục vụ tốt.",
  "images": ["https://example.com/image1.jpg"],
  "replyComment": "Cảm ơn quý khách đã đánh giá cao dịch vụ của chúng tôi.",
  "replyBy": "Nguyễn Quản Lý",
  "replyDate": "11/09/2023 09:15",
  "isFeatured": true,
  "isAnonymous": false,
  "status": "REPLIED",
  "createdAt": "10/09/2023 15:30",
  "updatedAt": "10/09/2023 15:30"
}
```

## API Endpoints

### 1. Lấy danh sách đánh giá

```
GET /api/reviews
```

Trả về danh sách tất cả các đánh giá, có phân trang và sắp xếp.

#### Tham số Query

| Tham số | Kiểu   | Mô tả                                                       |
|---------|--------|-------------------------------------------------------------|
| page    | int    | Số trang (mặc định: 0)                                      |
| size    | int    | Số lượng phần tử mỗi trang (mặc định: 10)                   |
| sortBy  | string | Trường để sắp xếp (mặc định: "id")                          |
| sortDir | string | Hướng sắp xếp: "asc" hoặc "desc" (mặc định: "desc")         |

#### Response

**Thành công (200)**

```json
{
  "statusCode": 200,
  "message": "Lấy danh sách đánh giá thành công",
  "data": {
    "content": [
      {
        "id": 1,
        "bookingId": "BK-2023090501",
        "guestName": "Nguyễn Văn An",
        "displayName": "Nguyễn V.",
        "roomNumber": "101",
        "roomType": "Deluxe",
        "rating": 5,
        "cleanliness": 5,
        "service": 5,
        "comfort": 5,
        "location": 4,
        "facilities": 5,
        "valueForMoney": 4,
        "comment": "Phòng rất sạch sẽ và thoải mái. Nhân viên thân thiện và phục vụ tốt.",
        "images": ["https://example.com/image1.jpg"],
        "replyComment": "Cảm ơn quý khách đã đánh giá cao dịch vụ của chúng tôi.",
        "replyBy": "Nguyễn Quản Lý",
        "replyDate": "11/09/2023 09:15",
        "isFeatured": true,
        "isAnonymous": false,
        "status": "REPLIED",
        "createdAt": "10/09/2023 15:30",
        "updatedAt": "10/09/2023 15:30"
      }
      // ... các đánh giá khác
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10,
      "sort": {
        "sorted": true,
        "unsorted": false,
        "empty": false
      },
      "offset": 0,
      "paged": true,
      "unpaged": false
    },
    "totalPages": 5,
    "totalElements": 50,
    "last": false,
    "size": 10,
    "number": 0,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "numberOfElements": 10,
    "first": true,
    "empty": false
  }
}
```

### 2. Lấy danh sách đánh giá công khai

```
GET /api/reviews/public
```

Trả về danh sách các đánh giá có thể hiển thị công khai (không bao gồm các đánh giá đã ẩn).

#### Tham số Query

| Tham số | Kiểu | Mô tả                                     |
|---------|------|-------------------------------------------|
| page    | int  | Số trang (mặc định: 0)                    |
| size    | int  | Số lượng phần tử mỗi trang (mặc định: 10) |

#### Response

**Thành công (200)**

```json
{
  "statusCode": 200,
  "message": "Lấy danh sách đánh giá công khai thành công",
  "data": {
    "content": [
      // Danh sách đánh giá
    ],
    // Thông tin phân trang
  }
}
```

### 3. Lấy danh sách đánh giá đang chờ xử lý

```
GET /api/reviews/pending
```

Trả về danh sách các đánh giá đang chờ xử lý (chưa được phản hồi).

#### Yêu cầu xác thực

Yêu cầu vai trò: ADMIN hoặc STAFF

#### Tham số Query

| Tham số | Kiểu | Mô tả                                     |
|---------|------|-------------------------------------------|
| page    | int  | Số trang (mặc định: 0)                    |
| size    | int  | Số lượng phần tử mỗi trang (mặc định: 10) |

#### Response

**Thành công (200)**

```json
{
  "statusCode": 200,
  "message": "Lấy danh sách đánh giá đang chờ xử lý thành công",
  "data": {
    "content": [
      // Danh sách đánh giá đang chờ xử lý
    ],
    // Thông tin phân trang
  }
}
```

### 4. Lấy danh sách đánh giá đã được phản hồi

```
GET /api/reviews/replied
```

Trả về danh sách các đánh giá đã được phản hồi.

#### Tham số Query

| Tham số | Kiểu | Mô tả                                     |
|---------|------|-------------------------------------------|
| page    | int  | Số trang (mặc định: 0)                    |
| size    | int  | Số lượng phần tử mỗi trang (mặc định: 10) |

#### Response

**Thành công (200)**

```json
{
  "statusCode": 200,
  "message": "Lấy danh sách đánh giá đã được phản hồi thành công",
  "data": {
    "content": [
      // Danh sách đánh giá đã được phản hồi
    ],
    // Thông tin phân trang
  }
}
```

### 5. Lấy danh sách đánh giá đã ẩn

```
GET /api/reviews/hidden
```

Trả về danh sách các đánh giá đã bị ẩn.

#### Yêu cầu xác thực

Yêu cầu vai trò: ADMIN hoặc STAFF

#### Tham số Query

| Tham số | Kiểu | Mô tả                                     |
|---------|------|-------------------------------------------|
| page    | int  | Số trang (mặc định: 0)                    |
| size    | int  | Số lượng phần tử mỗi trang (mặc định: 10) |

#### Response

**Thành công (200)**

```json
{
  "statusCode": 200,
  "message": "Lấy danh sách đánh giá đã ẩn thành công",
  "data": {
    "content": [
      // Danh sách đánh giá đã ẩn
    ],
    // Thông tin phân trang
  }
}
```

### 6. Lấy thông tin đánh giá theo ID

```
GET /api/reviews/{id}
```

Trả về thông tin chi tiết của một đánh giá theo ID.

#### Tham số Path

| Tham số | Kiểu | Mô tả           |
|---------|------|-----------------|
| id      | int  | ID của đánh giá |

#### Response

**Thành công (200)**

```json
{
  "statusCode": 200,
  "message": "Lấy đánh giá thành công",
  "data": {
    "id": 1,
    "bookingId": "BK-2023090501",
    "guestName": "Nguyễn Văn An",
    "displayName": "Nguyễn V.",
    "roomNumber": "101",
    "roomType": "Deluxe",
    "rating": 5,
    "cleanliness": 5,
    "service": 5,
    "comfort": 5,
    "location": 4,
    "facilities": 5,
    "valueForMoney": 4,
    "comment": "Phòng rất sạch sẽ và thoải mái. Nhân viên thân thiện và phục vụ tốt.",
    "images": ["https://example.com/image1.jpg"],
    "replyComment": "Cảm ơn quý khách đã đánh giá cao dịch vụ của chúng tôi.",
    "replyBy": "Nguyễn Quản Lý",
    "replyDate": "11/09/2023 09:15",
    "isFeatured": true,
    "isAnonymous": false,
    "status": "REPLIED",
    "createdAt": "10/09/2023 15:30",
    "updatedAt": "10/09/2023 15:30"
  }
}
```

**Không tìm thấy (404)**

```json
{
  "statusCode": 404,
  "message": "Đánh giá không tìm thấy với ID: 1",
  "data": null
}
```

### 7. Lấy thông tin đánh giá theo mã đặt phòng

```
GET /api/reviews/booking/{bookingId}
```

Trả về thông tin chi tiết của một đánh giá theo mã đặt phòng.

#### Tham số Path

| Tham số   | Kiểu   | Mô tả                |
|-----------|--------|----------------------|
| bookingId | string | Mã đặt phòng         |

#### Response

**Thành công (200)**

```json
{
  "statusCode": 200,
  "message": "Lấy đánh giá theo mã đặt phòng thành công",
  "data": {
    // Chi tiết đánh giá
  }
}
```

**Không tìm thấy (404)**

```json
{
  "statusCode": 404,
  "message": "Đánh giá không tìm thấy với Booking ID: BK-2023090501",
  "data": null
}
```

### 8. Lấy danh sách đánh giá theo số phòng

```
GET /api/reviews/room/{roomNumber}
```

Trả về danh sách các đánh giá theo số phòng.

#### Tham số Path

| Tham số    | Kiểu   | Mô tả           |
|------------|--------|-----------------|
| roomNumber | string | Số phòng        |

#### Response

**Thành công (200)**

```json
{
  "statusCode": 200,
  "message": "Lấy đánh giá theo số phòng thành công",
  "data": [
    // Danh sách đánh giá cho phòng cụ thể
  ]
}
```

### 9. Lấy danh sách đánh giá theo loại phòng

```
GET /api/reviews/room-type/{roomType}
```

Trả về danh sách các đánh giá theo loại phòng.

#### Tham số Path

| Tham số  | Kiểu   | Mô tả           |
|----------|--------|-----------------|
| roomType | string | Loại phòng      |

#### Response

**Thành công (200)**

```json
{
  "statusCode": 200,
  "message": "Lấy đánh giá theo loại phòng thành công",
  "data": [
    // Danh sách đánh giá cho loại phòng cụ thể
  ]
}
```

### 10. Lấy danh sách đánh giá nổi bật

```
GET /api/reviews/featured
```

Trả về danh sách các đánh giá được đánh dấu là nổi bật.

#### Response

**Thành công (200)**

```json
{
  "statusCode": 200,
  "message": "Lấy đánh giá nổi bật thành công",
  "data": [
    // Danh sách đánh giá nổi bật
  ]
}
```

### 11. Lấy danh sách đánh giá theo điểm tối thiểu

```
GET /api/reviews/min-rating/{minRating}
```

Trả về danh sách các đánh giá có điểm số từ một mức tối thiểu.

#### Tham số Path

| Tham số   | Kiểu | Mô tả                               |
|-----------|------|-------------------------------------|
| minRating | int  | Điểm đánh giá tối thiểu (1-5)       |

#### Tham số Query

| Tham số | Kiểu | Mô tả                                     |
|---------|------|-------------------------------------------|
| page    | int  | Số trang (mặc định: 0)                    |
| size    | int  | Số lượng phần tử mỗi trang (mặc định: 10) |

#### Response

**Thành công (200)**

```json
{
  "statusCode": 200,
  "message": "Lấy đánh giá theo điểm tối thiểu thành công",
  "data": {
    "content": [
      // Danh sách đánh giá có điểm số từ mức tối thiểu
    ],
    // Thông tin phân trang
  }
}
```

### 12. Tìm kiếm đánh giá theo tên khách hàng

```
GET /api/reviews/search
```

Tìm kiếm đánh giá theo tên khách hàng.

#### Tham số Query

| Tham số   | Kiểu   | Mô tả                                     |
|-----------|--------|-------------------------------------------|
| guestName | string | Tên khách hàng cần tìm kiếm               |
| page      | int    | Số trang (mặc định: 0)                    |
| size      | int    | Số lượng phần tử mỗi trang (mặc định: 10) |

#### Response

**Thành công (200)**

```json
{
  "statusCode": 200,
  "message": "Tìm kiếm đánh giá theo tên khách hàng thành công",
  "data": {
    "content": [
      // Danh sách đánh giá khớp với tên khách hàng
    ],
    // Thông tin phân trang
  }
}
```

### 13. Tạo đánh giá mới

```
POST /api/reviews
```

Tạo một đánh giá mới.

#### Request Body

```json
{
  "bookingId": "BK-2023090501",
  "guestName": "Nguyễn Văn An",
  "roomNumber": "101",
  "roomType": "Deluxe",
  "rating": 5,
  "cleanliness": 5,
  "service": 5,
  "comfort": 5,
  "location": 4,
  "facilities": 5,
  "valueForMoney": 4,
  "comment": "Phòng rất sạch sẽ và thoải mái. Nhân viên thân thiện và phục vụ tốt.",
  "images": ["https://example.com/image1.jpg"],
  "isAnonymous": false
}
```

#### Response

**Thành công (201)**

```json
{
  "statusCode": 201,
  "message": "Tạo đánh giá thành công",
  "data": {
    // Chi tiết đánh giá đã được tạo
  }
}
```

**Lỗi (400)**

```json
{
  "statusCode": 400,
  "message": "Đã tồn tại đánh giá cho booking ID: BK-2023090501",
  "data": null
}
```

### 14. Phản hồi đánh giá

```
POST /api/reviews/{id}/reply
```

Phản hồi một đánh giá.

#### Yêu cầu xác thực

Yêu cầu vai trò: ADMIN hoặc STAFF

#### Tham số Path

| Tham số | Kiểu | Mô tả           |
|---------|------|-----------------|
| id      | int  | ID của đánh giá |

#### Request Body

```json
{
  "replyComment": "Cảm ơn quý khách đã đánh giá cao dịch vụ của chúng tôi. Rất mong được đón tiếp quý khách trong tương lai!",
  "replyBy": "Nguyễn Quản Lý"
}
```

#### Response

**Thành công (200)**

```json
{
  "statusCode": 200,
  "message": "Phản hồi đánh giá thành công",
  "data": {
    // Chi tiết đánh giá đã được cập nhật
  }
}
```

**Không tìm thấy (404)**

```json
{
  "statusCode": 404,
  "message": "Đánh giá không tìm thấy với ID: 1",
  "data": null
}
```

### 15. Cập nhật đánh giá

```
PUT /api/reviews/{id}
```

Cập nhật thông tin của một đánh giá.

#### Yêu cầu xác thực

Yêu cầu vai trò: ADMIN hoặc STAFF

#### Tham số Path

| Tham số | Kiểu | Mô tả           |
|---------|------|-----------------|
| id      | int  | ID của đánh giá |

#### Request Body

```json
{
  "isFeatured": true,
  "isAnonymous": false,
  "status": "HIDDEN"
}
```

#### Response

**Thành công (200)**

```json
{
  "statusCode": 200,
  "message": "Cập nhật đánh giá thành công",
  "data": {
    // Chi tiết đánh giá đã được cập nhật
  }
}
```

**Không tìm thấy (404)**

```json
{
  "statusCode": 404,
  "message": "Đánh giá không tìm thấy với ID: 1",
  "data": null
}
```

### 16. Xóa đánh giá

```
DELETE /api/reviews/{id}
```

Xóa một đánh giá.

#### Yêu cầu xác thực

Yêu cầu vai trò: ADMIN

#### Tham số Path

| Tham số | Kiểu | Mô tả           |
|---------|------|-----------------|
| id      | int  | ID của đánh giá |

#### Response

**Thành công (200)**

```json
{
  "statusCode": 200,
  "message": "Xóa đánh giá thành công",
  "data": null
}
```

**Không tìm thấy (404)**

```json
{
  "statusCode": 404,
  "message": "Đánh giá không tìm thấy với ID: 1",
  "data": null
}
```

### 17. Lấy thống kê đánh giá

```
GET /api/reviews/statistics
```

Lấy thông tin thống kê đánh giá.

#### Yêu cầu xác thực

Yêu cầu vai trò: ADMIN hoặc STAFF

#### Response

**Thành công (200)**

```json
{
  "statusCode": 200,
  "message": "Lấy thống kê đánh giá thành công",
  "data": {
    "totalReviews": 50,
    "pendingReviews": 10,
    "repliedReviews": 35,
    "hiddenReviews": 5,
    "averageRating": 4.2,
    "fiveStarCount": 25,
    "fourStarCount": 15,
    "threeStarCount": 7,
    "twoStarCount": 2,
    "oneStarCount": 1,
    "fiveStarPercent": 50.0,
    "fourStarPercent": 30.0,
    "threeStarPercent": 14.0,
    "twoStarPercent": 4.0,
    "oneStarPercent": 2.0
  }
}
```

### 18. Khởi tạo dữ liệu đánh giá từ file JSON

```
POST /api/reviews/init
```

Khởi tạo dữ liệu đánh giá từ file JSON.

#### Yêu cầu xác thực

Yêu cầu vai trò: ADMIN

#### Response

**Thành công (200)**

```json
{
  "statusCode": 200,
  "message": "Khởi tạo dữ liệu đánh giá từ file JSON thành công",
  "data": null
}
```

## Mã lỗi

| Mã lỗi | Mô tả                                                  |
|--------|--------------------------------------------------------|
| 400    | Bad Request - Yêu cầu không hợp lệ                    |
| 401    | Unauthorized - Chưa xác thực                          |
| 403    | Forbidden - Không có quyền truy cập                   |
| 404    | Not Found - Không tìm thấy tài nguyên                 |
| 500    | Internal Server Error - Lỗi server nội bộ              |