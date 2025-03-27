package com.spring3.hotel.management.services;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spring3.hotel.management.models.RefreshToken;
import com.spring3.hotel.management.repositories.RefreshTokenRepository;
import com.spring3.hotel.management.repositories.UserRepository;

import jakarta.transaction.Transactional;


@Service
public class RefreshTokenService {

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @Autowired
    UserRepository userRepository;

    public RefreshToken createRefreshToken(String username){
        RefreshToken refreshToken = RefreshToken.builder()
                .user(userRepository.findByUsername(username))
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(7 * 24 * 60 * 60 * 1000)) // 7 ngày
                .build();
        return refreshTokenRepository.save(refreshToken);
    }



    public Optional<RefreshToken> findByToken(String token){
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token){
        if(token.getExpiryDate().compareTo(Instant.now())<0){
            refreshTokenRepository.delete(token);
            throw new RuntimeException(token.getToken() + " Refresh token is expired. Please make a new login..!");
        }
        return token;

    }

    @Transactional
    public void deleteByUsername(String username) {
        refreshTokenRepository.deleteByUser(userRepository.findByUsername(username));
    }

    public Optional<RefreshToken> findByUsername(String username) {
        return refreshTokenRepository.findByUser_Username(username);
    }
}
