package com.spring3.hotel.management.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.spring3.hotel.management.models.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

   public User findByUsername(String username);
   User findFirstById(Integer id);
   boolean existsByUsername(String username);
   boolean existsByEmail(String email);
   Optional<User> findByEmail(String email);

}
