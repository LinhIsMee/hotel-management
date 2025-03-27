package com.spring3.hotel.management.repositories;

import com.spring3.hotel.management.models.RefreshToken;
import com.spring3.hotel.management.models.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Integer> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User byUsername);

    Optional<RefreshToken> findByUser_Username(String username);
}
