package com.spring3.hotel.management.controllers;

import com.spring3.hotel.management.dto.request.UpsertServiceRequest;
import com.spring3.hotel.management.dto.response.ServiceResponseDTO;
import com.spring3.hotel.management.dto.response.SuccessResponse;
import com.spring3.hotel.management.services.ServiceService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/services")
public class ServiceController {

    @Autowired
    private ServiceService serviceService;
    
    /**
     * Lấy danh sách tất cả dịch vụ
     */
    @GetMapping
    public ResponseEntity<SuccessResponse<List<ServiceResponseDTO>>> getAllServices() {
        List<ServiceResponseDTO> services = serviceService.getAllServices();
        return ResponseEntity.ok(
                new SuccessResponse<>(HttpStatus.OK.value(), "Lấy danh sách dịch vụ thành công", services)
        );
    }
    
    /**
     * Lấy thông tin dịch vụ theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<ServiceResponseDTO>> getServiceById(
            @PathVariable Integer id) {
        ServiceResponseDTO service = serviceService.getServiceById(id);
        return ResponseEntity.ok(
                new SuccessResponse<>(HttpStatus.OK.value(), "Lấy thông tin dịch vụ thành công", service)
        );
    }
    
    /**
     * Lấy thông tin dịch vụ theo mã
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<SuccessResponse<ServiceResponseDTO>> getServiceByCode(
            @PathVariable String code) {
        ServiceResponseDTO service = serviceService.getServiceByCode(code);
        return ResponseEntity.ok(
                new SuccessResponse<>(HttpStatus.OK.value(), "Lấy thông tin dịch vụ thành công", service)
        );
    }
    
    /**
     * Tìm kiếm và lọc dịch vụ với nhiều tiêu chí
     * Hỗ trợ lọc theo loại, tên, giá tối đa và trạng thái khả dụng
     */
    @GetMapping("/search")
    public ResponseEntity<SuccessResponse<List<ServiceResponseDTO>>> searchServices(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Double price,
            @RequestParam(required = false) Boolean available) {
        
        List<ServiceResponseDTO> services;
        String message = "Tìm kiếm dịch vụ thành công";
        
        if (type != null && !type.isEmpty()) {
            // Lọc theo loại dịch vụ
            services = serviceService.getServicesByType(type);
            message = "Lấy danh sách dịch vụ theo loại thành công";
        } else if (name != null && !name.isEmpty()) {
            // Tìm kiếm theo tên
            services = serviceService.getServicesByName(name);
            message = "Tìm kiếm dịch vụ theo tên thành công";
        } else if (price != null) {
            // Lọc theo giá tối đa
            services = serviceService.getServicesByMaxPrice(price);
            message = "Lọc dịch vụ theo giá thành công";
        } else if (available != null && available) {
            // Lấy dịch vụ khả dụng
            services = serviceService.getAvailableServices();
            message = "Lấy danh sách dịch vụ khả dụng thành công";
        } else {
            // Lấy tất cả dịch vụ nếu không có tiêu chí lọc
            services = serviceService.getAllServices();
            message = "Lấy danh sách tất cả dịch vụ thành công";
        }
        
        return ResponseEntity.ok(
                new SuccessResponse<>(HttpStatus.OK.value(), message, services)
        );
    }
    
    /**
     * Lấy danh sách dịch vụ theo loại (giữ lại để tương thích ngược)
     * @deprecated API này sẽ bị loại bỏ trong phiên bản tới. Vui lòng sử dụng API GET /search?type={type} thay thế
     */
    @GetMapping("/type/{type}")
    @Deprecated
    public ResponseEntity<SuccessResponse<List<ServiceResponseDTO>>> getServicesByType(
            @PathVariable String type) {
        List<ServiceResponseDTO> services = serviceService.getServicesByType(type);
        return ResponseEntity.ok(
                new SuccessResponse<>(HttpStatus.OK.value(), 
                "Lấy danh sách dịch vụ theo loại thành công. API này sẽ bị loại bỏ, vui lòng sử dụng /search?type={type}", 
                services)
        );
    }
    
    /**
     * Lấy danh sách dịch vụ đang khả dụng (giữ lại để tương thích ngược)
     * @deprecated API này sẽ bị loại bỏ trong phiên bản tới. Vui lòng sử dụng API GET /search?available=true thay thế
     */
    @GetMapping("/available")
    @Deprecated
    public ResponseEntity<SuccessResponse<List<ServiceResponseDTO>>> getAvailableServices() {
        List<ServiceResponseDTO> services = serviceService.getAvailableServices();
        return ResponseEntity.ok(
                new SuccessResponse<>(HttpStatus.OK.value(), 
                "Lấy danh sách dịch vụ khả dụng thành công. API này sẽ bị loại bỏ, vui lòng sử dụng /search?available=true", 
                services)
        );
    }
    
    /**
     * Tạo mới dịch vụ
     */
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
    
    /**
     * Cập nhật thông tin dịch vụ
     */
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
    
    /**
     * Xóa dịch vụ
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<SuccessResponse<Void>> deleteService(
            @PathVariable Integer id) {
        serviceService.deleteService(id);
        return ResponseEntity.ok(
                new SuccessResponse<>(HttpStatus.OK.value(), "Xóa dịch vụ thành công", null)
        );
    }
}
