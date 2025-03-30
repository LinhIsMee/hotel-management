# API Quản Lý Đặt Phòng (Booking Management)

## 1. Lấy Thông Tin Booking Theo ID

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

## 2. Lấy Danh Sách Booking Theo UserId

Lấy danh sách tất cả các booking của một người dùng.

**Endpoint:** `GET /api/v1/bookings/user/{userId}`

**Tham số đường dẫn:**
- `userId` (Integer): ID của người dùng

**Phản hồi:**
- `200 OK`: Trả về danh sách booking
- `404 Not Found`: Người dùng không tồn tại

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

## 3. Lấy Tất Cả Booking

Lấy danh sách tất cả các booking trong hệ thống.

**Endpoint:** `GET /api/v1/bookings/`

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

## 4. Lấy Danh Sách Booking Mới Nhất Trong 7 Ngày

Lấy danh sách các booking được tạo mới nhất trong 7 ngày gần đây.

**Endpoint:** `GET /api/v1/bookings/recent`

**Phản hồi:**
- `200 OK`: Trả về danh sách booking mới nhất

**Ví dụ phản hồi thành công:**
```json
[
  {
    "bookingId": 1,
    "userId": 2,
    "fullName": "Nguyễn Văn A",
    "roomCount": 1
  }
]
```

## 5. Lấy Danh Sách Booking Theo Trạng Thái

Lấy danh sách các booking có cùng một trạng thái.

**Endpoint:** `GET /api/v1/bookings/status/{status}`

**Tham số đường dẫn:**
- `status` (String): Trạng thái của booking (PENDING, CONFIRMED, CANCELLED, CHECKED_IN, CHECKED_OUT)

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

## 6. Lấy Danh Sách Booking Trong Khoảng Thời Gian

Lấy danh sách các booking trong một khoảng thời gian cụ thể.

**Endpoint:** `GET /api/v1/bookings/date-range`

**Tham số query:**
- `startDate` (Date, định dạng ISO): Ngày bắt đầu khoảng thời gian (YYYY-MM-DD)
- `endDate` (Date, định dạng ISO): Ngày kết thúc khoảng thời gian (YYYY-MM-DD)

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

## 7. Lấy Danh Sách Phòng Đã Đặt Trong Khoảng Thời Gian

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

## 8. Tạo Mới Booking

Tạo một booking mới.

**Endpoint:** `POST /api/v1/bookings/create`

**Body Request:**
```json
{
  "userId": 2,
  "checkInDate": "2023-05-01",
  "checkOutDate": "2023-05-03",
  "totalPrice": 3000000,
  "discountId": 1,
  "status": "PENDING"
}
```

**Phản hồi:**
- `201 Created`: Booking đã được tạo thành công
- `400 Bad Request`: Dữ liệu không hợp lệ

**Ví dụ phản hồi thành công:**
```json
{
  "id": 1,
  "userId": 2,
  "fullName": "Nguyễn Văn A",
  "nationalId": "025123456789",
  "email": "example@gmail.com",
  "phone": "0912345678",
  "rooms": [],
  "checkInDate": "2023-05-01",
  "checkOutDate": "2023-05-03",
  "totalPrice": 3000000,
  "finalPrice": 2700000,
  "discountCode": "SUMMER10",
  "discountValue": 0.1,
  "discountType": "PERCENT",
  "status": "PENDING",
  "paymentMethod": "VnPay",
  "paymentStatus": "UNPAID",
  "paymentDate": null,
  "createdAt": "29-04-2023 15:30:45"
}
```

## 9. Cập Nhật Thông Tin Booking

Cập nhật thông tin của một booking.

**Endpoint:** `PUT /api/v1/bookings/update/{id}`

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

## 10. Hủy Booking

Hủy một booking (chỉ có thể hủy khi trạng thái là PENDING hoặc CONFIRMED).

**Endpoint:** `POST /api/v1/bookings/cancel/{id}`

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

## 11. Xác Nhận Booking

Xác nhận một booking sau khi thanh toán (chỉ có thể xác nhận khi trạng thái là PENDING).

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

## Các Trạng Thái Booking

Hệ thống sử dụng các trạng thái sau cho booking:

1. `PENDING`: Đang chờ xử lý (mới tạo booking, chưa thanh toán)
2. `CONFIRMED`: Đã xác nhận (đã thanh toán, chưa check-in)
3. `CHECKED_IN`: Đã check-in (khách đã đến nhận phòng)
4. `CHECKED_OUT`: Đã check-out (khách đã trả phòng)
5. `CANCELLED`: Đã hủy (booking bị hủy)

## Lưu ý

- Hệ thống tự động cập nhật trạng thái booking mỗi ngày vào lúc 00:00:
  - Các booking có `checkInDate` là ngày hiện tại và trạng thái là `PENDING` hoặc `CONFIRMED` sẽ được cập nhật thành `CHECKED_IN`
  - Các booking có `checkOutDate` là ngày hiện tại và trạng thái là `CHECKED_IN` sẽ được cập nhật thành `CHECKED_OUT` 