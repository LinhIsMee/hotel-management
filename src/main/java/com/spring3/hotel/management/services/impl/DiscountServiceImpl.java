package com.spring3.hotel.management.services.impl;

import com.spring3.hotel.management.dtos.request.UpsertDiscountRequest;
import com.spring3.hotel.management.models.Discount;
import com.spring3.hotel.management.repositories.DiscountRepository;
import com.spring3.hotel.management.services.DiscountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.random.RandomGenerator;

@Service
public class DiscountServiceImpl implements DiscountService {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 10;

    @Autowired
    private DiscountRepository discountRepository;

    // Tạo mã giảm giá duy nhất
    public String generateUniqueDiscountCode() {
        String code;
        do {
            code = generateRandomCode();
        } while (discountRepository.existsByCode(code)); // Kiểm tra trùng lặp
        return code;
    }

    // Tạo mã ngẫu nhiên sử dụng RandomGenerator
    private String generateRandomCode() {
        RandomGenerator random = RandomGenerator.getDefault();
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            code.append(CHARACTERS.charAt(randomIndex));
        }
        return code.toString();
    }

    @Override
    public Discount getDiscountById(Integer id) {
        return discountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Discount not found"));
    }

    @Override
    public List<Discount> getAllDiscounts() {
        return discountRepository.findAll();
    }

    @Override
    public Discount createDiscount(UpsertDiscountRequest request) {
        Discount discountCreate = new Discount();
        String code;
        do {
            code = generateRandomCode();
        } while (discountRepository.existsByCode(code)); // Kiểm tra trùng lặp
        discountCreate.setCode(code);
        discountCreate.setDiscountValue(request.getDiscountValue());
        discountCreate.setDiscountType(request.getDiscountType());
        discountCreate.setValidFrom(LocalDate.parse(request.getValidFrom()));
        discountCreate.setValidTo(LocalDate.parse(request.getValidTo()));
        discountCreate.setMaxUses(request.getMaxUsage());
        discountCreate.setUsedCount(0);
        return discountRepository.save(discountCreate);
    }

    @Override
    public Discount updateDiscount(UpsertDiscountRequest discount, Integer id) {
        Discount discountUpdate = discountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Discount not found"));
        discountUpdate.setDiscountValue(discount.getDiscountValue());
        discountUpdate.setDiscountType(discount.getDiscountType());
        discountUpdate.setValidFrom(LocalDate.parse(discount.getValidFrom()));
        discountUpdate.setValidTo(LocalDate.parse(discount.getValidTo()));
        discountUpdate.setMaxUses(discount.getMaxUsage());
        return discountRepository.save(discountUpdate);
    }

    @Override
    public Discount deleteDiscount(Integer id) {
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Discount not found"));
        discountRepository.delete(discount);
        return discount;
    }
}
