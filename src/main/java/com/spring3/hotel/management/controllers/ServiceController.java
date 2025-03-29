package com.spring3.hotel.management.controllers;

import com.spring3.hotel.management.dtos.request.UpsertServiceRequest;
import com.spring3.hotel.management.dtos.response.ServiceResponseDTO;
import com.spring3.hotel.management.dtos.response.SuccessResponse;
import com.spring3.hotel.management.services.interfaces.ServiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@Tag(name = "Service Management", description = "API cho quản lý dịch vụ")
public class ServiceController {

    @Autowired
    private ServiceService serviceService;
    
    @GetMapping
    @Operation(summary = "Lấy tất cả dịch vụ", description = "Trả về danh sách tất cả dịch vụ")
    public ResponseEntity<SuccessResponse<List<ServiceResponseDTO>>> getAllServices() {
        List<ServiceResponseDTO> services = serviceService.getAllServices();
        return ResponseEntity.ok(
                new SuccessResponse<>(HttpStatus.OK.value(), "Lấy danh sách dịch vụ thành công", services)
        );
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Lấy dịch vụ theo ID", description = "Trả về thông tin dịch vụ theo ID")
    public ResponseEntity<SuccessResponse<ServiceResponseDTO>> getServiceById(
            @Parameter(description = "ID của dịch vụ") @PathVariable Integer id) {
        ServiceResponseDTO service = serviceService.getServiceById(id);
        return ResponseEntity.ok(
                new SuccessResponse<>(HttpStatus.OK.value(), "Lấy thông tin dịch vụ thành công", service)
        );
    }
    
    @GetMapping("/code/{code}")
    @Operation(summary = "Lấy dịch vụ theo mã", description = "Trả về thông tin dịch vụ theo mã dịch vụ")
    public ResponseEntity<SuccessResponse<ServiceResponseDTO>> getServiceByCode(
            @Parameter(description = "Mã dịch vụ") @PathVariable String code) {
        ServiceResponseDTO service = serviceService.getServiceByCode(code);
        return ResponseEntity.ok(
                new SuccessResponse<>(HttpStatus.OK.value(), "Lấy thông tin dịch vụ thành công", service)
        );
    }
    
    @GetMapping("/type/{type}")
    @Operation(summary = "Lấy dịch vụ theo loại", description = "Trả về danh sách dịch vụ theo loại")
    public ResponseEntity<SuccessResponse<List<ServiceResponseDTO>>> getServicesByType(
            @Parameter(description = "Loại dịch vụ") @PathVariable String type) {
        List<ServiceResponseDTO> services = serviceService.getServicesByType(type);
        return ResponseEntity.ok(
                new SuccessResponse<>(HttpStatus.OK.value(), "Lấy danh sách dịch vụ theo loại thành công", services)
        );
    }
    
    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm dịch vụ theo tên", description = "Tìm kiếm và trả về danh sách dịch vụ theo tên")
    public ResponseEntity<SuccessResponse<List<ServiceResponseDTO>>> searchServicesByName(
            @Parameter(description = "Từ khóa tìm kiếm") @RequestParam String name) {
        List<ServiceResponseDTO> services = serviceService.getServicesByName(name);
        return ResponseEntity.ok(
                new SuccessResponse<>(HttpStatus.OK.value(), "Tìm kiếm dịch vụ thành công", services)
        );
    }
    
    @GetMapping("/price")
    @Operation(summary = "Lọc dịch vụ theo giá tối đa", description = "Trả về danh sách dịch vụ có giá không vượt quá mức giá chỉ định")
    public ResponseEntity<SuccessResponse<List<ServiceResponseDTO>>> getServicesByMaxPrice(
            @Parameter(description = "Giá tối đa") @RequestParam Double price) {
        List<ServiceResponseDTO> services = serviceService.getServicesByMaxPrice(price);
        return ResponseEntity.ok(
                new SuccessResponse<>(HttpStatus.OK.value(), "Lọc dịch vụ theo giá thành công", services)
        );
    }
    
    @GetMapping("/available")
    @Operation(summary = "Lấy danh sách dịch vụ khả dụng", description = "Trả về danh sách các dịch vụ có trạng thái khả dụng")
    public ResponseEntity<SuccessResponse<List<ServiceResponseDTO>>> getAvailableServices() {
        List<ServiceResponseDTO> services = serviceService.getAvailableServices();
        return ResponseEntity.ok(
                new SuccessResponse<>(HttpStatus.OK.value(), "Lấy danh sách dịch vụ khả dụng thành công", services)
        );
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "Tạo mới dịch vụ", description = "Tạo mới một dịch vụ")
    public ResponseEntity<SuccessResponse<ServiceResponseDTO>> createService(
            @Valid @RequestBody UpsertServiceRequest request) {
        ServiceResponseDTO createdService = serviceService.createService(request);
        return new ResponseEntity<>(
                new SuccessResponse<>(HttpStatus.CREATED.value(), "Tạo mới dịch vụ thành công", createdService),
                HttpStatus.CREATED
        );
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "Cập nhật dịch vụ", description = "Cập nhật thông tin dịch vụ theo ID")
    public ResponseEntity<SuccessResponse<ServiceResponseDTO>> updateService(
            @Parameter(description = "ID của dịch vụ") @PathVariable Integer id,
            @Valid @RequestBody UpsertServiceRequest request) {
        ServiceResponseDTO updatedService = serviceService.updateService(request, id);
        return ResponseEntity.ok(
                new SuccessResponse<>(HttpStatus.OK.value(), "Cập nhật dịch vụ thành công", updatedService)
        );
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "Xóa dịch vụ", description = "Xóa dịch vụ theo ID")
    public ResponseEntity<SuccessResponse<Void>> deleteService(
            @Parameter(description = "ID của dịch vụ") @PathVariable Integer id) {
        serviceService.deleteService(id);
        return ResponseEntity.ok(
                new SuccessResponse<>(HttpStatus.OK.value(), "Xóa dịch vụ thành công", null)
        );
    }
    
    @PostMapping("/init")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Khởi tạo dữ liệu dịch vụ", description = "Khởi tạo dữ liệu dịch vụ từ file JSON")
    public ResponseEntity<SuccessResponse<Void>> initServicesFromJson() {
        serviceService.initServicesFromJson();
        return ResponseEntity.ok(
                new SuccessResponse<>(HttpStatus.OK.value(), "Khởi tạo dữ liệu dịch vụ thành công", null)
        );
    }
} 