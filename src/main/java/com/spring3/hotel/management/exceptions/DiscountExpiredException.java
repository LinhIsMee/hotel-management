package com.spring3.hotel.management.exceptions;

public class DiscountExpiredException extends RuntimeException {
    public DiscountExpiredException(String message) {
        super(message);
    }
}