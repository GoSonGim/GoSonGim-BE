package com.example.GoSonGim_BE.domain.users.repository;

import com.example.GoSonGim_BE.domain.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
