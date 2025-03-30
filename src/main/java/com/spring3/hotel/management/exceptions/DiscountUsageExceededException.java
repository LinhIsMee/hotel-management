package com.spring3.hotel.management.exceptions;

public class DiscountUsageExceededException extends RuntimeException {
    public DiscountUsageExceededException(String message) {
        super(message);
    }
} 