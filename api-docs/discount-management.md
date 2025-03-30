# API Quản Lý Mã Giảm Giá (Discount Management)

## 1. Lấy Tất Cả Mã Giảm Giá

Lấy danh sách tất cả các mã giảm giá trong hệ thống.

**Endpoint:** `GET /api/discounts`

**Phản hồi:**
- `200 OK`: Trả về danh sách tất cả mã giảm giá

**Ví dụ phản hồi thành công:**
```json
[
  {
    "id": 1,
    "code": "SUMMER2023",
    "discountType": "PERCENT",
    "discountValue": 0.15,
    "validFrom": "2023-06-01",
    "validTo": "2023-08-31",
    "maxUses": 100,
    "usedCount": 45,
    "valid": true
  },
  {
    "id": 2,
    "code": "WELCOME",
    "discountType": "FIXED",
    "discountValue": 100000.0,
    "validFrom": "2023-01-01",
    "validTo": "2023-12-31",
    "maxUses": 1000,
    "usedCount": 387,
    "valid": true
  }
]
```

## 2. Lấy Mã Giảm Giá Theo ID

Lấy chi tiết thông tin của một mã giảm giá dựa trên ID.

**Endpoint:** `GET /api/discounts/{id}`

**Tham số đường dẫn:**
- `id` (Integer): ID của mã giảm giá cần lấy thông tin

**Phản hồi:**
- `200 OK`: Trả về chi tiết của mã giảm giá
- `404 Not Found`: Mã giảm giá không tồn tại

**Ví dụ phản hồi thành công:**
```json
{
  "id": 1,
  "code": "SUMMER2023",
  "discountType": "PERCENT",
  "discountValue": 0.15,
  "validFrom": "2023-06-01",
  "validTo": "2023-08-31",
  "maxUses": 100,
  "usedCount": 45,
  "valid": true
}
```

## 3. Lấy Mã Giảm Giá Theo Code

Lấy chi tiết thông tin của một mã giảm giá dựa trên mã code.

**Endpoint:** `GET /api/discounts/code/{code}`

**Tham số đường dẫn:**
- `code` (String): Mã code của discount cần lấy thông tin

**Phản hồi:**
- `200 OK`: Trả về chi tiết của mã giảm giá
- `404 Not Found`: Mã giảm giá không tồn tại

**Ví dụ phản hồi thành công:**
```json
{
  "id": 1,
  "code": "SUMMER2023",
  "discountType": "PERCENT",
  "discountValue": 0.15,
  "validFrom": "2023-06-01",
  "validTo": "2023-08-31",
  "maxUses": 100,
  "usedCount": 45,
  "valid": true
}
```

## 4. Lấy Danh Sách Mã Giảm Giá Đang Hoạt Động

Lấy danh sách các mã giảm giá đang có hiệu lực trong thời gian hiện tại và chưa hết số lượt sử dụng.

**Endpoint:** `GET /api/discounts/active`

**Phản hồi:**
- `200 OK`: Trả về danh sách mã giảm giá đang hoạt động

**Ví dụ phản hồi thành công:**
```json
[
  {
    "id": 1,
    "code": "SUMMER2023",
    "discountType": "PERCENT",
    "discountValue": 0.15,
    "validFrom": "2023-06-01",
    "validTo": "2023-08-31",
    "maxUses": 100,
    "usedCount": 45,
    "valid": true
  },
  {
    "id": 2,
    "code": "WELCOME",
    "discountType": "FIXED",
    "discountValue": 100000.0,
    "validFrom": "2023-01-01",
    "validTo": "2023-12-31",
    "maxUses": 1000,
    "usedCount": 387,
    "valid": true
  }
]
```

## 5. Tạo Mã Giảm Giá Mới

Tạo một mã giảm giá mới.

**Endpoint:** `POST /api/discounts`

**Quyền yêu cầu:** `ROLE_ADMIN`

**Body Request:**
```json
{
  "code": "LOYAL2023",
  "discountType": "PERCENT",
  "discountValue": 0.25,
  "validFrom": "2023-07-01",
  "validTo": "2023-12-31",
  "maxUses": 200,
  "usedCount": 0
}
```

**Phản hồi:**
- `201 Created`: Mã giảm giá đã được tạo thành công
- `400 Bad Request`: Dữ liệu không hợp lệ hoặc mã code đã tồn tại

**Ví dụ phản hồi thành công:**
```json
{
  "id": 3,
  "code": "LOYAL2023",
  "discountType": "PERCENT",
  "discountValue": 0.25,
  "validFrom": "2023-07-01",
  "validTo": "2023-12-31",
  "maxUses": 200,
  "usedCount": 0,
  "valid": true
}
```

## 6. Tạo Nhiều Mã Giảm Giá Ngẫu Nhiên

Tạo nhiều mã giảm giá ngẫu nhiên với các thuộc tính tương tự.

**Endpoint:** `POST /api/discounts/generate`

**Quyền yêu cầu:** `ROLE_ADMIN`

**Body Request:**
```json
{
  "prefix": "SUMMER", 
  "discountType": "PERCENT",
  "discountValue": 0.15,
  "validFrom": "2023-06-01",
  "validTo": "2023-08-31",
  "maxUses": 1,
  "count": 5
}
```

**Phản hồi:**
- `201 Created`: Các mã giảm giá đã được tạo thành công
- `400 Bad Request`: Dữ liệu không hợp lệ

**Ví dụ phản hồi thành công:**
```json
{
  "message": "Đã tạo 5 mã giảm giá ngẫu nhiên",
  "discounts": [
    {
      "id": 4,
      "code": "SUMMER-DKFG3J2L",
      "discountType": "PERCENT",
      "discountValue": 0.15,
      "validFrom": "2023-06-01",
      "validTo": "2023-08-31",
      "maxUses": 1,
      "usedCount": 0,
      "valid": true
    },
    {
      "id": 5,
      "code": "SUMMER-K78FH3G5",
      "discountType": "PERCENT",
      "discountValue": 0.15,
      "validFrom": "2023-06-01",
      "validTo": "2023-08-31",
      "maxUses": 1,
      "usedCount": 0,
      "valid": true
    },
    ... // 3 mã nữa
  ]
}
```

## 7. Cập Nhật Thông Tin Mã Giảm Giá

Cập nhật thông tin của một mã giảm giá.

**Endpoint:** `PUT /api/discounts/{id}`

**Quyền yêu cầu:** `ROLE_ADMIN`

**Tham số đường dẫn:**
- `id` (Integer): ID của mã giảm giá cần cập nhật

**Body Request:**
```json
{
  "code": "LOYAL2023",
  "discountType": "PERCENT",
  "discountValue": 0.30,
  "validFrom": "2023-07-01",
  "validTo": "2023-12-31",
  "maxUses": 300,
  "usedCount": 0
}
```

**Phản hồi:**
- `200 OK`: Mã giảm giá đã được cập nhật thành công
- `400 Bad Request`: Dữ liệu không hợp lệ hoặc mã code đã tồn tại
- `404 Not Found`: Mã giảm giá không tồn tại

**Ví dụ phản hồi thành công:**
```json
{
  "id": 3,
  "code": "LOYAL2023",
  "discountType": "PERCENT",
  "discountValue": 0.30,
  "validFrom": "2023-07-01",
  "validTo": "2023-12-31",
  "maxUses": 300,
  "usedCount": 0,
  "valid": true
}
```

## 8. Xóa Mã Giảm Giá

Xóa một mã giảm giá khỏi hệ thống.

**Endpoint:** `DELETE /api/discounts/{id}`

**Quyền yêu cầu:** `ROLE_ADMIN`

**Tham số đường dẫn:**
- `id` (Integer): ID của mã giảm giá cần xóa

**Phản hồi:**
- `204 No Content`: Mã giảm giá đã được xóa thành công
- `404 Not Found`: Mã giảm giá không tồn tại

## 9. Kiểm Tra Tính Hợp Lệ Của Mã Giảm Giá

Kiểm tra xem một mã giảm giá có hợp lệ hay không.

**Endpoint:** `GET /api/discounts/validate/{code}`

**Tham số đường dẫn:**
- `code` (String): Mã code cần kiểm tra

**Phản hồi:**
- `200 OK`: Trả về trạng thái hợp lệ của mã giảm giá (true hoặc false)

**Ví dụ phản hồi thành công:**
```json
true
```

## 10. Áp Dụng Mã Giảm Giá Vào Số Tiền

Áp dụng một mã giảm giá vào số tiền và tính toán số tiền sau khi giảm giá.

**Endpoint:** `GET /api/discounts/apply`

**Tham số query:**
- `code` (String): Mã giảm giá cần áp dụng
- `amount` (Double): Số tiền trước khi áp dụng mã giảm giá

**Phản hồi:**
- `200 OK`: Trả về thông tin về số tiền sau khi đã áp dụng mã giảm giá
- `400 Bad Request`: Mã giảm giá đã hết hạn hoặc đã hết lượt sử dụng
- `404 Not Found`: Mã giảm giá không tồn tại

**Ví dụ phản hồi thành công:**
```json
{
  "originalAmount": 1000000.0,
  "discountedAmount": 850000.0,
  "discountAmount": 150000.0,
  "discountCode": "SUMMER2023"
}
```

## 11. Sử Dụng Mã Giảm Giá

Tăng số lần sử dụng của một mã giảm giá lên 1.

**Endpoint:** `POST /api/discounts/use/{code}`

**Quyền yêu cầu:** `ROLE_ADMIN` hoặc `ROLE_STAFF`

**Tham số đường dẫn:**
- `code` (String): Mã code cần cập nhật

**Phản hồi:**
- `200 OK`: Số lần sử dụng của mã giảm giá đã được cập nhật
- `404 Not Found`: Mã giảm giá không tồn tại

**Ví dụ phản hồi thành công:**
```json
{
  "message": "Đã cập nhật số lần sử dụng của mã giảm giá: SUMMER2023"
}
```

## Các Loại Mã Giảm Giá

Hệ thống hỗ trợ hai loại mã giảm giá:

1. `PERCENT`: Giảm giá theo phần trăm (giá trị từ 0 đến 1)
   - Ví dụ: 0.15 tương đương với giảm 15%

2. `FIXED`: Giảm giá theo số tiền cố định
   - Ví dụ: 100000 tương đương với giảm 100.000 VND 