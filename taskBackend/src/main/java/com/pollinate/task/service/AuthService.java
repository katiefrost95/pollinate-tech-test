package com.pollinate.task.service;

import com.pollinate.task.model.AuthRequest;
import com.pollinate.task.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AuthRequest saveUser(AuthRequest request) {
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        AuthRequest updatedRequest = AuthRequest.builder().username(request.getUsername()).password(encodedPassword).build();
        return userRepository.save(updatedRequest);
    }

    public Boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
}
