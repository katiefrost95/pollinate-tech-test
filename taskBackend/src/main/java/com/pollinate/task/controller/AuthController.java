package com.pollinate.task.controller;

import com.pollinate.task.configuration.AppConfigurationProperties;
import com.pollinate.task.model.AuthRequest;
import com.pollinate.task.model.AuthResponse;
import com.pollinate.task.security.JwtUtil;
import com.pollinate.task.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtil jwtUtils;

    @Autowired
    private AppConfigurationProperties properties;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody AuthRequest user) {
        System.out.println("register user");
        if (authService.existsByUsername(user.getUsername())) {
            String errorMessage = String.format("Error: Username %s is already taken!", user.getUsername());
            log.error(errorMessage);
            return ResponseEntity.status(409).body(AuthResponse.builder().response(errorMessage).build());
        }
        authService.saveUser(user);
        return ResponseEntity.status(201).body(AuthResponse.builder().response("User registered successfully!").build());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest req, HttpServletResponse response) {
        try {
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword());
            Authentication authentication = authenticationManager.authenticate(token);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwt = jwtUtils.generateToken(userDetails.getUsername());

            ResponseCookie cookie = setCookie(jwt, properties.getExpiration());
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            return ResponseEntity.ok(AuthResponse.builder().response("Successfully authenticated user").build());
        } catch (AuthenticationException e) {
            log.error("Authentication failed", e);
            return ResponseEntity.status(401).body(AuthResponse.builder().response("Authentication failed: " + e.getMessage()).build());
        } catch (Exception e) {
            log.error("Error authenticating user", e);
            return ResponseEntity.status(500).body(AuthResponse.builder().response("Internal error: " + e.getMessage()).build());
        }
    }

    private ResponseCookie setCookie(String jwt, int expiresIn) {
        // Use secure(false) for local HTTP development. Set to true in production (HTTPS).
        return ResponseCookie.from(properties.getName(), jwt)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(expiresIn)
                .sameSite("Lax")
                .build();
    }


}
