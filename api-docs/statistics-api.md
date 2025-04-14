# API Thống kê và Báo cáo

API cho thống kê và báo cáo trong hệ thống quản lý khách sạn. Các API này cung cấp dữ liệu thống kê và báo cáo cho bảng điều khiển (dashboard).

## Danh sách API thống kê

### 1. Lấy thông tin tổng quan cho dashboard

```
GET /api/v1/statistics/count-info
```

Lấy thông tin tổng quan bao gồm tổng số phòng, tổng số đặt phòng, tổng số khách hàng, tổng doanh thu và tổng số đánh giá.

#### Phản hồi thành công (200 OK)

```json
{
  "totalBookings": 28, 
  "totalCustomers": 22,
  "totalRates": 8,
  "totalRevenue": 142500000.0
}
```

### 2. Lấy thống kê theo khoảng thời gian

```
GET /api/v1/statistics/date-range?startDate=2023-10-01T00:00:00&endDate=2023-10-31T23:59:59
```

Lấy dữ liệu thống kê theo khoảng thời gian chỉ định.

#### Tham số

- `startDate`: Ngày bắt đầu theo định dạng ISO (YYYY-MM-DDTHH:MM:SS)
- `endDate`: Ngày kết thúc theo định dạng ISO (YYYY-MM-DDTHH:MM:SS)

#### Phản hồi thành công (200 OK)

```json
[
  {
    "id": 1,
    "date": "2023-10-01T23:59:59.999999999",
    "totalRevenue": 12500000.0,
    "totalBookings": 5,
    "totalCustomers": 5,
    "totalRates": 2
  },
  {
    "id": 2,
    "date": "2023-10-02T23:59:59.999999999",
    "totalRevenue": 15600000.0,
    "totalBookings": 6,
    "totalCustomers": 6,
    "totalRates": 3
  }
]
```

### 3. Lấy thông tin đặt phòng gần đây

```
GET /api/v1/statistics/recent-bookings?days=7
```

Lấy danh sách đặt phòng gần đây trong khoảng thời gian chỉ định.

#### Tham số

- `days`: Số ngày (mặc định là 7)

#### Phản hồi thành công (200 OK)

```json
[
  {
    "id": 5,
    "customerName": "Hoàng Văn E",
    "roomNumber": "502",
    "roomType": "Suite Deluxe",
    "totalPrice": 2500000.0,
    "checkInDate": "2023-10-19",
    "checkOutDate": "2023-10-21",
    "status": "CONFIRMED",
    "createdAt": "2023-10-18T09:45:23"
  },
  {
    "id": 4,
    "customerName": "Phạm Thanh D",
    "roomNumber": "401",
    "roomType": "Junior Suite",
    "totalPrice": 1800000.0,
    "checkInDate": "2023-10-18",
    "checkOutDate": "2023-10-20",
    "status": "CANCELLED",
    "createdAt": "2023-10-17T14:22:10"
  }
]
```

### 4. Lấy thống kê đánh giá theo số sao

```
GET /api/v1/statistics/reviews-by-rating
```

Lấy thống kê số lượng đánh giá theo số sao (1-5).

#### Phản hồi thành công (200 OK)

```json
{
  "1": 2,
  "2": 5,
  "3": 10,
  "4": 15,
  "5": 8
}
```

### 5. Lấy danh sách phòng được đặt nhiều nhất

```
GET /api/v1/statistics/most-booked-rooms?limit=5
```

Lấy danh sách phòng được đặt nhiều nhất.

#### Tham số

- `limit`: Số lượng phòng muốn lấy (mặc định là 5)

#### Phản hồi thành công (200 OK)

```json
[
  {
    "roomNumber": "502",
    "roomType": "Suite Deluxe",
    "bookingCount": 12,
    "totalRevenue": 30000000.0,
    "occupancyRate": 75.5
  },
  {
    "roomNumber": "305",
    "roomType": "Superior",
    "bookingCount": 10,
    "totalRevenue": 21000000.0,
    "occupancyRate": 68.2
  }
]
```

### 6. Thống kê doanh thu theo ngày trong tháng hiện tại

```
GET /api/v1/statistics/revenue-by-day
```

Lấy doanh thu theo từng ngày trong tháng hiện tại.

#### Phản hồi thành công (200 OK)

```json
{
  "01/10/2023": 1500000.0,
  "02/10/2023": 2100000.0,
  "03/10/2023": 1800000.0
}
```

### 7. Thống kê số lượng đặt phòng theo ngày trong tháng hiện tại

```
GET /api/v1/statistics/bookings-by-day
```

Lấy số lượng đặt phòng theo từng ngày trong tháng hiện tại.

#### Phản hồi thành công (200 OK)

```json
{
  "01/10/2023": 3,
  "02/10/2023": 5,
  "03/10/2023": 4
}
```

### 8. So sánh doanh thu giữa tháng hiện tại và tháng trước

```
GET /api/v1/statistics/revenue-comparison
```

Lấy dữ liệu so sánh doanh thu giữa tháng hiện tại và tháng trước.

#### Phản hồi thành công (200 OK)

```json
{
  "currentMonth": 142500000.0,
  "previousMonth": 120000000.0,
  "percentChange": 18.75
}
```

### 9. Thống kê tỷ lệ đặt phòng theo trạng thái

```
GET /api/v1/statistics/booking-status
```

Lấy số lượng đặt phòng theo từng trạng thái.

#### Phản hồi thành công (200 OK)

```json
{
  "PENDING": 5,
  "CONFIRMED": 12,
  "CHECKED_IN": 3,
  "CHECKED_OUT": 8,
  "CANCELLED": 4
}
```

## Mã lỗi

Mã lỗi chung cho tất cả các API:

| Mã lỗi | Thông báo                     | Mô tả                           |
|--------|-------------------------------|----------------------------------|
| 400    | Bad Request                   | Tham số không hợp lệ            |
| 401    | Unauthorized                  | Chưa xác thực                   |
| 403    | Forbidden                     | Không có quyền truy cập         |
| 404    | Not Found                     | Không tìm thấy tài nguyên       |
| 500    | Internal Server Error         | Lỗi hệ thống                    | 