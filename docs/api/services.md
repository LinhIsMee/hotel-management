# API Quản lý Dịch vụ

## Mô tả

API quản lý dịch vụ cho phép thực hiện các chức năng CRUD (Create, Read, Update, Delete) đối với dịch vụ của khách sạn.

## Base URL

```
/api/services
```

## Endpoints

### 1. Lấy danh sách tất cả dịch vụ

```
GET /api/services
```

#### Mô tả
Lấy danh sách tất cả dịch vụ có trong hệ thống.

#### Quyền truy cập
- Tất cả người dùng

#### Phản hồi thành công (200 OK)
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
    // ...
  ]
}
```

### 2. Lấy dịch vụ theo ID

```
GET /api/services/{id}
```

#### Mô tả
Lấy thông tin chi tiết của một dịch vụ theo ID.

#### Tham số đường dẫn
- `id`: ID của dịch vụ cần lấy thông tin

#### Quyền truy cập
- Tất cả người dùng

#### Phản hồi thành công (200 OK)
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

#### Phản hồi lỗi (404 Not Found)
```json
{
  "status": 404,
  "message": "Không tìm thấy dịch vụ với ID: {id}",
  "timestamp": "..."
}
```

### 3. Lấy dịch vụ theo mã code

```
GET /api/services/code/{code}
```

#### Mô tả
Lấy thông tin chi tiết của một dịch vụ theo mã code.

#### Tham số đường dẫn
- `code`: Mã code của dịch vụ cần lấy thông tin

#### Quyền truy cập
- Tất cả người dùng

#### Phản hồi thành công (200 OK)
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

#### Phản hồi lỗi (404 Not Found)
```json
{
  "status": 404,
  "message": "Không tìm thấy dịch vụ với mã: {code}",
  "timestamp": "..."
}
```

### 4. Lấy dịch vụ theo loại

```
GET /api/services/type/{type}
```

#### Mô tả
Lấy danh sách dịch vụ theo loại.

#### Tham số đường dẫn
- `type`: Loại dịch vụ (ví dụ: HOUSEKEEPING, SPA, TRANSPORT, FOOD, BUSINESS, TOUR, FITNESS, OTHER)

#### Quyền truy cập
- Tất cả người dùng

#### Phản hồi thành công (200 OK)
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
    // ...
  ]
}
```

### 5. Tìm kiếm dịch vụ theo tên

```
GET /api/services/search?name={name}
```

#### Mô tả
Tìm kiếm dịch vụ theo tên (tìm kiếm không phân biệt hoa thường).

#### Tham số truy vấn
- `name`: Từ khóa tìm kiếm tên dịch vụ

#### Quyền truy cập
- Tất cả người dùng

#### Phản hồi thành công (200 OK)
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
    // ...
  ]
}
```

### 6. Lọc dịch vụ theo giá tối đa

```
GET /api/services/price?price={maxPrice}
```

#### Mô tả
Lọc danh sách dịch vụ có giá không vượt quá giá tối đa chỉ định.

#### Tham số truy vấn
- `price`: Giá tối đa để lọc dịch vụ

#### Quyền truy cập
- Tất cả người dùng

#### Phản hồi thành công (200 OK)
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
    // ...
  ]
}
```

### 7. Lấy danh sách dịch vụ khả dụng

```
GET /api/services/available
```

#### Mô tả
Lấy danh sách các dịch vụ có trạng thái khả dụng.

#### Quyền truy cập
- Tất cả người dùng

#### Phản hồi thành công (200 OK)
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
    // ...
  ]
}
```

### 8. Tạo mới dịch vụ

```
POST /api/services
```

#### Mô tả
Tạo mới một dịch vụ.

#### Quyền truy cập
- ROLE_ADMIN, ROLE_MANAGER

#### Yêu cầu
```json
{
  "name": "Dịch vụ giặt ủi",
  "code": "LAUNDRY",
  "type": "HOUSEKEEPING",
  "description": "Giặt ủi quần áo cho khách hàng",
  "price": 150000,
  "unit": "Kg",
  "isAvailable": true
}
```

#### Phản hồi thành công (201 Created)
```json
{
  "statusCode": 201,
  "message": "Tạo mới dịch vụ thành công",
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

#### Phản hồi lỗi (400 Bad Request)
```json
{
  "status": 400,
  "message": "Mã dịch vụ đã tồn tại: LAUNDRY",
  "timestamp": "..."
}
```

### 9. Cập nhật dịch vụ

```
PUT /api/services/{id}
```

#### Mô tả
Cập nhật thông tin dịch vụ theo ID.

#### Tham số đường dẫn
- `id`: ID của dịch vụ cần cập nhật

#### Quyền truy cập
- ROLE_ADMIN, ROLE_MANAGER

#### Yêu cầu
```json
{
  "name": "Dịch vụ giặt ủi cao cấp",
  "code": "LAUNDRY",
  "type": "HOUSEKEEPING",
  "description": "Giặt ủi quần áo cao cấp cho khách hàng",
  "price": 200000,
  "unit": "Kg",
  "isAvailable": true
}
```

#### Phản hồi thành công (200 OK)
```json
{
  "statusCode": 200,
  "message": "Cập nhật dịch vụ thành công",
  "data": {
    "id": 1,
    "name": "Dịch vụ giặt ủi cao cấp",
    "code": "LAUNDRY",
    "type": "HOUSEKEEPING",
    "description": "Giặt ủi quần áo cao cấp cho khách hàng",
    "price": 200000,
    "unit": "Kg",
    "isAvailable": true,
    "createdAt": "01/01/2023",
    "updatedAt": "10/05/2023"
  }
}
```

#### Phản hồi lỗi (404 Not Found)
```json
{
  "status": 404,
  "message": "Không tìm thấy dịch vụ với ID: {id}",
  "timestamp": "..."
}
```

### 10. Xóa dịch vụ

```
DELETE /api/services/{id}
```

#### Mô tả
Xóa dịch vụ theo ID.

#### Tham số đường dẫn
- `id`: ID của dịch vụ cần xóa

#### Quyền truy cập
- ROLE_ADMIN, ROLE_MANAGER

#### Phản hồi thành công (200 OK)
```json
{
  "statusCode": 200,
  "message": "Xóa dịch vụ thành công",
  "data": null
}
```

#### Phản hồi lỗi (404 Not Found)
```json
{
  "status": 404,
  "message": "Không tìm thấy dịch vụ với ID: {id}",
  "timestamp": "..."
}
```

### 11. Khởi tạo dữ liệu dịch vụ từ file JSON

```
POST /api/services/init
```

#### Mô tả
Khởi tạo dữ liệu dịch vụ từ file JSON. API này chỉ được sử dụng khi cần phục hồi dữ liệu hoặc khởi tạo dữ liệu ban đầu. Chỉ hoạt động khi không có dịch vụ nào trong hệ thống.

#### Quyền truy cập
- ROLE_ADMIN

#### Phản hồi thành công (200 OK)
```json
{
  "statusCode": 200,
  "message": "Khởi tạo dữ liệu dịch vụ thành công",
  "data": null
}
``` 