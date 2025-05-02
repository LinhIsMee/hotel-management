package com.spring3.hotel.management.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring3.hotel.management.dto.response.ServiceResponseDTO;
import com.spring3.hotel.management.dto.request.UpsertServiceRequest;
import com.spring3.hotel.management.exceptions.ResourceNotFoundException;
import com.spring3.hotel.management.models.Service;
import com.spring3.hotel.management.repositories.ServiceRepository;
import com.spring3.hotel.management.services.interfaces.ServiceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDate;
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
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ với ID: " + id));
        return ServiceResponseDTO.fromEntity(service);
    }
    
    @Override
    public ServiceResponseDTO getServiceByCode(String code) {
        Service service = serviceRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ với mã: " + code));
        return ServiceResponseDTO.fromEntity(service);
    }
    
    @Override
    public List<ServiceResponseDTO> getServicesByType(String type) {
        return serviceRepository.findByType(type)
                .stream()
                .map(ServiceResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ServiceResponseDTO> getServicesByName(String name) {
        return serviceRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(ServiceResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ServiceResponseDTO> getServicesByMaxPrice(Double price) {
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
        Service service = Service.builder()
                .name(request.getName())
                .code(request.getCode())
                .type(request.getType())
                .description(request.getDescription())
                .price(request.getPrice())
                .unit(request.getUnit())
                .isAvailable(request.getIsAvailable())
                .build();
        
        Service savedService = serviceRepository.save(service);
        return ServiceResponseDTO.fromEntity(savedService);
    }
    
    @Override
    public ServiceResponseDTO updateService(UpsertServiceRequest request, Integer id) {
        Service existingService = serviceRepository.findById(id)
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
        existingService.setUnit(request.getUnit());
        
        if (request.getIsAvailable() != null) {
            existingService.setIsAvailable(request.getIsAvailable());
        }
        
        Service updatedService = serviceRepository.save(existingService);
        return ServiceResponseDTO.fromEntity(updatedService);
    }
    
    @Override
    public void deleteService(Integer id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ với ID: " + id));
        
        serviceRepository.delete(service);
    }
    
    @Override
    public void initServicesFromJson() {
        try {
            log.info("Bắt đầu khởi tạo dữ liệu dịch vụ từ file JSON...");
            
            // Kiểm tra xem đã có dữ liệu trong DB chưa
            if (serviceRepository.count() > 0) {
                log.info("Dữ liệu dịch vụ đã tồn tại trong DB, bỏ qua việc khởi tạo.");
                return;
            }
            
            // Đọc file JSON
            File jsonFile = Paths.get("data", "services.json").toFile();
            if (!jsonFile.exists()) {
                log.warn("Không tìm thấy file dữ liệu dịch vụ JSON: {}", jsonFile.getAbsolutePath());
                return;
            }
            
            JsonNode rootNode = objectMapper.readTree(jsonFile);
            JsonNode dataNode = rootNode.get("data");
            
            if (dataNode == null || !dataNode.isArray() || dataNode.isEmpty()) {
                log.warn("Không có dữ liệu hợp lệ trong file JSON.");
                return;
            }
            
            List<Service> services = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            
            for (JsonNode node : dataNode) {
                LocalDate createdAt = null;
                if (node.has("createdAt") && !node.get("createdAt").isNull()) {
                    createdAt = LocalDate.parse(node.get("createdAt").asText(), formatter);
                }
                
                Service service = Service.builder()
                        .name(node.get("name").asText())
                        .code(node.get("code").asText())
                        .type(node.get("type").asText())
                        .description(node.has("description") ? node.get("description").asText() : null)
                        .price(node.get("price").asDouble())
                        .unit(node.get("unit").asText())
                        .isAvailable(node.has("isAvailable") ? node.get("isAvailable").asBoolean() : true)
                        .createdAt(createdAt)
                        .build();
                
                services.add(service);
            }
            
            serviceRepository.saveAll(services);
            log.info("Đã khởi tạo thành công {} dịch vụ từ file JSON.", services.size());
            
        } catch (Exception e) {
            log.error("Lỗi khi khởi tạo dữ liệu dịch vụ từ file JSON: {}", e.getMessage(), e);
        }
    }
} 