package com.spring3.hotel.management.services.impl;

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

import java.math.BigDecimal;
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
} 
