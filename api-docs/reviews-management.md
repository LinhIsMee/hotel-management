
# Quản lý Đánh giá (Reviews) - Tài liệu API

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

### 2. Lấy thông tin đánh giá theo ID

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

### 3. Lấy danh sách đánh giá theo số phòng

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

### 4. Tạo đánh giá mới

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

### 5. Phản hồi đánh giá

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
  "replyComment": "Cảm ơn quý khách đã đánh giá cao dịch vụ của chúng tôi.",
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

### 6. Cập nhật đánh giá

```
PUT /api/v1/reviews/update/{id}
```

Cập nhật thông tin của một đánh giá. **Lưu ý quan trọng**: Bạn phải gửi toàn bộ thông tin của đánh giá, không chỉ các trường cần cập nhật.

#### Tham số Path

| Tham số | Kiểu | Mô tả           |
|---------|------|-----------------|
| id      | int  | ID của đánh giá |

#### Request Body

Gửi đầy đủ thông tin đánh giá cùng với các trường cần cập nhật:

```json
{
  "id": 13,
  "bookingId": "uer",
  "guestName": "Nguyễn Văn A423", 
  "displayName": "Nguyễn A.22222222",
  "cleanliness": 5,
  "comfort": 5,
  "comment": "fdsfsdf",
  "createdAt": "30/03/2025 11:34",
  "facilities": 5,
  "images": [],
  "isAnonymous": true,
  "isFeatured": false,
  "location": 4,
  "rating": 3,
  "replyBy": "Admin",
  "replyComment": "dsdasd",
  "replyDate": "30/03/2025 11:39",
  "roomNumber": "34",
  "roomType": "Standard",
  "service": 5,
  "status": "REPLIED",
  "updatedAt": "30/03/2025 11:39",
  "valueForMoney": 4
}
```

#### Response

**Thành công (200)**
```json
{
  "id": 13,
  "bookingId": "uer",
  "guestName": "Nguyễn Văn A423",
  "displayName": "Nguyễn A.22222222",
  // ... thông tin đánh giá đã được cập nhật
}
```

### 7. Xóa đánh giá

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
  "status": "success",
  "message": "Xóa đánh giá thành công",
  "reviewId": 13
}
```

### 8. Lấy thống kê đánh giá

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

## Lưu ý quan trọng khi cập nhật đánh giá

1. **Gửi đầy đủ thông tin**: Khi cập nhật (PUT), phải gửi toàn bộ thông tin của đánh giá, bao gồm cả các trường không thay đổi.
2. **Giữ nguyên ID**: Luôn gửi đúng ID của đánh giá cần cập nhật trong cả URL path và trong body request.
3. **Định dạng ngày**: Định dạng ngày phải đúng chuẩn "dd/MM/yyyy HH:mm".
4. **Trạng thái hợp lệ**: Các giá trị hợp lệ cho trường status: "PENDING", "REPLIED", "HIDDEN".

## Mã lỗi

| Mã lỗi | Mô tả                                                  |
|--------|--------------------------------------------------------|
| 400    | Bad Request - Yêu cầu không hợp lệ                    |
| 401    | Unauthorized - Chưa xác thực                          |
| 403    | Forbidden - Không có quyền truy cập                   |
| 404    | Not Found - Không tìm thấy tài nguyên                 |
| 500    | Internal Server Error - Lỗi server nội bộ              |
