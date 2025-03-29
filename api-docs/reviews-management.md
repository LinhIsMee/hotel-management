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
  "roomId": 101,
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

### 1. Lấy tất cả đánh giá

```
GET /api/v1/reviews/
```

Trả về danh sách tất cả các đánh giá.

#### Response

**Thành công (200)**

```json
[
  {
    "id": 1,
    "bookingId": "BK-2023090501",
    "guestName": "Nguyễn Văn An",
    "displayName": "Nguyễn V.",
    "roomId": 101,
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
  },
  // ... các đánh giá khác
]
```

### 2. Lấy danh sách đánh giá có phân trang

```
GET /api/v1/reviews?page=0&size=10
```

Trả về danh sách các đánh giá có phân trang.

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
  "content": [
    {
      "id": 1,
      "bookingId": "BK-2023090501",
      // ... thông tin đánh giá
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
```

### 3. Lấy thông tin đánh giá theo ID

```
GET /api/v1/reviews/{id}
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
  "id": 1,
  "bookingId": "BK-2023090501",
  "guestName": "Nguyễn Văn An",
  "displayName": "Nguyễn V.",
  "roomId": 101,
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

**Không tìm thấy (404)**

```json
{
  "statusCode": 404,
  "message": "Đánh giá không tìm thấy với ID: 1",
  "data": null
}
```

### 4. Lấy danh sách đánh giá theo số phòng (Room ID)

```
GET /api/v1/reviews/room/{roomId}
```

Trả về danh sách các đánh giá theo ID phòng.

#### Tham số Path

| Tham số | Kiểu | Mô tả      |
|---------|------|------------|
| roomId  | int  | ID của phòng |

#### Response

**Thành công (200)**

```json
[
  {
    "id": 1,
    "bookingId": "BK-2023090501",
    // ... thông tin đánh giá
  },
  // ... các đánh giá khác của phòng
]
```

### 5. Tạo đánh giá mới

```
POST /api/v1/reviews/
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
  "id": 1,
  "bookingId": "BK-2023090501",
  // ... thông tin đánh giá đã được tạo
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

### 6. Phản hồi đánh giá

```
POST /api/v1/reviews/{id}/reply
```

Phản hồi một đánh giá.

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
  "id": 1,
  "bookingId": "BK-2023090501",
  // ... thông tin đánh giá đã được cập nhật với phản hồi
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

### 7. Cập nhật đánh giá

```
PUT /api/v1/reviews/update/{id}
```

Cập nhật thông tin của một đánh giá.

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
  "id": 1,
  "bookingId": "BK-2023090501",
  // ... thông tin đánh giá đã được cập nhật
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

### 8. Xóa đánh giá

```
DELETE /api/v1/reviews/{id}
```

Xóa một đánh giá.

#### Tham số Path

| Tham số | Kiểu | Mô tả           |
|---------|------|-----------------|
| id      | int  | ID của đánh giá |

#### Response

**Thành công (200)**

```json
{
  "id": 1,
  "bookingId": "BK-2023090501",
  // ... thông tin đánh giá đã bị xóa
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

### 9. Lấy thống kê đánh giá

```
GET /api/v1/reviews/statistics
```

Lấy thông tin thống kê đánh giá.

#### Response

**Thành công (200)**

```json
{
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
```

## Trạng thái của đánh giá (Review Status)

| Trạng thái | Mô tả                                           |
|------------|------------------------------------------------|
| PENDING    | Đánh giá mới, chưa được xử lý                   |
| REPLIED    | Đánh giá đã được phản hồi                       |
| HIDDEN     | Đánh giá bị ẩn (không hiển thị công khai)       |

## Mã lỗi

| Mã lỗi | Mô tả                                                  |
|--------|--------------------------------------------------------|
| 400    | Bad Request - Yêu cầu không hợp lệ                    |
| 401    | Unauthorized - Chưa xác thực                          |
| 403    | Forbidden - Không có quyền truy cập                   |
| 404    | Not Found - Không tìm thấy tài nguyên                 |
| 500    | Internal Server Error - Lỗi server nội bộ              |