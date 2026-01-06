package com.pollinate.task.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pollinate.task.model.AuthRequest;
import com.pollinate.task.service.AuthService;
import com.pollinate.task.configuration.AppConfigurationProperties;
import com.pollinate.task.security.JwtUtil;
import com.pollinate.task.service.CustomUserDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
@Slf4j
public class AuthControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private PasswordEncoder encoder;

    @MockitoBean
    private JwtUtil jwtUtils;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private AppConfigurationProperties properties;

    @Autowired
    private ObjectMapper objectMapper;

    private final String username = "alice";
    private final String password = "secret";
    private final String authJson = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);

    @Test
    public void registerUser_success() throws Exception {
        when(authService.existsByUsername(username)).thenReturn(false);

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(authJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.response").value("User registered successfully!"));

        ArgumentCaptor<AuthRequest> captor = ArgumentCaptor.forClass(AuthRequest.class);
        verify(authService, times(1)).saveUser(captor.capture());
        AuthRequest sent = captor.getValue();
        assert username.equals(sent.getUsername());
        assert password.equals(sent.getPassword());

        verify(authService, times(1)).existsByUsername(username);
    }

    @Test
    public void registerUser_conflict_usernameTaken() throws Exception {
        // Given: username exists
        when(authService.existsByUsername(username)).thenReturn(true);

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(authJson))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.response").value(containsString("already taken")));

        // Then: saveUser NOT called
        verify(authService, never()).saveUser(any(AuthRequest.class));
        verify(authService, times(1)).existsByUsername(username);
    }

    @Test
    public void login_success_setsCookieAndReturns200() throws Exception {
        // Given authentication succeeds
        var userDetails = new User(username, "encoded", Collections.emptyList());
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(auth);

        // Given JWT creation and cookie config
        String token = "jwt-token";
        when(jwtUtils.generateToken(username)).thenReturn(token);
        when(properties.getName()).thenReturn("AUTH");
        when(properties.getExpiration()).thenReturn(3600);

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(authJson))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.response").value("Successfully authenticated user"))
                // Verify Set-Cookie header contains expected attributes
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("AUTH=" + token)))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=3600")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Path=/")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("SameSite=Lax")));

        // Interactions
        verify(authenticationManager, times(1)).authenticate(any(Authentication.class));
        verify(jwtUtils, times(1)).generateToken(username);
        verify(properties, times(1)).getName();
        verify(properties, times(1)).getExpiration();
    }

    @Test
    public void login_failure_unauthorized() throws Exception {
        // Given: AuthenticationManager throws AuthenticationException
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new org.springframework.security.core.AuthenticationException("Bad credentials") {});

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(authJson))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.response").value(containsString("Authentication failed")));

        // Should NOT set cookie
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(authJson))
                .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));

        verify(authenticationManager, atLeastOnce()).authenticate(any(Authentication.class));
        verify(jwtUtils, never()).generateToken(anyString());
    }

    @Test
    public void login_internalError_returns500() throws Exception {
        // Given: Some unexpected runtime exception inside controller path
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new RuntimeException("Boom"));

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(authJson))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.response").value(containsString("Internal error")));

        // Should NOT set cookie
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(authJson))
                .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));

        verify(jwtUtils, never()).generateToken(anyString());
    }

    @Test
    public void registerUser_invalidPayload_returns400() throws Exception {
        String invalidJson = "{}";

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(authService, never()).saveUser(any(AuthRequest.class));
        verify(authService, never()).existsByUsername(anyString());
    }
}
