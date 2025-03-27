package com.spring3.hotel.management.controllers;

import com.spring3.hotel.management.models.Offering;
import com.spring3.hotel.management.services.OfferingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/offerings")
public class OfferingController {

    @Autowired
    private OfferingService offeringService;

    // Tạo mới một dịch vụ
    @PostMapping("/create")
    public ResponseEntity<Offering> createOffering(@RequestBody Offering offering) {
        Offering newOffering = offeringService.createService(offering);
        return new ResponseEntity<>(newOffering, HttpStatus.CREATED);
    }

    // Cập nhật thông tin dịch vụ theo ID
    @PutMapping("/update/{id}")
    public ResponseEntity<Offering> updateOffering(@PathVariable Integer id, @RequestBody Offering offering) {
        Offering updatedOffering = offeringService.updateService(id, offering);
        return new ResponseEntity<>(updatedOffering, HttpStatus.OK);
    }

    // Xóa dịch vụ theo ID
    @DeleteMapping("/remove/{id}")
    public ResponseEntity<Void> deleteOffering(@PathVariable Integer id) {
        offeringService.deleteService(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // Lấy thông tin dịch vụ theo ID
    @GetMapping("/{id}")
    public ResponseEntity<Offering> getOfferingById(@PathVariable Integer id) {
        Offering offering = offeringService.getServiceById(id);
        return new ResponseEntity<>(offering, HttpStatus.OK);
    }

    // Lấy danh sách dịch vụ theo tên
    @GetMapping("/name/{name}")
    public ResponseEntity<List<Offering>> getOfferingsByName(@PathVariable String name) {
        List<Offering> offerings = offeringService.getServicesByName(name);
        return new ResponseEntity<>(offerings, HttpStatus.OK);
    }

    // Lấy danh sách dịch vụ có giá nhỏ hơn giá cho trước
    @GetMapping("/price-less-than/{price}")
    public ResponseEntity<List<Offering>> getOfferingsByPriceLessThan(@PathVariable Double price) {
        List<Offering> offerings = offeringService.getServicesByPrice(price);
        return new ResponseEntity<>(offerings, HttpStatus.OK);
    }

    // Lấy tất cả dịch vụ
    @GetMapping("/")
    public ResponseEntity<List<Offering>> getAllOfferings() {
        List<Offering> offerings = offeringService.getAllServices();
        return new ResponseEntity<>(offerings, HttpStatus.OK);
    }
}