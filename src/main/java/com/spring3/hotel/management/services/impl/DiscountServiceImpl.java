package com.spring3.hotel.management.services.impl;

import com.spring3.hotel.management.dto.response.DiscountDTO;
import com.spring3.hotel.management.dto.request.GenerateDiscountRequest;
import com.spring3.hotel.management.dto.request.UpsertDiscountRequest;
import com.spring3.hotel.management.exceptions.DiscountExpiredException;
import com.spring3.hotel.management.exceptions.DiscountNotFoundException;
import com.spring3.hotel.management.exceptions.DiscountUsageExceededException;
import com.spring3.hotel.management.models.Discount;
import com.spring3.hotel.management.repositories.DiscountRepository;
import com.spring3.hotel.management.services.DiscountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class DiscountServiceImpl implements DiscountService {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 8;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private DiscountRepository discountRepository;

    @Override
    public List<Discount> getAllDiscounts() {
        return discountRepository.findAll().stream()
                // .filter(Discount::isActive) // Commenting out: Missing isActive method or field
                .collect(Collectors.toList());
    }

    @Override
    public Discount getDiscountById(Integer id) {
        Optional<Discount> discount = discountRepository.findById(id);
        if (discount.isEmpty()) {
            throw new DiscountNotFoundException(id);
        }
        return discount.get();
    }

    @Override
    public Discount getDiscountByCode(String code) {
        Optional<Discount> discount = discountRepository.findByCode(code);
        if (discount.isEmpty()) {
            throw new DiscountNotFoundException(code, "code");
        }
        return discount.get();
    }

    @Override
    public Discount createDiscount(UpsertDiscountRequest request) {
        String code = generateUniqueCode(8);
        
        Discount discount = new Discount();
        discount.setCode(code);
        discount.setDiscountType(request.getDiscountType());
        discount.setDiscountValue(request.getDiscountValue());
        discount.setValidFrom(LocalDate.parse(request.getValidFrom(), DATE_FORMATTER));
        discount.setValidTo(LocalDate.parse(request.getValidTo(), DATE_FORMATTER));
        discount.setMaxUses(request.getMaxUsage());
        discount.setUsedCount(0);
        
        return discountRepository.save(discount);
    }

    @Override
    public Discount updateDiscount(UpsertDiscountRequest request, Integer id) {
        Optional<Discount> existingDiscountOpt = discountRepository.findById(id);
        
        if (existingDiscountOpt.isEmpty()) {
            throw new DiscountNotFoundException(id);
        }
        
        Discount existingDiscount = existingDiscountOpt.get();
        
        // Cập nhật thông tin
        existingDiscount.setDiscountType(request.getDiscountType());
        existingDiscount.setDiscountValue(request.getDiscountValue());
        existingDiscount.setValidFrom(LocalDate.parse(request.getValidFrom(), DATE_FORMATTER));
        existingDiscount.setValidTo(LocalDate.parse(request.getValidTo(), DATE_FORMATTER));
        existingDiscount.setMaxUses(request.getMaxUsage());
        
        return discountRepository.save(existingDiscount);
    }

    @Override
    public Discount deleteDiscount(Integer id) {
        Optional<Discount> discountOpt = discountRepository.findById(id);
        if (discountOpt.isEmpty()) {
            throw new DiscountNotFoundException(id);
        }
        Discount discount = discountOpt.get();
        return discountRepository.save(discount);
    }

    @Override
    public boolean isDiscountValid(String discountCode) {
        Optional<Discount> discountOpt = discountRepository.findByCode(discountCode);
        
        if (discountOpt.isEmpty()) {
            return false;
        }
        
        Discount discount = discountOpt.get();
        LocalDate today = LocalDate.now();
        
        // Kiểm tra ngày hết hạn
        if (today.isBefore(discount.getValidFrom()) || today.isAfter(discount.getValidTo())) {
            return false;
        }
        
        // Kiểm tra số lần sử dụng
        if (discount.getUsedCount() >= discount.getMaxUses()) {
            return false;
        }
        
        return true;
    }

    @Override
    public double applyDiscount(String discountCode, double amount) {
        Optional<Discount> discountOpt = discountRepository.findByCode(discountCode);
        
        if (discountOpt.isEmpty()) {
            throw new DiscountNotFoundException(discountCode, "code");
        }
        
        Discount discount = discountOpt.get();
        LocalDate today = LocalDate.now();
        
        // Kiểm tra ngày hết hạn
        if (today.isBefore(discount.getValidFrom()) || today.isAfter(discount.getValidTo())) {
            throw new DiscountExpiredException("Mã giảm giá '" + discountCode + "' đã hết hạn sử dụng");
        }
        
        // Kiểm tra số lần sử dụng
        if (discount.getUsedCount() >= discount.getMaxUses()) {
            throw new DiscountUsageExceededException("Mã giảm giá '" + discountCode + "' đã đạt giới hạn số lần sử dụng");
        }
        
        // Áp dụng giảm giá
        if ("PERCENT".equals(discount.getDiscountType())) {
            return amount * (1 - discount.getDiscountValue() / 100.0);
        } else if ("FIXED".equals(discount.getDiscountType())) {
            return Math.max(0, amount - discount.getDiscountValue());
        }
        
        return amount;
    }

    @Override
    public void incrementUsedCount(String discountCode) {
        Optional<Discount> discountOpt = discountRepository.findByCode(discountCode);
        
        if (discountOpt.isEmpty()) {
            throw new DiscountNotFoundException(discountCode, "code");
        }
        
        Discount discount = discountOpt.get();
        discount.setUsedCount(discount.getUsedCount() + 1);
        discountRepository.save(discount);
    }

    @Override
    public List<Discount> getActiveDiscounts() {
        LocalDate today = LocalDate.now();
        return discountRepository.findActiveDiscounts(today).stream()
                .collect(Collectors.toList());
    }

    @Override
    public List<DiscountDTO> generateRandomDiscounts(GenerateDiscountRequest request) {
        List<DiscountDTO> generatedDiscounts = new ArrayList<>();
        Random random = new Random();
        
        for (int i = 0; i < request.getCount(); i++) {
            String code;
            do {
                // Tạo mã ngẫu nhiên
                StringBuilder codeBuilder = new StringBuilder();
                if (request.getPrefix() != null && !request.getPrefix().isEmpty()) {
                    codeBuilder.append(request.getPrefix());
                    if (!request.getPrefix().endsWith("-")) {
                        codeBuilder.append("-");
                    }
                }
                
                // Thêm phần ngẫu nhiên
                for (int j = 0; j < CODE_LENGTH; j++) {
                    int randomIndex = random.nextInt(CHARACTERS.length());
                    codeBuilder.append(CHARACTERS.charAt(randomIndex));
                }
                
                code = codeBuilder.toString();
            } while (discountRepository.existsByCode(code));
            
            // Tạo DTO mới từ request
            DiscountDTO discountDTO = new DiscountDTO();
            discountDTO.setCode(code);
            discountDTO.setDiscountType(request.getDiscountType());
            discountDTO.setDiscountValue(request.getDiscountValue());
            discountDTO.setValidFrom(request.getValidFrom());
            discountDTO.setValidTo(request.getValidTo());
            discountDTO.setMaxUses(request.getMaxUses());
            discountDTO.setUsedCount(0);
            
            // Lưu vào cơ sở dữ liệu
            Discount discount = convertToEntity(discountDTO);
            discount = discountRepository.save(discount);
            
            // Thêm vào danh sách kết quả
            generatedDiscounts.add(convertToDTO(discount));
        }
        
        return generatedDiscounts;
    }
    
    /**
     * Tạo mã giảm giá ngẫu nhiên và duy nhất
     */
    private String generateUniqueCode(int length) {
        Random random = new Random();
        String code;
        
        do {
            StringBuilder codeBuilder = new StringBuilder();
            for (int i = 0; i < length; i++) {
                int randomIndex = random.nextInt(CHARACTERS.length());
                codeBuilder.append(CHARACTERS.charAt(randomIndex));
            }
            code = codeBuilder.toString();
        } while (discountRepository.existsByCode(code));
        
        return code;
    }
    
    /**
     * Chuyển đổi từ Entity sang DTO
     */
    private DiscountDTO convertToDTO(Discount discount) {
        DiscountDTO dto = new DiscountDTO();
        dto.setId(discount.getId());
        dto.setCode(discount.getCode());
        dto.setDiscountType(discount.getDiscountType());
        dto.setDiscountValue(discount.getDiscountValue());
        dto.setValidFrom(discount.getValidFrom());
        dto.setValidTo(discount.getValidTo());
        dto.setMaxUses(discount.getMaxUses());
        dto.setUsedCount(discount.getUsedCount());
        
        // Kiểm tra tính hợp lệ
        LocalDate today = LocalDate.now();
        dto.setValid(today.isAfter(discount.getValidFrom().minusDays(1)) &&
                    today.isBefore(discount.getValidTo().plusDays(1)) &&
                    discount.getUsedCount() < discount.getMaxUses());
        
        return dto;
    }
    
    /**
     * Chuyển đổi từ DTO sang Entity
     */
    private Discount convertToEntity(DiscountDTO dto) {
        Discount discount = new Discount();
        discount.setId(dto.getId());
        discount.setCode(dto.getCode());
        discount.setDiscountType(dto.getDiscountType());
        discount.setDiscountValue(dto.getDiscountValue());
        discount.setValidFrom(dto.getValidFrom());
        discount.setValidTo(dto.getValidTo());
        discount.setMaxUses(dto.getMaxUses());
        discount.setUsedCount(dto.getUsedCount());
        return discount;
    }
}
