package com.spring3.hotel.management.services.impl;

import com.spring3.hotel.management.dto.DiscountDTO;
import com.spring3.hotel.management.dto.GenerateDiscountRequest;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class DiscountServiceImpl implements DiscountService {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 8;

    @Autowired
    private DiscountRepository discountRepository;

    @Override
    public List<DiscountDTO> getAllDiscounts() {
        return discountRepository.findAll().stream()
                .filter(Discount::isActive)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DiscountDTO getDiscountById(Integer id) {
        Optional<Discount> discount = discountRepository.findById(id);
        if (discount.isEmpty() || !discount.get().isActive()) {
            throw new DiscountNotFoundException(id);
        }
        return convertToDTO(discount.get());
    }

    @Override
    public DiscountDTO getDiscountByCode(String code) {
        Optional<Discount> discount = discountRepository.findByCode(code);
        if (discount.isEmpty() || !discount.get().isActive()) {
            throw new DiscountNotFoundException(code, "code");
        }
        return convertToDTO(discount.get());
    }

    @Override
    public DiscountDTO createDiscount(DiscountDTO discountDTO) {
        if (discountRepository.existsByCode(discountDTO.getCode())) {
            throw new IllegalArgumentException("Mã giảm giá '" + discountDTO.getCode() + "' đã tồn tại");
        }
        
        Discount discount = convertToEntity(discountDTO);
        discount = discountRepository.save(discount);
        return convertToDTO(discount);
    }

    @Override
    public DiscountDTO updateDiscount(Integer id, DiscountDTO discountDTO) {
        Optional<Discount> existingDiscountOpt = discountRepository.findById(id);
        
        if (existingDiscountOpt.isEmpty()) {
            throw new DiscountNotFoundException(id);
        }
        
        Discount existingDiscount = existingDiscountOpt.get();
        
        // Kiểm tra nếu code bị thay đổi và code mới đã tồn tại
        if (!existingDiscount.getCode().equals(discountDTO.getCode()) && 
            discountRepository.existsByCode(discountDTO.getCode())) {
            throw new IllegalArgumentException("Mã giảm giá '" + discountDTO.getCode() + "' đã tồn tại");
        }
        
        // Cập nhật thông tin
        existingDiscount.setCode(discountDTO.getCode());
        existingDiscount.setDiscountType(discountDTO.getDiscountType());
        existingDiscount.setDiscountValue(discountDTO.getDiscountValue());
        existingDiscount.setValidFrom(discountDTO.getValidFrom());
        existingDiscount.setValidTo(discountDTO.getValidTo());
        existingDiscount.setMaxUses(discountDTO.getMaxUses());
        existingDiscount.setUsedCount(discountDTO.getUsedCount());
        
        existingDiscount = discountRepository.save(existingDiscount);
        return convertToDTO(existingDiscount);
    }

    @Override
    public void deleteDiscount(Integer id) {
        Optional<Discount> discountOpt = discountRepository.findById(id);
        if (discountOpt.isEmpty()) {
            throw new DiscountNotFoundException(id);
        }
        Discount discount = discountOpt.get();
        discount.setActive(false);
        discountRepository.save(discount);
    }

    @Override
    public boolean isDiscountValid(String discountCode) {
        Optional<Discount> discountOpt = discountRepository.findByCode(discountCode);
        
        if (discountOpt.isEmpty() || !discountOpt.get().isActive()) {
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
        
        if (discountOpt.isEmpty() || !discountOpt.get().isActive()) {
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
            return amount * (1 - discount.getDiscountValue());
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
    public List<DiscountDTO> getActiveDiscounts() {
        LocalDate today = LocalDate.now();
        return discountRepository.findActiveDiscounts(today).stream()
                .filter(Discount::isActive)
                .map(this::convertToDTO)
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
    
    // Phương thức chuyển đổi từ Entity sang DTO
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
    
    // Phương thức chuyển đổi từ DTO sang Entity
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
