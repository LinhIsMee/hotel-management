package com.spring3.hotel.management.controllers;

import com.spring3.hotel.management.dtos.request.UpsertServiceRequest;
import com.spring3.hotel.management.dtos.response.ServiceResponseDTO;
import com.spring3.hotel.management.dtos.response.SuccessResponse;
import com.spring3.hotel.management.services.interfaces.ServiceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    @Autowired
    private ServiceService serviceService;
    
    @GetMapping
    public ResponseEntity<SuccessResponse<List<ServiceResponseDTO>>> getAllServices() {
        List<ServiceResponseDTO> services = serviceService.getAllServices();
        return ResponseEntity.ok(
                new SuccessResponse<>(HttpStatus.OK.value(), "Lấy danh sách dịch vụ thành công", services)
        );
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<ServiceResponseDTO>> getServiceById(
            @PathVariable Integer id) {
        ServiceResponseDTO service = serviceService.getServiceById(id);
        return ResponseEntity.ok(
                new SuccessResponse<>(HttpStatus.OK.value(), "Lấy thông tin dịch vụ thành công", service)
        );
    }
    
    @GetMapping("/code/{code}")
    public ResponseEntity<SuccessResponse<ServiceResponseDTO>> getServiceByCode(
            @PathVariable String code) {
        ServiceResponseDTO service = serviceService.getServiceByCode(code);
        return ResponseEntity.ok(
                new SuccessResponse<>(HttpStatus.OK.value(), "Lấy thông tin dịch vụ thành công", service)
        );
    }
    
    @GetMapping("/type/{type}")
    public ResponseEntity<SuccessResponse<List<ServiceResponseDTO>>> getServicesByType(
            @PathVariable String type) {
        List<ServiceResponseDTO> services = serviceService.getServicesByType(type);
        return ResponseEntity.ok(
                new SuccessResponse<>(HttpStatus.OK.value(), "Lấy danh sách dịch vụ theo loại thành công", services)
        );
    }
    
    @GetMapping("/search")
    public ResponseEntity<SuccessResponse<List<ServiceResponseDTO>>> searchServicesByName(
            @RequestParam String name) {
        List<ServiceResponseDTO> services = serviceService.getServicesByName(name);
        return ResponseEntity.ok(
                new SuccessResponse<>(HttpStatus.OK.value(), "Tìm kiếm dịch vụ thành công", services)
        );
    }
    
    @GetMapping("/price")
    public ResponseEntity<SuccessResponse<List<ServiceResponseDTO>>> getServicesByMaxPrice(
            @RequestParam Double price) {
        List<ServiceResponseDTO> services = serviceService.getServicesByMaxPrice(price);
        return ResponseEntity.ok(
                new SuccessResponse<>(HttpStatus.OK.value(), "Lọc dịch vụ theo giá thành công", services)
        );
    }
    
    @GetMapping("/available")
    public ResponseEntity<SuccessResponse<List<ServiceResponseDTO>>> getAvailableServices() {
        List<ServiceResponseDTO> services = serviceService.getAvailableServices();
        return ResponseEntity.ok(
                new SuccessResponse<>(HttpStatus.OK.value(), "Lấy danh sách dịch vụ khả dụng thành công", services)
        );
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
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
    public ResponseEntity<SuccessResponse<ServiceResponseDTO>> updateService(
            @PathVariable Integer id,
            @Valid @RequestBody UpsertServiceRequest request) {
        ServiceResponseDTO updatedService = serviceService.updateService(request, id);
        return ResponseEntity.ok(
                new SuccessResponse<>(HttpStatus.OK.value(), "Cập nhật dịch vụ thành công", updatedService)
        );
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<SuccessResponse<Void>> deleteService(
            @PathVariable Integer id) {
        serviceService.deleteService(id);
        return ResponseEntity.ok(
                new SuccessResponse<>(HttpStatus.OK.value(), "Xóa dịch vụ thành công", null)
        );
    }
    
    @PostMapping("/init")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<SuccessResponse<Void>> initServicesFromJson() {
        serviceService.initServicesFromJson();
        return ResponseEntity.ok(
                new SuccessResponse<>(HttpStatus.OK.value(), "Khởi tạo dữ liệu dịch vụ thành công", null)
        );
    }
} 