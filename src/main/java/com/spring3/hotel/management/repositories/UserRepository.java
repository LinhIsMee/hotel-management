package com.spring3.hotel.management.repositories;

import com.spring3.hotel.management.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

   public User findByUsername(String username);
   User findFirstById(Integer id);

}
