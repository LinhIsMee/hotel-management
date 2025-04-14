# API Quản Lý Đặt Phòng (Booking Management)

## API Cho Người Dùng Thông Thường

### 1. Lấy Thông Tin Booking Theo ID

Lấy chi tiết thông tin của một booking dựa trên ID.

**Endpoint:** `GET /api/v1/bookings/{id}`

**Tham số đường dẫn:**
- `id` (Integer): ID của booking cần lấy thông tin

**Phản hồi:**
- `200 OK`: Trả về chi tiết của booking
- `404 Not Found`: Booking không tồn tại

**Ví dụ phản hồi thành công:**
```json
{
  "id": 1,
  "userId": 2,
  "fullName": "Nguyễn Văn A",
  "nationalId": "025123456789",
  "email": "example@gmail.com",
  "phone": "0912345678",
  "rooms": [
    {
      "roomId": 1,
      "roomNumber": "101",
      "roomType": "Deluxe",
      "price": 1500000
    }
  ],
  "checkInDate": "2023-05-01",
  "checkOutDate": "2023-05-03",
  "totalPrice": 3000000,
  "finalPrice": 2700000,
  "discountCode": "SUMMER10",
  "discountValue": 0.1,
  "discountType": "PERCENT",
  "status": "CONFIRMED",
  "paymentMethod": "VnPay",
  "paymentStatus": "PAID",
  "paymentDate": "2023-04-29",
  "createdAt": "29-04-2023 15:30:45"
}
```

### 2. Lấy Danh Sách Booking Theo UserId

Lấy danh sách tất cả các booking của một người dùng.

**Endpoint:** `GET /api/v1/bookings/user/{userId}`

**Tham số đường dẫn:**
- `userId` (Integer): ID của người dùng

**Phản hồi:**
- `200 OK`: Trả về danh sách booking
- `404 Not Found`: Người dùng không tồn tại

**Yêu cầu bảo mật:**
- Yêu cầu user đã đăng nhập (`ROLE_USER`)

**Ví dụ phản hồi thành công:**
```json
[
  {
    "id": 1,
    "userId": 2,
    "fullName": "Nguyễn Văn A",
    "nationalId": "025123456789",
    "email": "example@gmail.com",
    "phone": "0912345678",
    "rooms": [
      {
        "roomId": 1,
        "roomNumber": "101",
        "roomType": "Deluxe",
        "price": 1500000
      }
    ],
    "checkInDate": "2023-05-01",
    "checkOutDate": "2023-05-03",
    "totalPrice": 3000000,
    "finalPrice": 2700000,
    "discountCode": "SUMMER10",
    "discountValue": 0.1,
    "discountType": "PERCENT",
    "status": "CONFIRMED",
    "paymentMethod": "VnPay",
    "paymentStatus": "PAID",
    "paymentDate": "2023-04-29",
    "createdAt": "29-04-2023 15:30:45"
  }
]
```

### 3. Lấy Danh Sách Phòng Đã Đặt Trong Khoảng Thời Gian

Lấy danh sách các phòng đã được đặt trong một khoảng thời gian cụ thể.

**Endpoint:** `GET /api/v1/bookings/booked-rooms`

**Tham số query:**
- `startDate` (Date, định dạng ISO): Ngày bắt đầu khoảng thời gian (YYYY-MM-DD)
- `endDate` (Date, định dạng ISO): Ngày kết thúc khoảng thời gian (YYYY-MM-DD)

**Phản hồi:**
- `200 OK`: Trả về danh sách phòng đã đặt trong khoảng thời gian

**Ví dụ phản hồi thành công:**
```json
[
  {
    "roomId": 1,
    "roomNumber": "101",
    "roomType": "Deluxe",
    "price": 1500000
  },
  {
    "roomId": 2,
    "roomNumber": "102",
    "roomType": "Deluxe",
    "price": 1500000
  }
]
```

### 4. Tạo Mới Booking (Cho Người Dùng)

Tạo một booking mới với quy trình thanh toán VNPay.

**Endpoint:** `POST /api/v1/bookings/create`

**Yêu cầu bảo mật:**
- Yêu cầu user đã đăng nhập (`ROLE_USER`)

**Body Request:**
```json
{
  "userId": 2,
  "fullName": "Nguyễn Văn A",
  "nationalId": "025123456789",
  "email": "example@gmail.com",
  "phone": "0912345678",
  "roomIds": [1, 2],
  "checkInDate": "2023-05-01",
  "checkOutDate": "2023-05-03",
  "totalPrice": 3000000,
  "discountCode": "SUMMER10",
  "status": "PENDING"
}
```

**Phản hồi:**
- `201 Created`: Booking đã được tạo thành công
- `400 Bad Request`: Dữ liệu không hợp lệ
- `404 Not Found`: Không tìm thấy phòng hoặc mã giảm giá

**Ví dụ phản hồi thành công:**
```json
{
  "id": 1,
  "userId": 2,
  "fullName": "Nguyễn Văn A",
  "nationalId": "025123456789",
  "email": "example@gmail.com",
  "phone": "0912345678",
  "rooms": [
    {
      "roomId": 1,
      "roomNumber": "101",
      "roomType": "Deluxe",
      "price": 1500000
    },
    {
      "roomId": 2,
      "roomNumber": "102",
      "roomType": "Deluxe",
      "price": 1500000
    }
  ],
  "checkInDate": "2023-05-01",
  "checkOutDate": "2023-05-03",
  "totalPrice": 3000000,
  "finalPrice": 2700000,
  "discountCode": "SUMMER10",
  "discountValue": 0.1,
  "discountType": "PERCENT",
  "status": "PENDING",
  "paymentMethod": null,
  "paymentStatus": "UNPAID",
  "paymentDate": null,
  "createdAt": "29-04-2023 15:30:45"
}
```

### 5. Cập Nhật Thông Tin Booking (Cho Người Dùng)

Cập nhật thông tin của một booking. Chỉ cho phép cập nhật khi booking ở trạng thái PENDING.

**Endpoint:** `PUT /api/v1/bookings/update/{id}`

**Yêu cầu bảo mật:**
- Yêu cầu user đã đăng nhập (`ROLE_USER`)

**Tham số đường dẫn:**
- `id` (Integer): ID của booking cần cập nhật

**Body Request:**
```json
{
  "userId": 2,
  "checkInDate": "2023-05-01",
  "checkOutDate": "2023-05-03",
  "totalPrice": 3500000,
  "discountId": 1,
  "status": "CONFIRMED"
}
```

**Phản hồi:**
- `200 OK`: Booking đã được cập nhật thành công
- `400 Bad Request`: Dữ liệu không hợp lệ
- `404 Not Found`: Booking không tồn tại

**Ví dụ phản hồi thành công:**
```json
{
  "id": 1,
  "userId": 2,
  "fullName": "Nguyễn Văn A",
  "nationalId": "025123456789",
  "email": "example@gmail.com",
  "phone": "0912345678",
  "rooms": [
    {
      "roomId": 1,
      "roomNumber": "101",
      "roomType": "Deluxe",
      "price": 1500000
    }
  ],
  "checkInDate": "2023-05-01",
  "checkOutDate": "2023-05-03",
  "totalPrice": 3500000,
  "finalPrice": 3150000,
  "discountCode": "SUMMER10",
  "discountValue": 0.1,
  "discountType": "PERCENT",
  "status": "CONFIRMED",
  "paymentMethod": "VnPay",
  "paymentStatus": "PAID",
  "paymentDate": "2023-04-29",
  "createdAt": "29-04-2023 15:30:45"
}
```

### 6. Hủy Booking (Cho Người Dùng)

Hủy một booking (chỉ có thể hủy khi trạng thái là PENDING hoặc CONFIRMED).

**Endpoint:** `POST /api/v1/bookings/cancel/{id}`

**Yêu cầu bảo mật:**
- Yêu cầu user đã đăng nhập (`ROLE_USER`)

**Tham số đường dẫn:**
- `id` (Integer): ID của booking cần hủy

**Phản hồi:**
- `200 OK`: Booking đã được hủy thành công
- `400 Bad Request`: Không thể hủy booking (không đúng trạng thái)
- `404 Not Found`: Booking không tồn tại

**Ví dụ phản hồi thành công:**
```json
{
  "id": 1,
  "userId": 2,
  "fullName": "Nguyễn Văn A",
  "nationalId": "025123456789",
  "email": "example@gmail.com",
  "phone": "0912345678",
  "rooms": [
    {
      "roomId": 1,
      "roomNumber": "101",
      "roomType": "Deluxe",
      "price": 1500000
    }
  ],
  "checkInDate": "2023-05-01",
  "checkOutDate": "2023-05-03",
  "totalPrice": 3000000,
  "finalPrice": 2700000,
  "discountCode": "SUMMER10",
  "discountValue": 0.1,
  "discountType": "PERCENT",
  "status": "CANCELLED",
  "paymentMethod": "VnPay",
  "paymentStatus": "REFUNDED",
  "paymentDate": "2023-04-29",
  "createdAt": "29-04-2023 15:30:45"
}
```

### 7. Xác Nhận Booking

Xác nhận một booking sau khi thanh toán qua VNPay (chỉ có thể xác nhận khi trạng thái là PENDING).

**Endpoint:** `POST /api/v1/bookings/confirm/{id}`

**Tham số đường dẫn:**
- `id` (Integer): ID của booking cần xác nhận

**Phản hồi:**
- `200 OK`: Booking đã được xác nhận thành công
- `400 Bad Request`: Không thể xác nhận booking (không đúng trạng thái)
- `404 Not Found`: Booking không tồn tại

**Ví dụ phản hồi thành công:**
```json
{
  "id": 1,
  "userId": 2,
  "fullName": "Nguyễn Văn A",
  "nationalId": "025123456789",
  "email": "example@gmail.com",
  "phone": "0912345678",
  "rooms": [
    {
      "roomId": 1,
      "roomNumber": "101",
      "roomType": "Deluxe",
      "price": 1500000
    }
  ],
  "checkInDate": "2023-05-01",
  "checkOutDate": "2023-05-03",
  "totalPrice": 3000000,
  "finalPrice": 2700000,
  "discountCode": "SUMMER10",
  "discountValue": 0.1,
  "discountType": "PERCENT",
  "status": "CONFIRMED",
  "paymentMethod": "VnPay",
  "paymentStatus": "PAID",
  "paymentDate": "2023-04-29",
  "createdAt": "29-04-2023 15:30:45"
}
```

## API Dành Riêng Cho Admin

### 1. Lấy Tất Cả Booking (Cho Admin)

Lấy danh sách tất cả các booking trong hệ thống.

**Endpoint:** `GET /api/v1/admin/bookings/`

**Tham số query:**
- `page` (Integer, mặc định: 0): Trang cần hiển thị
- `size` (Integer, mặc định: 10): Số lượng booking mỗi trang

**Yêu cầu bảo mật:**
- Yêu cầu quyền admin (`ROLE_ADMIN`)

**Phản hồi:**
- `200 OK`: Trả về danh sách tất cả booking

**Ví dụ phản hồi thành công:**
```json
[
  {
    "id": 1,
    "userId": 2,
    "fullName": "Nguyễn Văn A",
    "nationalId": "025123456789",
    "email": "example@gmail.com",
    "phone": "0912345678",
    "rooms": [
      {
        "roomId": 1,
        "roomNumber": "101",
        "roomType": "Deluxe",
        "price": 1500000
      }
    ],
    "checkInDate": "2023-05-01",
    "checkOutDate": "2023-05-03",
    "totalPrice": 3000000,
    "finalPrice": 2700000,
    "discountCode": "SUMMER10",
    "discountValue": 0.1,
    "discountType": "PERCENT",
    "status": "CONFIRMED",
    "paymentMethod": "VnPay",
    "paymentStatus": "PAID",
    "paymentDate": "2023-04-29",
    "createdAt": "29-04-2023 15:30:45"
  }
]
```

### 2. Lấy Booking Theo ID (Cho Admin)

Lấy chi tiết thông tin của một booking dựa trên ID.

**Endpoint:** `GET /api/v1/admin/bookings/{id}`

**Tham số đường dẫn:**
- `id` (Integer): ID của booking cần lấy thông tin

**Yêu cầu bảo mật:**
- Yêu cầu quyền admin (`ROLE_ADMIN`)

**Phản hồi:**
- `200 OK`: Trả về chi tiết của booking
- `404 Not Found`: Booking không tồn tại

**Ví dụ phản hồi thành công:**
```json
{
  "id": 1,
  "userId": 2,
  "fullName": "Nguyễn Văn A",
  "nationalId": "025123456789",
  "email": "example@gmail.com",
  "phone": "0912345678",
  "rooms": [
    {
      "roomId": 1,
      "roomNumber": "101",
      "roomType": "Deluxe",
      "price": 1500000
    }
  ],
  "checkInDate": "2023-05-01",
  "checkOutDate": "2023-05-03",
  "totalPrice": 3000000,
  "finalPrice": 2700000,
  "discountCode": "SUMMER10",
  "discountValue": 0.1,
  "discountType": "PERCENT",
  "status": "CONFIRMED",
  "paymentMethod": "VnPay",
  "paymentStatus": "PAID",
  "paymentDate": "2023-04-29",
  "createdAt": "29-04-2023 15:30:45"
}
```

### 3. Lấy Danh Sách Booking Mới Nhất (Cho Admin)

Lấy danh sách các booking mới nhất trong hệ thống.

**Endpoint:** `GET /api/v1/admin/bookings/recent`

**Yêu cầu bảo mật:**
- Yêu cầu quyền admin (`ROLE_ADMIN`)

**Phản hồi:**
- `200 OK`: Trả về danh sách booking mới nhất

**Ví dụ phản hồi thành công:**
```json
[
  {
    "id": 5,
    "userId": 3,
    "fullName": "Trần Thị B",
    "roomNumbers": ["201", "202"],
    "checkInDate": "2023-05-10",
    "checkOutDate": "2023-05-12",
    "totalPrice": 4000000,
    "status": "PENDING",
    "createdAt": "08-04-2025 15:30:45"
  },
  {
    "id": 4,
    "userId": 2,
    "fullName": "Nguyễn Văn A",
    "roomNumbers": ["101"],
    "checkInDate": "2023-05-05",
    "checkOutDate": "2023-05-07",
    "totalPrice": 3000000,
    "status": "CONFIRMED",
    "createdAt": "07-04-2025 10:15:30"
  }
]
```

### 4. Lấy Danh Sách Booking Theo Trạng Thái (Cho Admin)

Lấy danh sách các booking có cùng trạng thái.

**Endpoint:** `GET /api/v1/admin/bookings/status/{status}`

**Tham số đường dẫn:**
- `status` (String): Trạng thái của booking (PENDING, CONFIRMED, CANCELLED, CHECKED_IN, CHECKED_OUT, COMPLETED)

**Yêu cầu bảo mật:**
- Yêu cầu quyền admin (`ROLE_ADMIN`)

**Phản hồi:**
- `200 OK`: Trả về danh sách booking theo trạng thái

**Ví dụ phản hồi thành công:**
```json
[
  {
    "id": 1,
    "userId": 2,
    "fullName": "Nguyễn Văn A",
    "nationalId": "025123456789",
    "email": "example@gmail.com",
    "phone": "0912345678",
    "rooms": [
      {
        "roomId": 1,
        "roomNumber": "101",
        "roomType": "Deluxe",
        "price": 1500000
      }
    ],
    "checkInDate": "2023-05-01",
    "checkOutDate": "2023-05-03",
    "totalPrice": 3000000,
    "finalPrice": 2700000,
    "discountCode": "SUMMER10",
    "discountValue": 0.1,
    "discountType": "PERCENT",
    "status": "CONFIRMED",
    "paymentMethod": "VnPay",
    "paymentStatus": "PAID",
    "paymentDate": "2023-04-29",
    "createdAt": "29-04-2023 15:30:45"
  }
]
```

### 5. Lấy Danh Sách Booking Theo Khoảng Thời Gian (Cho Admin)

Lấy danh sách các booking trong một khoảng thời gian cụ thể.

**Endpoint:** `GET /api/v1/admin/bookings/date-range`

**Tham số query:**
- `startDate` (Date, định dạng ISO): Ngày bắt đầu khoảng thời gian (YYYY-MM-DD)
- `endDate` (Date, định dạng ISO): Ngày kết thúc khoảng thời gian (YYYY-MM-DD)

**Yêu cầu bảo mật:**
- Yêu cầu quyền admin (`ROLE_ADMIN`)

**Phản hồi:**
- `200 OK`: Trả về danh sách booking trong khoảng thời gian

**Ví dụ phản hồi thành công:**
```json
[
  {
    "id": 1,
    "userId": 2,
    "fullName": "Nguyễn Văn A",
    "nationalId": "025123456789",
    "email": "example@gmail.com",
    "phone": "0912345678",
    "rooms": [
      {
        "roomId": 1,
        "roomNumber": "101",
        "roomType": "Deluxe",
        "price": 1500000
      }
    ],
    "checkInDate": "2023-05-01",
    "checkOutDate": "2023-05-03",
    "totalPrice": 3000000,
    "finalPrice": 2700000,
    "discountCode": "SUMMER10",
    "discountValue": 0.1,
    "discountType": "PERCENT",
    "status": "CONFIRMED",
    "paymentMethod": "VnPay",
    "paymentStatus": "PAID",
    "paymentDate": "2023-04-29",
    "createdAt": "29-04-2023 15:30:45"
  }
]
```

### 6. Tạo Mới Booking (Dành Riêng Cho Admin)

Tạo một booking mới trực tiếp bởi admin.

**Endpoint:** `POST /api/v1/admin/bookings/create`

**Yêu cầu bảo mật:**
- Yêu cầu quyền admin (`ROLE_ADMIN`)

**Body Request:**
```json
{
  "userId": 2,
  "fullName": "Nguyễn Văn A",
  "nationalId": "025123456789",
  "email": "example@gmail.com",
  "phone": "0912345678",
  "roomIds": [1, 2],
  "checkInDate": "2023-05-01",
  "checkOutDate": "2023-05-03",
  "totalPrice": 3000000,
  "discountCode": "SUMMER10",
  "status": "CONFIRMED",
  "paymentMethod": "CASH",
  "paymentStatus": "PAID"
}
```

**Phản hồi:**
- `201 Created`: Booking đã được tạo thành công
- `400 Bad Request`: Dữ liệu không hợp lệ
- `404 Not Found`: Không tìm thấy phòng hoặc mã giảm giá

**Ví dụ phản hồi thành công:**
```json
{
  "id": 1,
  "userId": 2,
  "fullName": "Nguyễn Văn A",
  "nationalId": "025123456789",
  "email": "example@gmail.com",
  "phone": "0912345678",
  "rooms": [
    {
      "roomId": 1,
      "roomNumber": "101",
      "roomType": "Deluxe",
      "price": 1500000
    },
    {
      "roomId": 2,
      "roomNumber": "102",
      "roomType": "Deluxe",
      "price": 1500000
    }
  ],
  "checkInDate": "2023-05-01",
  "checkOutDate": "2023-05-03",
  "totalPrice": 3000000,
  "finalPrice": 2700000,
  "discountCode": "SUMMER10",
  "discountValue": 0.1,
  "discountType": "PERCENT",
  "status": "CONFIRMED",
  "paymentMethod": "CASH",
  "paymentStatus": "PAID",
  "paymentDate": "08-04-2025",
  "createdAt": "08-04-2025 15:30:45"
}
```

### 7. Cập Nhật Booking (Dành Riêng Cho Admin)

Cập nhật thông tin của một booking bởi admin.

**Endpoint:** `PUT /api/v1/admin/bookings/update/{id}`

**Yêu cầu bảo mật:**
- Yêu cầu quyền admin (`ROLE_ADMIN`)

**Tham số đường dẫn:**
- `id` (Integer): ID của booking cần cập nhật

**Body Request:**
```json
{
  "userId": 2,
  "fullName": "Nguyễn Văn A",
  "nationalId": "025123456789",
  "email": "example@gmail.com",
  "phone": "0912345678",
  "roomIds": [1],
  "checkInDate": "2023-05-01",
  "checkOutDate": "2023-05-04",
  "totalPrice": 4500000,
  "discountCode": "SUMMER10",
  "status": "CONFIRMED",
  "paymentMethod": "CASH",
  "paymentStatus": "PAID"
}
```

**Phản hồi:**
- `200 OK`: Booking đã được cập nhật thành công
- `400 Bad Request`: Dữ liệu không hợp lệ
- `404 Not Found`: Booking không tồn tại

**Ví dụ phản hồi thành công:**
```json
{
  "id": 1,
  "userId": 2,
  "fullName": "Nguyễn Văn A",
  "nationalId": "025123456789",
  "email": "example@gmail.com",
  "phone": "0912345678",
  "rooms": [
    {
      "roomId": 1,
      "roomNumber": "101",
      "roomType": "Deluxe",
      "price": 1500000
    }
  ],
  "checkInDate": "2023-05-01",
  "checkOutDate": "2023-05-04",
  "totalPrice": 4500000,
  "finalPrice": 4050000,
  "discountCode": "SUMMER10",
  "discountValue": 0.1,
  "discountType": "PERCENT",
  "status": "CONFIRMED",
  "paymentMethod": "CASH",
  "paymentStatus": "PAID",
  "paymentDate": "08-04-2025",
  "createdAt": "29-04-2023 15:30:45",
  "updatedAt": "08-04-2025 16:45:30"
}
```

### 8. Hủy Booking (Dành Riêng Cho Admin)

Hủy một booking bởi admin.

**Endpoint:** `POST /api/v1/admin/bookings/cancel/{id}`

**Yêu cầu bảo mật:**
- Yêu cầu quyền admin (`ROLE_ADMIN`)

**Tham số đường dẫn:**
- `id` (Integer): ID của booking cần hủy

**Phản hồi:**
- `200 OK`: Booking đã được hủy thành công
- `404 Not Found`: Booking không tồn tại

**Ví dụ phản hồi thành công:**
```json
{
  "id": 1,
  "userId": 2,
  "fullName": "Nguyễn Văn A",
  "nationalId": "025123456789",
  "email": "example@gmail.com",
  "phone": "0912345678",
  "rooms": [
    {
      "roomId": 1,
      "roomNumber": "101",
      "roomType": "Deluxe",
      "price": 1500000
    }
  ],
  "checkInDate": "2023-05-01",
  "checkOutDate": "2023-05-03",
  "totalPrice": 3000000,
  "finalPrice": 2700000,
  "discountCode": "SUMMER10",
  "discountValue": 0.1,
  "discountType": "PERCENT",
  "status": "CANCELLED",
  "paymentMethod": "VnPay",
  "paymentStatus": "REFUNDED",
  "paymentDate": "2023-04-29",
  "createdAt": "29-04-2023 15:30:45",
  "updatedAt": "08-04-2025 17:15:20"
}
```

### 9. Xác Nhận Booking (Dành Riêng Cho Admin)

Xác nhận một booking bởi admin.

**Endpoint:** `POST /api/v1/admin/bookings/confirm/{id}`

**Yêu cầu bảo mật:**
- Yêu cầu quyền admin (`ROLE_ADMIN`)

**Tham số đường dẫn:**
- `id` (Integer): ID của booking cần xác nhận

**Phản hồi:**
- `200 OK`: Booking đã được xác nhận thành công
- `404 Not Found`: Booking không tồn tại

**Ví dụ phản hồi thành công:**
```json
{
  "id": 1,
  "userId": 2,
  "fullName": "Nguyễn Văn A",
  "nationalId": "025123456789",
  "email": "example@gmail.com",
  "phone": "0912345678",
  "rooms": [
    {
      "roomId": 1,
      "roomNumber": "101",
      "roomType": "Deluxe",
      "price": 1500000
    }
  ],
  "checkInDate": "2023-05-01",
  "checkOutDate": "2023-05-03",
  "totalPrice": 3000000,
  "finalPrice": 2700000,
  "discountCode": "SUMMER10",
  "discountValue": 0.1,
  "discountType": "PERCENT",
  "status": "CONFIRMED",
  "paymentMethod": "VnPay",
  "paymentStatus": "PAID",
  "paymentDate": "2023-04-29",
  "createdAt": "29-04-2023 15:30:45",
  "updatedAt": "08-04-2025 17:30:10"
}
```

### 10. Check-in Booking (Dành Riêng Cho Admin)

Check-in một booking bởi admin.

**Endpoint:** `POST /api/v1/admin/bookings/check-in/{id}`

**Yêu cầu bảo mật:**
- Yêu cầu quyền admin (`ROLE_ADMIN`)

**Tham số đường dẫn:**
- `id` (Integer): ID của booking cần check-in

**Phản hồi:**
- `200 OK`: Booking đã được check-in thành công
- `404 Not Found`: Booking không tồn tại

**Ví dụ phản hồi thành công:**
```json
{
  "id": 1,
  "userId": 2,
  "fullName": "Nguyễn Văn A",
  "nationalId": "025123456789",
  "email": "example@gmail.com",
  "phone": "0912345678",
  "rooms": [
    {
      "roomId": 1,
      "roomNumber": "101",
      "roomType": "Deluxe",
      "price": 1500000
    }
  ],
  "checkInDate": "2023-05-01",
  "checkOutDate": "2023-05-03",
  "totalPrice": 3000000,
  "finalPrice": 2700000,
  "discountCode": "SUMMER10",
  "discountValue": 0.1,
  "discountType": "PERCENT",
  "status": "CHECKED_IN",
  "paymentMethod": "VnPay",
  "paymentStatus": "PAID",
  "paymentDate": "2023-04-29",
  "createdAt": "29-04-2023 15:30:45",
  "updatedAt": "08-04-2025 18:00:00"
}
```

### 11. Check-out Booking (Dành Riêng Cho Admin)

Check-out một booking bởi admin.

**Endpoint:** `POST /api/v1/admin/bookings/check-out/{id}`

**Yêu cầu bảo mật:**
- Yêu cầu quyền admin (`ROLE_ADMIN`)

**Tham số đường dẫn:**
- `id` (Integer): ID của booking cần check-out

**Phản hồi:**
- `200 OK`: Booking đã được check-out thành công
- `404 Not Found`: Booking không tồn tại

**Ví dụ phản hồi thành công:**
```json
{
  "id": 1,
  "userId": 2,
  "fullName": "Nguyễn Văn A",
  "nationalId": "025123456789",
  "email": "example@gmail.com",
  "phone": "0912345678",
  "rooms": [
    {
      "roomId": 1,
      "roomNumber": "101",
      "roomType": "Deluxe",
      "price": 1500000
    }
  ],
  "checkInDate": "2023-05-01",
  "checkOutDate": "2023-05-03",
  "totalPrice": 3000000,
  "finalPrice": 2700000,
  "discountCode": "SUMMER10",
  "discountValue": 0.1,
  "discountType": "PERCENT",
  "status": "CHECKED_OUT",
  "paymentMethod": "VnPay",
  "paymentStatus": "PAID",
  "paymentDate": "2023-04-29",
  "createdAt": "29-04-2023 15:30:45",
  "updatedAt": "08-04-2025 12:00:00"
}
```

### 12. Xóa Booking (Dành Riêng Cho Admin)

Xóa một booking bởi admin.

**Endpoint:** `DELETE /api/v1/admin/bookings/{id}`

**Yêu cầu bảo mật:**
- Yêu cầu quyền admin (`ROLE_ADMIN`)

**Tham số đường dẫn:**
- `id` (Integer): ID của booking cần xóa

**Phản hồi:**
- `200 OK`: Booking đã được xóa thành công
- `404 Not Found`: Booking không tồn tại

**Ví dụ phản hồi thành công:**
```json
{
  "message": "Booking đã được xóa thành công",
  "bookingId": 1
}
```

## Các Trạng Thái Booking

Hệ thống sử dụng các trạng thái sau cho booking:

1. `PENDING`: Đang chờ xử lý (mới tạo booking, chưa thanh toán)
2. `CONFIRMED`: Đã xác nhận (đã thanh toán, chưa check-in)
3. `CHECKED_IN`: Đã check-in (khách đã đến nhận phòng)
4. `CHECKED_OUT`: Đã check-out (khách đã trả phòng)
5. `CANCELLED`: Đã hủy (booking bị hủy)
6. `COMPLETED`: Đã hoàn thành (sau khi check-out và thanh toán đầy đủ)

## Lưu ý

- **Đặt phòng cho người dùng thông thường**:
  - Tạo booking với trạng thái PENDING
  - Yêu cầu thanh toán qua VnPay
  - Sau khi thanh toán thành công, booking được cập nhật sang trạng thái CONFIRMED

- **Đặt phòng cho admin**:
  - Tạo booking với trạng thái mặc định là CONFIRMED
  - Không bắt buộc phải thanh toán qua VnPay (có thể thanh toán bằng CASH, CARD, TRANSFER)
  - Admin có thể tạo booking cho khách hàng không có tài khoản trong hệ thống
  - Admin có thêm quyền check-in và check-out booking