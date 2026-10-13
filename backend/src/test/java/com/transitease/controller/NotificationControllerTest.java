package com.transitease.controller;

import com.transitease.dto.NotificationResponse;
import com.transitease.entity.Role;
import com.transitease.entity.User;
import com.transitease.repository.UserRepository;
import com.transitease.security.JwtService;
import com.transitease.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private UserRepository userRepository;

    private String passengerToken;
    private NotificationResponse sampleNotification;

    @BeforeEach
    void setUp() {
        User passenger = new User();
        passenger.setId(10L);
        passenger.setFullName("Alice Smith");
        passenger.setEmail("alice@example.com");
        passenger.setRole(Role.PASSENGER);

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(passenger));
        passengerToken = jwtService.generateToken(passenger);

        sampleNotification = new NotificationResponse();
        sampleNotification.setId(100L);
        sampleNotification.setRecipientId(10L);
        sampleNotification.setRecipientEmail("alice@example.com");
        sampleNotification.setRecipientName("Alice Smith");
        sampleNotification.setTitle("Pass Application Verified");
        sampleNotification.setMessage("Your pass application #101 has been verified.");
        sampleNotification.setType("APPLICATION_STATUS");
        sampleNotification.setEventType("APPLICATION_VERIFIED");
        sampleNotification.setIsRead(false);
        sampleNotification.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testGetMyNotifications_Success() throws Exception {
        when(notificationService.getMyNotifications(null)).thenReturn(List.of(sampleNotification));

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + passengerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].title").value("Pass Application Verified"))
                .andExpect(jsonPath("$[0].isRead").value(false));
    }

    @Test
    void testGetMyNotifications_UnreadOnly_Success() throws Exception {
        when(notificationService.getMyNotifications(true)).thenReturn(List.of(sampleNotification));

        mockMvc.perform(get("/api/notifications")
                        .param("unreadOnly", "true")
                        .header("Authorization", "Bearer " + passengerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100));
    }

    @Test
    void testGetUnreadCount_Success() throws Exception {
        when(notificationService.getUnreadCount()).thenReturn(3L);

        mockMvc.perform(get("/api/notifications/unread-count")
                        .header("Authorization", "Bearer " + passengerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(3))
                .andExpect(jsonPath("$.count").value(3));
    }

    @Test
    void testGetNotificationById_Success() throws Exception {
        when(notificationService.getNotificationById(100L)).thenReturn(sampleNotification);

        mockMvc.perform(get("/api/notifications/100")
                        .header("Authorization", "Bearer " + passengerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.title").value("Pass Application Verified"));
    }

    @Test
    void testGetNotificationById_AnotherPassenger_Returns403() throws Exception {
        when(notificationService.getNotificationById(999L))
                .thenThrow(new AccessDeniedException("You do not have permission to access this notification"));

        mockMvc.perform(get("/api/notifications/999")
                        .header("Authorization", "Bearer " + passengerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("You do not have permission to access this notification"));
    }

    @Test
    void testMarkAsRead_Success() throws Exception {
        sampleNotification.setIsRead(true);
        when(notificationService.markAsRead(100L)).thenReturn(sampleNotification);

        mockMvc.perform(put("/api/notifications/100/read")
                        .header("Authorization", "Bearer " + passengerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.isRead").value(true));
    }

    @Test
    void testMarkAsRead_AnotherPassenger_Returns403() throws Exception {
        when(notificationService.markAsRead(999L))
                .thenThrow(new AccessDeniedException("You do not have permission to access this notification"));

        mockMvc.perform(put("/api/notifications/999/read")
                        .header("Authorization", "Bearer " + passengerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void testMarkAllAsRead_Success() throws Exception {
        mockMvc.perform(put("/api/notifications/read-all")
                        .header("Authorization", "Bearer " + passengerToken))
                .andExpect(status().isOk());
    }

    @Test
    void testGetNotifications_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isUnauthorized());
    }
}
