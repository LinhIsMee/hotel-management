package com.spring3.hotel.management.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring3.hotel.management.dto.request.UpsertServiceRequest;
import com.spring3.hotel.management.dto.response.ServiceResponseDTO;
import com.spring3.hotel.management.enums.ServiceType;
import com.spring3.hotel.management.exceptions.ResourceNotFoundException;
import com.spring3.hotel.management.models.HotelService;
import com.spring3.hotel.management.repositories.ServiceRepository;
import com.spring3.hotel.management.services.interfaces.ServiceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@Slf4j
public class ServiceServiceImpl implements ServiceService {
    
    @Autowired
    private ServiceRepository serviceRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Override
    public List<ServiceResponseDTO> getAllServices() {
        return serviceRepository.findAll()
                .stream()
                .map(ServiceResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public ServiceResponseDTO getServiceById(Integer id) {
        HotelService service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ với ID: " + id));
        return ServiceResponseDTO.fromEntity(service);
    }
    
    @Override
    public ServiceResponseDTO getServiceByCode(String code) {
        HotelService service = serviceRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ với mã: " + code));
        return ServiceResponseDTO.fromEntity(service);
    }
    
    @Override
    public List<ServiceResponseDTO> getServicesByType(String typeStr) {
        try {
            ServiceType type = ServiceType.valueOf(typeStr.toUpperCase());
            return serviceRepository.findByType(type)
                    .stream()
                    .map(ServiceResponseDTO::fromEntity)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            log.warn("Loại dịch vụ không hợp lệ: {}", typeStr);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<ServiceResponseDTO> getServicesByName(String name) {
        return serviceRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(ServiceResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ServiceResponseDTO> getServicesByMaxPrice(Double priceDouble) {
        BigDecimal price = BigDecimal.valueOf(priceDouble);
        return serviceRepository.findByPriceLessThanEqual(price)
                .stream()
                .map(ServiceResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ServiceResponseDTO> getAvailableServices() {
        return serviceRepository.findByIsAvailable(true)
                .stream()
                .map(ServiceResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public ServiceResponseDTO createService(UpsertServiceRequest request) {
        // Kiểm tra mã dịch vụ đã tồn tại chưa
        if (serviceRepository.findByCode(request.getCode()).isPresent()) {
            throw new IllegalArgumentException("Mã dịch vụ đã tồn tại: " + request.getCode());
        }
        
        // Tạo mới đối tượng Service
        HotelService service = HotelService.builder()
                .name(request.getName())
                .code(request.getCode())
                .type(request.getType())
                .description(request.getDescription())
                .price(request.getPrice())
                .isAvailable(request.getIsAvailable())
                .imageUrl(request.getImageUrl())
                .build();
        
        HotelService savedService = serviceRepository.save(service);
        return ServiceResponseDTO.fromEntity(savedService);
    }
    
    @Override
    public ServiceResponseDTO updateService(UpsertServiceRequest request, Integer id) {
        HotelService existingService = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ với ID: " + id));
        
        // Kiểm tra mã dịch vụ đã tồn tại chưa và không phải là dịch vụ hiện tại
        if (!existingService.getCode().equals(request.getCode()) && 
                serviceRepository.findByCode(request.getCode()).isPresent()) {
            throw new IllegalArgumentException("Mã dịch vụ đã tồn tại: " + request.getCode());
        }
        
        // Cập nhật thông tin
        existingService.setName(request.getName());
        existingService.setCode(request.getCode());
        existingService.setType(request.getType());
        existingService.setDescription(request.getDescription());
        existingService.setPrice(request.getPrice());
        
        if (request.getImageUrl() != null) {
            existingService.setImageUrl(request.getImageUrl());
        }
        
        if (request.getIsAvailable() != null) {
            existingService.setIsAvailable(request.getIsAvailable());
        }
        
        HotelService updatedService = serviceRepository.save(existingService);
        return ServiceResponseDTO.fromEntity(updatedService);
    }
    
    @Override
    public void deleteService(Integer id) {
        HotelService service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ với ID: " + id));
        
        serviceRepository.delete(service);
    }
    
    @Override
    public void initServicesFromJson() {
        try {
            // Kiểm tra xem đã có dịch vụ nào trong hệ thống chưa
            if (!serviceRepository.findAll().isEmpty()) {
                log.info("Đã có dữ liệu dịch vụ trong DB, bỏ qua quá trình khởi tạo");
                return;
            }
            
            // Đọc dữ liệu từ file JSON
            log.info("Bắt đầu quá trình khởi tạo dữ liệu dịch vụ từ JSON");
            File jsonFile = new ClassPathResource("/data/services.json").getFile();
            JsonNode rootNode = objectMapper.readTree(jsonFile);
            JsonNode dataNode = rootNode.path("data");
            
            if (dataNode.isMissingNode() || !dataNode.isArray()) {
                log.warn("Không tìm thấy dữ liệu dịch vụ hợp lệ trong file JSON");
                return;
            }
            
            List<HotelService> services = new ArrayList<>();
            
            for (JsonNode node : dataNode) {
                if (!node.has("name") || !node.has("code") || !node.has("price")) {
                    log.warn("Bỏ qua dữ liệu dịch vụ không hợp lệ: {}", node);
                    continue;
                }
                
                // Chuyển đổi type thành ServiceType
                ServiceType serviceType = ServiceType.OTHERS;
                if (node.has("type")) {
                    try {
                        serviceType = ServiceType.valueOf(node.get("type").asText().toUpperCase());
                    } catch (IllegalArgumentException e) {
                        log.warn("Loại dịch vụ không hợp lệ: {}, sử dụng giá trị mặc định OTHERS", node.get("type").asText());
                    }
                }
                
                HotelService service = HotelService.builder()
                        .name(node.get("name").asText())
                        .code(node.get("code").asText())
                        .type(serviceType)
                        .description(node.has("description") ? node.get("description").asText() : null)
                        .price(new BigDecimal(node.get("price").asText()))
                        .isAvailable(node.has("isAvailable") ? node.get("isAvailable").asBoolean() : true)
                        .build();
                
                services.add(service);
            }
            
            if (!services.isEmpty()) {
                serviceRepository.saveAll(services);
                log.info("Đã khởi tạo thành công {} dịch vụ từ file JSON", services.size());
            } else {
                log.warn("Không có dữ liệu dịch vụ hợp lệ để khởi tạo");
            }
        } catch (Exception e) {
            log.error("Lỗi khi khởi tạo dữ liệu dịch vụ từ JSON: {}", e.getMessage(), e);
        }
    }
} 
