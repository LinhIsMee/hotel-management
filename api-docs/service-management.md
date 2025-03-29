# API Quản lý Dịch vụ

## Giới thiệu

API quản lý dịch vụ cho phép người dùng (có quyền phù hợp) thực hiện các hoạt động CRUD (Tạo, Đọc, Cập nhật, Xóa) đối với dịch vụ khách sạn. Dịch vụ khách sạn là các tiện ích bổ sung mà khách hàng có thể sử dụng trong thời gian lưu trú.

## Base URL

```
http://localhost:9000/api
```

## Xác thực

Tất cả API đều yêu cầu xác thực bằng JWT token trừ khi có ghi chú khác. Token phải được gửi trong header `Authorization` với định dạng `Bearer {token}`.

## Phân quyền

- **ROLE_ADMIN**: Có quyền truy cập tất cả các API
- **ROLE_MANAGER**: Có quyền truy cập tất cả các API quản lý dịch vụ
- **ROLE_STAFF**: Có quyền truy cập API xem dịch vụ
- **ROLE_USER**: Có quyền truy cập API xem dịch vụ

## Cấu trúc Dịch vụ

```json
{
  "id": 1,
  "name": "Dịch vụ giặt ủi",
  "code": "LAUNDRY",
  "type": "HOUSEKEEPING",
  "description": "Giặt ủi quần áo cho khách hàng",
  "price": 150000,
  "unit": "Kg",
  "isAvailable": true,
  "createdAt": "01/01/2023",
  "updatedAt": "01/01/2023"
}
```

## API Endpoints

### 1. Lấy tất cả dịch vụ

#### Request

```http
GET /api/services
```

#### Headers

| Tên | Mô tả |
| --- | --- |
| Authorization | Bearer {token} |

#### Response

```http
Status: 200 OK
```

```json
{
  "statusCode": 200,
  "message": "Lấy danh sách dịch vụ thành công",
  "data": [
    {
      "id": 1,
      "name": "Dịch vụ giặt ủi",
      "code": "LAUNDRY",
      "type": "HOUSEKEEPING",
      "description": "Giặt ủi quần áo cho khách hàng",
      "price": 150000,
      "unit": "Kg",
      "isAvailable": true,
      "createdAt": "01/01/2023",
      "updatedAt": "01/01/2023"
    },
    {
      "id": 2,
      "name": "Spa - Massage toàn thân",
      "code": "SPA_FULL_BODY",
      "type": "SPA",
      "description": "Massage toàn thân thư giãn với tinh dầu thiên nhiên",
      "price": 650000,
      "unit": "Giờ",
      "isAvailable": true,
      "createdAt": "05/01/2023",
      "updatedAt": "05/01/2023"
    }
    // ... Các dịch vụ khác
  ]
}
```

### 2. Lấy dịch vụ theo ID

#### Request

```http
GET /api/services/{id}
```

#### Parameters

| Tên | Mô tả |
| --- | --- |
| id | ID của dịch vụ cần lấy thông tin |

#### Headers

| Tên | Mô tả |
| --- | --- |
| Authorization | Bearer {token} |

#### Response Success

```http
Status: 200 OK
```

```json
{
  "statusCode": 200,
  "message": "Lấy thông tin dịch vụ thành công",
  "data": {
    "id": 1,
    "name": "Dịch vụ giặt ủi",
    "code": "LAUNDRY",
    "type": "HOUSEKEEPING",
    "description": "Giặt ủi quần áo cho khách hàng",
    "price": 150000,
    "unit": "Kg",
    "isAvailable": true,
    "createdAt": "01/01/2023",
    "updatedAt": "01/01/2023"
  }
}
```

#### Response Error

```http
Status: 404 Not Found
```

```json
{
  "status": 404,
  "message": "Không tìm thấy dịch vụ với ID: 1",
  "timestamp": "2023-06-20T10:15:30Z",
  "details": "URI=/api/services/1"
}
```

### 3. Lấy dịch vụ theo mã code

#### Request

```http
GET /api/services/code/{code}
```

#### Parameters

| Tên | Mô tả |
| --- | --- |
| code | Mã code của dịch vụ cần lấy thông tin |

#### Headers

| Tên | Mô tả |
| --- | --- |
| Authorization | Bearer {token} |

#### Response Success

```http
Status: 200 OK
```

```json
{
  "statusCode": 200,
  "message": "Lấy thông tin dịch vụ thành công",
  "data": {
    "id": 1,
    "name": "Dịch vụ giặt ủi",
    "code": "LAUNDRY",
    "type": "HOUSEKEEPING",
    "description": "Giặt ủi quần áo cho khách hàng",
    "price": 150000,
    "unit": "Kg",
    "isAvailable": true,
    "createdAt": "01/01/2023",
    "updatedAt": "01/01/2023"
  }
}
```

#### Response Error

```http
Status: 404 Not Found
```

```json
{
  "status": 404,
  "message": "Không tìm thấy dịch vụ với mã: LAUNDRY",
  "timestamp": "2023-06-20T10:16:30Z",
  "details": "URI=/api/services/code/LAUNDRY"
}
```

### 4. Lấy dịch vụ theo loại

#### Request

```http
GET /api/services/type/{type}
```

#### Parameters

| Tên | Mô tả |
| --- | --- |
| type | Loại dịch vụ (HOUSEKEEPING, SPA, TRANSPORT, FOOD, BUSINESS, TOUR, FITNESS, OTHER) |

#### Headers

| Tên | Mô tả |
| --- | --- |
| Authorization | Bearer {token} |

#### Response

```http
Status: 200 OK
```

```json
{
  "statusCode": 200,
  "message": "Lấy danh sách dịch vụ theo loại thành công",
  "data": [
    {
      "id": 1,
      "name": "Dịch vụ giặt ủi",
      "code": "LAUNDRY",
      "type": "HOUSEKEEPING",
      "description": "Giặt ủi quần áo cho khách hàng",
      "price": 150000,
      "unit": "Kg",
      "isAvailable": true,
      "createdAt": "01/01/2023",
      "updatedAt": "01/01/2023"
    },
    {
      "id": 3,
      "name": "Dịch vụ giặt khô",
      "code": "DRY_CLEANING",
      "type": "HOUSEKEEPING",
      "description": "Giặt khô quần áo cao cấp",
      "price": 350000,
      "unit": "Bộ",
      "isAvailable": true,
      "createdAt": "01/01/2023",
      "updatedAt": "01/01/2023"
    }
    // ... Các dịch vụ khác cùng loại
  ]
}
```

### 5. Tìm kiếm dịch vụ theo tên

#### Request

```http
GET /api/services/search?name={name}
```

#### Query Parameters

| Tên | Mô tả |
| --- | --- |
| name | Từ khóa tìm kiếm tên dịch vụ |

#### Headers

| Tên | Mô tả |
| --- | --- |
| Authorization | Bearer {token} |

#### Response

```http
Status: 200 OK
```

```json
{
  "statusCode": 200,
  "message": "Tìm kiếm dịch vụ thành công",
  "data": [
    {
      "id": 1,
      "name": "Dịch vụ giặt ủi",
      "code": "LAUNDRY",
      "type": "HOUSEKEEPING",
      "description": "Giặt ủi quần áo cho khách hàng",
      "price": 150000,
      "unit": "Kg",
      "isAvailable": true,
      "createdAt": "01/01/2023",
      "updatedAt": "01/01/2023"
    },
    {
      "id": 2,
      "name": "Dịch vụ giặt ủi nhanh",
      "code": "FAST_LAUNDRY",
      "type": "HOUSEKEEPING",
      "description": "Giặt ủi quần áo nhanh trong vòng 3 giờ",
      "price": 250000,
      "unit": "Kg",
      "isAvailable": true,
      "createdAt": "01/01/2023",
      "updatedAt": "01/01/2023"
    }
    // ... Các dịch vụ khác phù hợp với từ khóa tìm kiếm
  ]
}
```

### 6. Lọc dịch vụ theo giá tối đa

#### Request

```http
GET /api/services/price?price={maxPrice}
```

#### Query Parameters

| Tên | Mô tả |
| --- | --- |
| price | Giá tối đa để lọc dịch vụ |

#### Headers

| Tên | Mô tả |
| --- | --- |
| Authorization | Bearer {token} |

#### Response

```http
Status: 200 OK
```

```json
{
  "statusCode": 200,
  "message": "Lọc dịch vụ theo giá thành công",
  "data": [
    {
      "id": 1,
      "name": "Dịch vụ giặt ủi",
      "code": "LAUNDRY",
      "type": "HOUSEKEEPING",
      "description": "Giặt ủi quần áo cho khách hàng",
      "price": 150000,
      "unit": "Kg",
      "isAvailable": true,
      "createdAt": "01/01/2023",
      "updatedAt": "01/01/2023"
    },
    {
      "id": 19,
      "name": "Phòng tập gym",
      "code": "GYM",
      "type": "FITNESS",
      "description": "Sử dụng phòng tập gym của khách sạn",
      "price": 100000,
      "unit": "Ngày",
      "isAvailable": true,
      "createdAt": "05/02/2023",
      "updatedAt": "05/02/2023"
    }
    // ... Các dịch vụ khác có giá dưới giá tối đa
  ]
}
```

### 7. Lấy danh sách dịch vụ khả dụng

#### Request

```http
GET /api/services/available
```

#### Headers

| Tên | Mô tả |
| --- | --- |
| Authorization | Bearer {token} |

#### Response

```http
Status: 200 OK
```

```json
{
  "statusCode": 200,
  "message": "Lấy danh sách dịch vụ khả dụng thành công",
  "data": [
    {
      "id": 1,
      "name": "Dịch vụ giặt ủi",
      "code": "LAUNDRY",
      "type": "HOUSEKEEPING",
      "description": "Giặt ủi quần áo cho khách hàng",
      "price": 150000,
      "unit": "Kg",
      "isAvailable": true,
      "createdAt": "01/01/2023",
      "updatedAt": "01/01/2023"
    },
    // ... Các dịch vụ khác có trạng thái khả dụng
  ]
}
```

### 8. Tạo mới dịch vụ

#### Request

```http
POST /api/services
Content-Type: application/json
```

#### Headers

| Tên | Mô tả |
| --- | --- |
| Authorization | Bearer {token} |
| Content-Type | application/json |

#### Request Body

```json
{
  "name": "Dịch vụ đưa đón sân bay VIP",
  "code": "AIRPORT_VIP",
  "type": "TRANSPORT",
  "description": "Dịch vụ đưa đón sân bay bằng xe sang trọng",
  "price": 800000,
  "unit": "Chiều",
  "isAvailable": true
}
```

#### Response Success

```http
Status: 201 Created
```

```json
{
  "statusCode": 201,
  "message": "Tạo mới dịch vụ thành công",
  "data": {
    "id": 21,
    "name": "Dịch vụ đưa đón sân bay VIP",
    "code": "AIRPORT_VIP",
    "type": "TRANSPORT",
    "description": "Dịch vụ đưa đón sân bay bằng xe sang trọng",
    "price": 800000,
    "unit": "Chiều",
    "isAvailable": true,
    "createdAt": "20/06/2023",
    "updatedAt": "20/06/2023"
  }
}
```

#### Response Error

```http
Status: 400 Bad Request
```

```json
{
  "status": 400,
  "message": "Mã dịch vụ đã tồn tại: AIRPORT_VIP",
  "timestamp": "2023-06-20T10:30:30Z",
  "details": "URI=/api/services"
}
```

Hoặc:

```http
Status: 400 Bad Request
```

```json
{
  "status": 400,
  "message": "Dữ liệu không hợp lệ",
  "timestamp": "2023-06-20T10:30:30Z",
  "details": [
    "name: không được để trống",
    "code: không được để trống",
    "price: phải lớn hơn hoặc bằng 0"
  ]
}
```

### 9. Cập nhật dịch vụ

#### Request

```http
PUT /api/services/{id}
Content-Type: application/json
```

#### Parameters

| Tên | Mô tả |
| --- | --- |
| id | ID của dịch vụ cần cập nhật |

#### Headers

| Tên | Mô tả |
| --- | --- |
| Authorization | Bearer {token} |
| Content-Type | application/json |

#### Request Body

```json
{
  "name": "Dịch vụ đưa đón sân bay VIP Plus",
  "code": "AIRPORT_VIP",
  "type": "TRANSPORT",
  "description": "Dịch vụ đưa đón sân bay bằng xe sang trọng kèm đồ ăn nhẹ",
  "price": 1000000,
  "unit": "Chiều",
  "isAvailable": true
}
```

#### Response Success

```http
Status: 200 OK
```

```json
{
  "statusCode": 200,
  "message": "Cập nhật dịch vụ thành công",
  "data": {
    "id": 21,
    "name": "Dịch vụ đưa đón sân bay VIP Plus",
    "code": "AIRPORT_VIP",
    "type": "TRANSPORT",
    "description": "Dịch vụ đưa đón sân bay bằng xe sang trọng kèm đồ ăn nhẹ",
    "price": 1000000,
    "unit": "Chiều",
    "isAvailable": true,
    "createdAt": "20/06/2023",
    "updatedAt": "20/06/2023"
  }
}
```

#### Response Error

```http
Status: 404 Not Found
```

```json
{
  "status": 404,
  "message": "Không tìm thấy dịch vụ với ID: 21",
  "timestamp": "2023-06-20T10:35:30Z",
  "details": "URI=/api/services/21"
}
```

Hoặc:

```http
Status: 400 Bad Request
```

```json
{
  "status": 400,
  "message": "Mã dịch vụ đã tồn tại: AIRPORT_VIP",
  "timestamp": "2023-06-20T10:35:30Z",
  "details": "URI=/api/services/21"
}
```

### 10. Xóa dịch vụ

#### Request

```http
DELETE /api/services/{id}
```

#### Parameters

| Tên | Mô tả |
| --- | --- |
| id | ID của dịch vụ cần xóa |

#### Headers

| Tên | Mô tả |
| --- | --- |
| Authorization | Bearer {token} |

#### Response Success

```http
Status: 200 OK
```

```json
{
  "statusCode": 200,
  "message": "Xóa dịch vụ thành công",
  "data": null
}
```

#### Response Error

```http
Status: 404 Not Found
```

```json
{
  "status": 404,
  "message": "Không tìm thấy dịch vụ với ID: 21",
  "timestamp": "2023-06-20T10:40:30Z",
  "details": "URI=/api/services/21"
}
```

### 11. Khởi tạo dữ liệu dịch vụ từ file JSON

#### Request

```http
POST /api/services/init
```

#### Headers

| Tên | Mô tả |
| --- | --- |
| Authorization | Bearer {token} |

#### Access Rights
Chỉ ROLE_ADMIN mới có quyền truy cập endpoint này

#### Response Success

```http
Status: 200 OK
```

```json
{
  "statusCode": 200,
  "message": "Khởi tạo dữ liệu dịch vụ thành công",
  "data": null
}
```

## Mã lỗi

| Mã lỗi | Mô tả |
| --- | --- |
| 200 | Thành công |
| 201 | Tạo thành công |
| 400 | Yêu cầu không hợp lệ |
| 401 | Chưa xác thực |
| 403 | Không có quyền truy cập |
| 404 | Không tìm thấy tài nguyên |
| 500 | Lỗi server |

## Dữ liệu tham khảo

### Loại dịch vụ
- HOUSEKEEPING: Dịch vụ vệ sinh, giặt ủi
- SPA: Dịch vụ spa, massage
- TRANSPORT: Dịch vụ đưa đón, thuê xe
- FOOD: Dịch vụ ăn uống
- BUSINESS: Dịch vụ kinh doanh (phòng họp, tổ chức sự kiện)
- TOUR: Dịch vụ tour, tham quan
- FITNESS: Dịch vụ thể dục, thể thao
- OTHER: Các dịch vụ khác 