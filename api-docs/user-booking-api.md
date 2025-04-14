# API Đặt Phòng Cho Người Dùng (User Booking API)

API đặt phòng dành riêng cho người dùng thông thường với quy trình thanh toán qua VNPay.

## Quy Trình Đặt Phòng & Thanh Toán

1. Người dùng tạo đơn đặt phòng
2. Hệ thống tạo đơn đặt phòng với trạng thái `PENDING`
3. Hệ thống tạo liên kết thanh toán VNPay
4. Người dùng được chuyển hướng tới trang thanh toán VNPay
5. Sau khi thanh toán, VNPay chuyển hướng về callback URL
6. Hệ thống cập nhật trạng thái đơn đặt thành `CONFIRMED` nếu thanh toán thành công

## 1. Lấy Thông Tin Booking Theo ID

Lấy chi tiết thông tin của một booking dựa trên ID.

**Endpoint:** `GET /api/v1/user/bookings/{id}`

**Yêu cầu bảo mật:**
- Yêu cầu user đã đăng nhập (`ROLE_USER`)
- User chỉ có thể xem thông tin booking của chính họ

**Tham số đường dẫn:**
- `id` (Integer): ID của booking cần lấy thông tin

**Phản hồi:**
- `200 OK`: Trả về chi tiết của booking
- `403 Forbidden`: Không có quyền xem booking này
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
      "id": 1,
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

## 2. Lấy Danh Sách Booking Của Người Dùng Hiện Tại

Lấy danh sách tất cả các booking của người dùng đã đăng nhập.

**Endpoint:** `GET /api/v1/user/bookings/my-bookings`

**Yêu cầu bảo mật:**
- Yêu cầu user đã đăng nhập (`ROLE_USER`)

**Phản hồi:**
- `200 OK`: Trả về danh sách booking

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
        "id": 1,
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

## 3. Lấy Danh Sách Phòng Đã Đặt Trong Khoảng Thời Gian

Lấy danh sách các phòng đã được đặt trong một khoảng thời gian cụ thể.

**Endpoint:** `GET /api/v1/user/bookings/booked-rooms`

**Tham số query:**
- `startDate` (Date, định dạng ISO): Ngày bắt đầu khoảng thời gian (YYYY-MM-DD)
- `endDate` (Date, định dạng ISO): Ngày kết thúc khoảng thời gian (YYYY-MM-DD)

**Phản hồi:**
- `200 OK`: Trả về danh sách phòng đã đặt trong khoảng thời gian

**Ví dụ phản hồi thành công:**
```json
[
  {
    "id": 1,
    "roomNumber": "101",
    "roomType": "Deluxe",
    "price": 1500000
  },
  {
    "id": 2,
    "roomNumber": "102",
    "roomType": "Deluxe",
    "price": 1500000
  }
]
```

## 4. Tạo Mới Booking Và Thanh Toán

Tạo một booking mới và tạo liên kết thanh toán VNPay.

**Endpoint:** `POST /api/v1/user/bookings/create`

**Yêu cầu bảo mật:**
- Yêu cầu user đã đăng nhập (`ROLE_USER`)

**Body Request:**
```json
{
  "roomIds": [1, 2],
  "checkInDate": "2023-05-01",
  "checkOutDate": "2023-05-03",
  "totalPrice": 3000000,
  "discountId": 1,
  "adults": 2,
  "children": 1,
  "specialRequests": "Phòng tầng cao, view đẹp"
}
```

**Lưu ý:**
- `userId` được lấy tự động từ thông tin đăng nhập, không cần gửi trong request
- `status` mặc định được đặt là "PENDING"

**Phản hồi:**
- `201 Created`: Booking đã được tạo thành công
- `400 Bad Request`: Dữ liệu không hợp lệ
- `404 Not Found`: Không tìm thấy phòng hoặc mã giảm giá

**Ví dụ phản hồi thành công:**
```json
{
  "booking": {
    "id": 1,
    "userId": 2,
    "fullName": "Nguyễn Văn A",
    "nationalId": "025123456789",
    "email": "example@gmail.com",
    "phone": "0912345678",
    "rooms": [
      {
        "id": 1,
        "roomNumber": "101",
        "roomType": "Deluxe",
        "price": 1500000
      },
      {
        "id": 2,
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
  },
  "payment": {
    "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=270000000&vnp_Command=pay&vnp_CreateDate=20230429153045&vnp_CurrCode=VND&vnp_IpAddr=127.0.0.1&vnp_Locale=vn&vnp_OrderInfo=Thanh+toan+dat+phong+khach+san+-+Ma+dat+phong%3A+1&vnp_OrderType=other&vnp_ReturnUrl=http%3A%2F%2Flocalhost%3A8080%2Fapi%2Fv1%2Fpayments%2Fcallback&vnp_TmnCode=DEMO&vnp_TxnRef=12345678&vnp_Version=2.1.0&vnp_SecureHash=343fb9fcff67d5f00970bc7ab4f16cc11aeb43b5a8b8e0f9968372edbee8a1c293ad2f33ef9c6ff98e7b1dd7688d2496ac52a77e4a3cbf4ef65f72a4faf486f0",
    "orderInfo": "Thanh toan dat phong khach san - Ma dat phong: 1"
  }
}
```

## 5. Cập Nhật Thông Tin Booking

Cập nhật thông tin của một booking. Chỉ cho phép cập nhật khi booking ở trạng thái PENDING.

**Endpoint:** `PUT /api/v1/user/bookings/update/{id}`

**Yêu cầu bảo mật:**
- Yêu cầu user đã đăng nhập (`ROLE_USER`)
- User chỉ có thể cập nhật booking của chính họ

**Tham số đường dẫn:**
- `id` (Integer): ID của booking cần cập nhật

**Body Request:**
```json
{
  "roomIds": [1, 3],
  "checkInDate": "2023-05-02",
  "checkOutDate": "2023-05-04",
  "totalPrice": 3500000,
  "discountId": 1,
  "adults": 2,
  "children": 0,
  "specialRequests": "Phòng yên tĩnh"
}
```

**Phản hồi:**
- `200 OK`: Booking đã được cập nhật thành công
- `400 Bad Request`: Không thể cập nhật (không đúng trạng thái)
- `403 Forbidden`: Không có quyền cập nhật booking này
- `404 Not Found`: Booking không tồn tại

**Ví dụ phản hồi thành công (Nếu giá không thay đổi):**
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
      "id": 1,
      "roomNumber": "101",
      "roomType": "Deluxe",
      "price": 1500000
    },
    {
      "id": 3,
      "roomNumber": "201",
      "roomType": "Premium",
      "price": 2000000
    }
  ],
  "checkInDate": "2023-05-02",
  "checkOutDate": "2023-05-04",
  "totalPrice": 3500000,
  "finalPrice": 3150000,
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

**Ví dụ phản hồi thành công (Nếu giá thay đổi - tạo liên kết thanh toán mới):**
```json
{
  "booking": {
    "id": 1,
    "userId": 2,
    "fullName": "Nguyễn Văn A",
    "nationalId": "025123456789",
    "email": "example@gmail.com",
    "phone": "0912345678",
    "rooms": [
      {
        "id": 1,
        "roomNumber": "101",
        "roomType": "Deluxe",
        "price": 1500000
      },
      {
        "id": 3,
        "roomNumber": "201",
        "roomType": "Premium",
        "price": 2000000
      }
    ],
    "checkInDate": "2023-05-02",
    "checkOutDate": "2023-05-04",
    "totalPrice": 3500000,
    "finalPrice": 3150000,
    "discountCode": "SUMMER10",
    "discountValue": 0.1,
    "discountType": "PERCENT",
    "status": "PENDING",
    "paymentMethod": null,
    "paymentStatus": "UNPAID",
    "paymentDate": null,
    "createdAt": "29-04-2023 15:30:45"
  },
  "payment": {
    "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=315000000&vnp_Command=pay&vnp_CreateDate=20230429153045&vnp_CurrCode=VND&vnp_IpAddr=127.0.0.1&vnp_Locale=vn&vnp_OrderInfo=Thanh+toan+dat+phong+khach+san+-+Ma+dat+phong%3A+1&vnp_OrderType=other&vnp_ReturnUrl=http%3A%2F%2Flocalhost%3A8080%2Fapi%2Fv1%2Fpayments%2Fcallback&vnp_TmnCode=DEMO&vnp_TxnRef=87654321&vnp_Version=2.1.0&vnp_SecureHash=343fb9fcff67d5f00970bc7ab4f16cc11aeb43b5a8b8e0f9968372edbee8a1c293ad2f33ef9c6ff98e7b1dd7688d2496ac52a77e4a3cbf4ef65f72a4faf486f0",
    "orderInfo": "Thanh toan dat phong khach san - Ma dat phong: 1"
  }
}
```

## 6. Hủy Booking

Hủy một booking (chỉ có thể hủy khi trạng thái là PENDING hoặc CONFIRMED).

**Endpoint:** `POST /api/v1/user/bookings/cancel/{id}`

**Yêu cầu bảo mật:**
- Yêu cầu user đã đăng nhập (`ROLE_USER`)
- User chỉ có thể hủy booking của chính họ

**Tham số đường dẫn:**
- `id` (Integer): ID của booking cần hủy

**Phản hồi:**
- `200 OK`: Booking đã được hủy thành công
- `400 Bad Request`: Không thể hủy booking (không đúng trạng thái)
- `403 Forbidden`: Không có quyền hủy booking này
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
      "id": 1,
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

## 7. Kiểm Tra Trạng Thái Thanh Toán

Kiểm tra trạng thái thanh toán của một booking.

**Endpoint:** `GET /api/v1/user/bookings/payment-status/{id}`

**Yêu cầu bảo mật:**
- Yêu cầu user đã đăng nhập (`ROLE_USER`)
- User chỉ có thể xem trạng thái thanh toán của booking của chính họ

**Tham số đường dẫn:**
- `id` (Integer): ID của booking cần kiểm tra trạng thái

**Phản hồi:**
- `200 OK`: Trả về thông tin trạng thái thanh toán
- `403 Forbidden`: Không có quyền xem trạng thái thanh toán của booking này
- `404 Not Found`: Booking không tồn tại

**Ví dụ phản hồi thành công:**
```json
{
  "booking": {
    "id": 1,
    "userId": 2,
    "fullName": "Nguyễn Văn A",
    "nationalId": "025123456789",
    "email": "example@gmail.com",
    "phone": "0912345678",
    "rooms": [
      {
        "id": 1,
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
  },
  "paymentStatus": "PAID",
  "paymentMethod": "VnPay",
  "paymentDate": "2023-04-29"
}
```

## Các Trạng Thái Booking & Thanh Toán

### Trạng Thái Booking:

1. `PENDING`: Đang chờ xử lý (mới tạo booking, chưa thanh toán)
2. `CONFIRMED`: Đã xác nhận (đã thanh toán, chưa check-in)
3. `CHECKED_IN`: Đã check-in (khách đã đến nhận phòng)
4. `COMPLETED`: Đã check-out (khách đã trả phòng)
5. `CANCELLED`: Đã hủy (booking bị hủy)

### Trạng Thái Thanh Toán:

1. `UNPAID`: Chưa thanh toán
2. `PAID`: Đã thanh toán
3. `PROCESSING`: Đang xử lý thanh toán
4. `REFUNDED`: Đã hoàn tiền
5. `FAILED`: Thanh toán thất bại

## Quy Trình Tích Hợp VNPay

1. **Tạo Đơn Đặt Phòng:**
   - Gọi API `POST /api/v1/user/bookings/create`
   - Nhận URL thanh toán từ response

2. **Chuyển Hướng Người Dùng:**
   - Mở URL thanh toán từ VNPay trong trình duyệt
   - Người dùng thực hiện thanh toán trên cổng VNPay

3. **Nhận Kết Quả Thanh Toán:**
   - VNPay gọi callback URL của hệ thống sau khi thanh toán
   - Hệ thống cập nhật trạng thái booking dựa trên kết quả thanh toán

4. **Kiểm Tra Trạng Thái:**
   - Sử dụng API `GET /api/v1/user/bookings/payment-status/{id}`
   - Hiển thị kết quả thanh toán cho người dùng 

## 6. Lấy Thông Tin Chi Tiết Đơn Đặt Phòng Kèm Thanh Toán

Lấy thông tin chi tiết đơn đặt phòng kèm thông tin thanh toán đầy đủ.

**Endpoint:** `GET /api/v1/user/bookings/detail/{id}`

**Tham số đường dẫn:**
- `id` (Integer): ID của đơn đặt phòng

**Phản hồi:**
- `200 OK`: Trả về thông tin chi tiết đơn đặt phòng và thanh toán
- `404 Not Found`: Không tìm thấy đơn đặt phòng

**Ví dụ phản hồi thành công:**
```json
{
    "booking": {
        "id": 152,
        "userId": 40,
        "fullName": "Phạm Thùy Linh",
        "nationalId": "123445435345",
        "email": "linh@example.com",
        "phone": "0773352286",
        "rooms": [
            {
                "roomId": 13,
                "roomNumber": "104A",
                "roomType": "Phòng Đơn Tiêu Chuẩn",
                "imagePath": "/images/rooms/standard-single.jpg",
                "price": 500000.0
            },
            {
                "roomId": 12,
                "roomNumber": "103A",
                "roomType": "Phòng Đơn Tiêu Chuẩn",
                "imagePath": "/images/rooms/standard-single.jpg",
                "price": 500000.0
            }
        ],
        "checkInDate": "2026-05-03",
        "checkOutDate": "2026-05-08",
        "totalPrice": 5000000.0,
        "finalPrice": 4900000.0,
        "discountCode": "DEMO29",
        "discountValue": 100000.0,
        "discountType": "FIXED",
        "status": "PENDING",
        "paymentMethod": "VNPAY",
        "paymentStatus": "UNPAID",
        "paymentDate": "",
        "createdAt": "07-04-2025 23:25:27"
    },
    "payment": {
        "bankCode": "",
        "amount": 5000000,
        "totalPrice": 5000000.0,
        "pending": false,
        "transactionNo": "",
        "bookingId": 152,
        "paymentId": 207,
        "success": false,
        "paymentMethod": "VNPAY",
        "paymentDate": "",
        "formattedAmount": "5.000.000 ₫",
        "paymentStatus": "UNPAID",
        "status": "PENDING"
    }
}
```

## 7. Lấy Danh Sách Đơn Đặt Phòng Đã Xác Nhận

Lấy danh sách tất cả các đơn đặt phòng đã xác nhận trong khoảng thời gian.

**Endpoint:** `GET /api/v1/user/bookings/list-confirmed`

**Tham số query:**
- `startDate` (Date, optional): Ngày bắt đầu (mặc định là ngày hiện tại)
- `endDate` (Date, optional): Ngày kết thúc (mặc định là 30 ngày sau startDate)

**Phản hồi:**
- `200 OK`: Trả về danh sách đơn đặt phòng đã xác nhận

**Ví dụ phản hồi thành công:**
```json
{
    "bookings": [
        {
            "id": 1,
            "userId": 1,
            "fullName": "Nguyễn Văn A",
            "nationalId": "123456789",
            "email": "nguyenvana@example.com",
            "phone": "0123456789",
            "rooms": [
                {
                    "roomId": 1,
                    "roomNumber": "101",
                    "roomType": "Standard",
                    "imagePath": "/images/rooms/standard.jpg",
                    "price": 1000000
                }
            ],
            "checkInDate": "2024-04-01",
            "checkOutDate": "2024-04-03",
            "totalPrice": 2000000,
            "finalPrice": 2000000,
            "discountCode": "",
            "discountValue": 0,
            "discountType": "",
            "status": "CONFIRMED",
            "paymentMethod": "VNPAY",
            "paymentStatus": "00",
            "paymentDate": "20240401221053",
            "createdAt": "01-04-2024 22:10:53"
        }
    ],
    "startDate": "2024-04-01",
    "endDate": "2024-04-30",
    "count": 1
}
``` 