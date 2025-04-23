package com.spring3.hotel.management.mappers;

import com.spring3.hotel.management.dto.ServiceDTO;
import com.spring3.hotel.management.models.HotelService;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ServiceMapper {
    ServiceMapper INSTANCE = Mappers.getMapper(ServiceMapper.class);

    ServiceDTO toDTO(HotelService service);
    HotelService toEntity(ServiceDTO serviceDTO);
} 
