# Ứng dụng Quản lý Khách sạn

## Cài đặt và chạy với Docker

### Yêu cầu hệ thống
- Docker
- Docker Compose

### Các bước chạy ứng dụng
1. Clone repository:
```bash
git clone [repository-url]
cd hotel-management
```

2. Chạy ứng dụng bằng Docker Compose:
```bash
docker-compose up -d
```

3. Kiểm tra ứng dụng:
- Backend API: http://localhost:9000
- Database MySQL: localhost:3307 (có thể truy cập từ bên ngoài Docker)

### Cấu hình môi trường
Ứng dụng sẽ sử dụng các biến môi trường từ file docker-compose.yml. Bạn có thể thay đổi các giá trị trong file này trước khi chạy ứng dụng.

### Ngừng ứng dụng
```bash
docker-compose down
```

### Xóa dữ liệu và chạy lại
```bash
docker-compose down -v
docker-compose up -d
```

## Phát triển ứng dụng

### Thay đổi mã nguồn
Sau khi thay đổi mã nguồn, bạn cần rebuild lại Docker image:
```bash
docker-compose down
docker-compose build
docker-compose up -d
```

### Xem logs
```bash
docker-compose logs -f app
```

### Truy cập vào container
```bash
docker exec -it hotel-management-app bash
```

### Truy cập vào database
```bash
docker exec -it hotel-management-db mysql -uhotel_management -pT258ChaZ7M2Q6sBe hotel_management
``` 