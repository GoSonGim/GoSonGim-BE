package com.example.GoSonGim_BE.domain.users.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.GoSonGim_BE.domain.users.entity.User;
import com.example.GoSonGim_BE.domain.users.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    
    @Override
    public User createDefaultUser() {
        User user = User.createDefault();
        return userRepository.save(user);
    }
}
