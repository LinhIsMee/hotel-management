# API Thanh Toán

## Thông Tin Về Booking và BookingId

Hệ thống thanh toán có liên kết với hệ thống đặt phòng. Mỗi thanh toán có thể được kết nối với một booking cụ thể thông qua:

- **Booking**: Đối tượng đặt phòng đầy đủ (quan hệ nhiều-một)
- **BookingId**: ID của đặt phòng, được lưu trữ trực tiếp trong bảng Payment cho truy vấn hiệu quả

Khi tạo thanh toán, `bookingId` là tùy chọn nhưng nên được cung cấp để theo dõi mục đích thanh toán. Tất cả các API sẽ trả về `bookingId` trong kết quả nếu đã được liên kết với thanh toán.

## API Chính

### 1. Tạo Thanh Toán Và Đặt Phòng Kết Hợp (Đề Xuất)

**Endpoint**: `POST /api/v1/payments/create-booking-payment`

**Mô tả**: API này tự động kiểm tra phòng trống, tạo đặt phòng và thanh toán trong một bước. Hệ thống sẽ kiểm tra xem phòng có khả dụng trong khoảng thời gian yêu cầu không, và chỉ tạo đặt phòng và thanh toán nếu tất cả phòng đều khả dụng.

**Request Body**:
```json
{
  "userId": 1,
  "roomIds": [101, 102],
  "checkInDate": "2025-05-01",
  "checkOutDate": "2025-05-03",
  "discountCode": "SUMMER2025",
  "ipAddress": "127.0.0.1",
  "returnUrl": "http://example.com/payment-callback"
}
```

**Response Body (Thành công)**:
```json
{
  "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?...",
  "transactionNo": "12345678",
  "amount": 2700000,
  "orderInfo": "Thanh toán đặt phòng #124",
  "bookingId": 124
}
```

**Response Body (Lỗi khi phòng đã được đặt)**:
```json
{
  "message": "Phòng đã được đặt trong khoảng thời gian này",
  "conflictRoomId": 102,
  "conflictBookingId": 123
}
```

**Response Body (Lỗi mã giảm giá)**:
```json
{
  "message": "Mã giảm giá không hợp lệ"
}
```

### 2. Tạo Thanh Toán (API Đơn Giản)

**Endpoint**: `POST /api/v1/payments/create`

**Mô tả**: API này tạo một giao dịch thanh toán mới và trả về URL thanh toán VNPay. API này KHÔNG tạo đặt phòng, chỉ tạo thanh toán. Nếu bạn muốn tạo đặt phòng và thanh toán cùng lúc, hãy sử dụng API `/create-booking-payment` ở trên.

**Request Body**:MVN 
```json
{
  "userId": 1,
  "orderInfo": "Mô tả giao dịch",
  "amount": 1000000,
  "ipAddress": "127.0.0.1",
  "returnUrl": "http://example.com/payment-callback",
  "bookingId": 123
}
```

**Response Body**:
```json
{
  "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?...",
  "transactionNo": "12345678",
  "amount": 1000000,
  "orderInfo": "Mô tả giao dịch",
  "bookingId": 123
}
```

**Response Body (Lỗi thiếu thông tin)**:
```json
{
  "success": false,
  "message": "Thiếu thông tin userId"
}
```

hoặc

```json
{
  "success": false,
  "message": "Thiếu thông tin orderInfo"
}
```

hoặc

```json
{
  "success": false,
  "message": "Thiếu thông tin amount"
}
```

hoặc

```json
{
  "success": false,
  "message": "Thiếu thông tin ipAddress"
}
```

hoặc

```json
{
  "success": false,
  "message": "Thiếu thông tin returnUrl"
}
```

### 3. Kiểm Tra Trạng Thái Thanh Toán

**Endpoint**: `GET /api/v1/payments/check-status/{transactionNo}`

**Mô tả**: API này kiểm tra trạng thái thanh toán của một giao dịch.

**Response Body (Đang chờ thanh toán)**:
```json
{
  "success": false,
  "message": "Chờ thanh toán",
  "pending": true,
  "transactionNo": "12345678",
  "amount": 1000000,
  "bookingId": 123
}
```

**Response Body (Thanh toán thành công)**:
```json
{
  "success": true,
  "message": "Thanh toán thành công",
  "transactionNo": "12345678",
  "amount": 1000000,
  "bookingId": 123
}
```

**Response Body (Thanh toán thất bại)**:
```json
{
  "success": false,
  "message": "Thanh toán thất bại",
  "transactionNo": "12345678",
  "amount": 1000000,
  "bookingId": 123
}
```

### 4. Callback từ VNPay

**Endpoint**: `GET /api/v1/payments/callback`

**Mô tả**: API này xử lý callback từ VNPay sau khi khách hàng thực hiện thanh toán.

**Response Body**:
```json
{
  "success": true,
  "message": "Thanh toán thành công",
  "transactionNo": "12345678",
  "amount": 1000000,
  "bookingId": 123
}
```

### 5. Kiểm Tra Lịch Sử Thanh Toán

**Endpoint**: `GET /api/v1/payments/history/{bookingId}`

**Mô tả**: API này lấy thông tin thanh toán của một đặt phòng.

**Response Body (Thành công)**:
```json
{
  "id": 1,
  "transactionNo": "12345678",
  "amount": 1000000,
  "status": "00",
  "orderInfo": "Thanh toán đặt phòng",
  "createdAt": "2025-04-07T10:15:30",
  "bookingId": 123
}
```

**Response Body (Không tìm thấy)**:
```json
{
  "success": false,
  "message": "Không tìm thấy thanh toán cho đặt phòng này"
}
```

### 6. Cập Nhật Trạng Thái Thanh Toán (Dành cho phát triển)

**Endpoint**: `POST /api/v1/payments/update-status/{transactionNo}`

**Mô tả**: API này cập nhật trạng thái của một giao dịch thanh toán.

**Response Body**:
```json
{
  "success": true,
  "message": "Đã cập nhật trạng thái thanh toán",
  "transactionNo": "12345678",
  "newStatus": "13",
  "bookingId": 123
}
```

## Mã trạng thái VNPay

| Mã | Ý nghĩa |
|----|---------|
| 00 | Thanh toán thành công |
| 01 | Giao dịch đã tồn tại |
| 02 | Merchant không hợp lệ |
| 03 | Dữ liệu gửi sang không đúng định dạng |
| 04 | Khởi tạo giao dịch thành công |
| 09 | Giao dịch đã quá thời gian chờ thanh toán |
| 10 | Giao dịch bị từ chối bởi ngân hàng thanh toán |
| 11 | Giao dịch bị hủy |
| 12 | Giao dịch bị từ chối |
| 13 | Khách hàng hủy giao dịch |
| 24 | Giao dịch không thành công do khách hàng nhập sai thông tin |
| 51 | Tài khoản không đủ số dư |
| 65 | Tài khoản khách hàng vượt quá hạn mức giao dịch trong ngày |
| 75 | Ngân hàng thanh toán đang bảo trì |
| 79 | Giao dịch nghi ngờ gian lận |
| 99 | Sai chữ ký |

## Cấu hình VNPay (application.properties)

```properties
# Cấu hình VNPay
vnpay.tmnCode=M7LG94H1
vnpay.secretKey=QJZQZQZQZQZQZQZQZQZQZQZQZQZQZQZ
vnpay.url=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
vnpay.version=2.1.0
```

| Tham số | Mô tả |
|---------|-------|
| vnpay.tmnCode | Mã merchant do VNPay cấp |
| vnpay.secretKey | Khóa bí mật để tạo chữ ký |
| vnpay.url | URL cổng thanh toán VNPay |
| vnpay.version | Phiên bản API VNPay |

## Luồng thanh toán kết hợp đặt phòng (Đề xuất)

1. **Kiểm tra phòng trống và tạo thanh toán**:
   - Frontend gọi API `POST /api/v1/payments/create-booking-payment` với thông tin đặt phòng đầy đủ
   - Backend kiểm tra xem phòng có khả dụng trong khoảng thời gian yêu cầu không
   - Nếu phòng không khả dụng, trả về thông tin xung đột để frontend có thể hiển thị
   - Nếu phòng khả dụng, tạo đặt phòng và thanh toán, trả về URL thanh toán VNPay

2. **Thanh toán trên VNPay**:
   - Frontend điều hướng khách hàng đến URL thanh toán VNPay
   - Khách hàng thực hiện thanh toán trên cổng VNPay
   - VNPay xử lý giao dịch và gửi kết quả về

3. **Xử lý callback**:
   - VNPay điều hướng khách hàng về `returnUrl` (callback URL)
   - Backend xử lý callback qua API `GET /api/v1/payments/callback`
   - Backend cập nhật trạng thái thanh toán và đặt phòng thành công

4. **Xác nhận kết quả**:
   - Frontend kiểm tra `bookingId` trong kết quả để điều hướng về trang chi tiết đặt phòng
   - Frontend hiển thị thông báo xác nhận đặt phòng thành công

## Luồng thanh toán thông thường

1. **Tạo thanh toán**:
   - Frontend gọi API `POST /api/v1/payments/create` để tạo thanh toán, truyền `bookingId` nếu có
   - Backend lưu thông tin thanh toán với trạng thái `01` (chờ thanh toán) và trả về `paymentUrl`
   - Frontend điều hướng khách hàng đến `paymentUrl`

2. **Thanh toán trên VNPay**:
   - Khách hàng thực hiện thanh toán trên cổng VNPay
   - VNPay xử lý giao dịch và gửi kết quả về

3. **Xử lý callback**:
   - VNPay điều hướng khách hàng về `returnUrl` (callback URL)
   - Backend xử lý callback qua API `GET /api/v1/payments/callback`
   - Backend cập nhật trạng thái thanh toán thành `00` (thành công) hoặc mã lỗi khác

4. **Kiểm tra kết quả**:
   - Frontend sử dụng API `GET /api/v1/payments/check-status/{transactionNo}` để kiểm tra trạng thái thanh toán
   - Frontend kiểm tra `bookingId` trong kết quả để xác nhận booking được thanh toán
   - Frontend hiển thị kết quả thanh toán cho khách hàng

5. **Xem thông tin thanh toán** (nếu cần):
   - Backend có thể hiển thị thông tin thanh toán qua API `GET /api/v1/payments/history/{bookingId}`

## Bảo mật

1. **Kiểm tra chữ ký**:
   - Mọi callback từ VNPay đều có chữ ký bảo mật (`vnp_SecureHash`)
   - Backend kiểm tra chữ ký để đảm bảo dữ liệu không bị giả mạo
   - Trong môi trường phát triển, việc kiểm tra chữ ký có thể được bỏ qua

2. **Xác thực giao dịch**:
   - Backend lưu trữ thông tin giao dịch và kiểm tra khớp với dữ liệu từ VNPay
   - Các trường quan trọng như `vnp_TxnRef`, `vnp_Amount` cần được kiểm tra nghiêm ngặt

## Xử lý lỗi

- **Giao dịch không tồn tại**: Trả về thông báo lỗi "Không tìm thấy giao dịch"
- **Sai chữ ký**: Trả về thông báo lỗi "Sai chữ ký"
- **Lỗi khởi tạo thanh toán**: Trả về thông báo lỗi chi tiết
- **Lỗi xử lý callback**: Trả về thông báo lỗi chi tiết
- **Phòng không khả dụng**: Trả về danh sách phòng không khả dụng và thông tin xung đột

## Môi trường phát triển vs. Sản xuất

- **Môi trường phát triển (dev)**:
  - Sử dụng VNPay Sandbox
  - Có thể bỏ qua kiểm tra chữ ký
  - Dữ liệu thanh toán có thể được giả lập

- **Môi trường sản xuất (prod)**:
  - Sử dụng VNPay Production
  - Kiểm tra chữ ký bắt buộc
  - Cần cấu hình thông tin merchant thật 