package com.spring3.hotel.management.services;

import com.spring3.hotel.management.dto.request.UpsertServiceRequest;
import com.spring3.hotel.management.dto.response.ServiceResponseDTO;

import java.util.List;

public interface ServiceService {
    
    List<ServiceResponseDTO> getAllServices();
    
    ServiceResponseDTO getServiceById(Integer id);
    
    ServiceResponseDTO getServiceByCode(String code);
    
    List<ServiceResponseDTO> getServicesByType(String type);
    
    List<ServiceResponseDTO> getServicesByName(String name);
    
    List<ServiceResponseDTO> getServicesByMaxPrice(Double price);
    
    List<ServiceResponseDTO> getAvailableServices();
    
    ServiceResponseDTO createService(UpsertServiceRequest request);
    
    ServiceResponseDTO updateService(UpsertServiceRequest request, Integer id);
    
    void deleteService(Integer id);
} 
