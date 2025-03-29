package com.spring3.hotel.management.services.interfaces;

import com.spring3.hotel.management.dtos.request.UpsertServiceRequest;
import com.spring3.hotel.management.dtos.response.ServiceResponseDTO;

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
    
    void initServicesFromJson();
} 