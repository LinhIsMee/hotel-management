package com.spring3.hotel.management.services.impl;

import com.spring3.hotel.management.repositories.OfferingsRepository;
import com.spring3.hotel.management.services.OfferingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import com.spring3.hotel.management.models.Offering;
import java.util.List;

@Service
public class OfferingServiceImpl implements OfferingService {

    @Autowired
    private OfferingsRepository offeringsRepository;

    @Override
    public Offering createService(Offering offering) {
        Offering newOffering = new Offering();
        //check trung ten hoac bi null
        if (offering.getName() == null || offeringsRepository.findByName(offering.getName()).isPresent()) {
            throw new RuntimeException("Name is null or already exists");
        }
        newOffering.setName(offering.getName());
        if (offering.getPrice() == null) {
            throw new RuntimeException("Price is null");
        }
        newOffering.setPrice(offering.getPrice());
        newOffering.setDescription(offering.getDescription());
        return offeringsRepository.save(newOffering);
    }

    @Override
    public Offering updateService(Integer id, Offering offering) {
        Offering offeringToUpdate = offeringsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offering not found"));
        //check ten bi trung hoac null
        if (offering.getName() == null || offeringsRepository.findByName(offering.getName()).isPresent()) {
            throw new RuntimeException("Name is null or already exists");
        }
        offeringToUpdate.setName(offering.getName());
        if (offering.getPrice() == null) {
            throw new RuntimeException("Price is null");
        }
        offeringToUpdate.setPrice(offering.getPrice());
        offeringToUpdate.setDescription(offering.getDescription());
        return offeringsRepository.save(offeringToUpdate);
    }

    @Override
    public Offering deleteService(Integer id) {
        Offering offeringToDelete = offeringsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offering not found"));
        offeringsRepository.delete(offeringToDelete);
        return offeringToDelete;
    }

    @Override
    public Offering getServiceById(Integer id) {
        return offeringsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offering not found"));
    }

    @Override
    public List<Offering> getServicesByName(String name) {
        return offeringsRepository.findByName(name)
                .map(List::of)
                .orElse(List.of());
    }

    // tim cac dich vu co gia nho hon gia cho truoc
    @Override
    public List<Offering> getServicesByPrice(Double price) {
        return offeringsRepository.findByPriceLessThan(price);
    }

    @Override
    public List<Offering> getAllServices() {
        return offeringsRepository.findAll();
    }
}
