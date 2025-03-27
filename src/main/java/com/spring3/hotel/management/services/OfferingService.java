package com.spring3.hotel.management.services;

import com.spring3.hotel.management.models.Offering;

import java.util.List;

// quan ly cac dich vu cua khach san
public interface OfferingService {
    Offering createService(Offering offering);
    Offering updateService(Integer id, Offering offering);
    Offering deleteService(Integer id);
    Offering getServiceById(Integer id);
    List<Offering> getServicesByName(String name);
    List<Offering> getServicesByPrice(Double price);
    List<Offering> getAllServices();

}
