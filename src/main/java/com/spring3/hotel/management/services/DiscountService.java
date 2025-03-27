package com.spring3.hotel.management.services;

import com.spring3.hotel.management.dtos.request.UpsertDiscountRequest;
import com.spring3.hotel.management.models.Discount;

import java.util.List;

public interface DiscountService {
    Discount getDiscountById(Integer id);
    List<Discount> getAllDiscounts();
    Discount createDiscount(UpsertDiscountRequest discount);
    Discount updateDiscount(UpsertDiscountRequest discount, Integer id);
    Discount deleteDiscount(Integer id);

}
