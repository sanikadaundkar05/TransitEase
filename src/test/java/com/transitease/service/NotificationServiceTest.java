package com.transitease.service;

import com.transitease.dto.NotificationResponse;
import com.transitease.entity.*;
import com.transitease.exception.ResourceNotFoundException;
import com.transitease.repository.NotificationRepository;
import com.transitease.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationService notificationService;

    private com.transitease.entity.User passenger1;
    private com.transitease.entity.User passenger2;
    private PassCategory passCategory;
    private PassApplication application;
    private DigitalPass digitalPass;

    @BeforeEach
    void setUp() {
        passenger1 = new com.transitease.entity.User();
        passenger1.setId(10L);
        passenger1.setEmail("passenger1@example.com");
        passenger1.setFullName("Passenger One");
        passenger1.setRole(Role.PASSENGER);

        passenger2 = new com.transitease.entity.User();
        passenger2.setId(20L);
        passenger2.setEmail("passenger2@example.com");
        passenger2.setFullName("Passenger Two");
        passenger2.setRole(Role.PASSENGER);

        passCategory = new PassCategory();
        passCategory.setId(1L);
        passCategory.setName("Student Pass");

        application = new PassApplication();
        application.setId(101L);
        application.setPassenger(passenger1);
        application.setPassCategory(passCategory);
        application.setStatus(ApplicationStatus.PENDING);

        digitalPass = new DigitalPass();
        digitalPass.setId(201L);
        digitalPass.setPassNumber("TE-PASS-20261013-11223344");
        digitalPass.setPassenger(passenger1);
        digitalPass.setApplication(application);
        digitalPass.setValidityEndDate(LocalDate.now().plusDays(30));
    }

    private void authenticateAs(com.transitease.entity.User user) {
        UserDetails userDetails = new User(
                user.getEmail(),
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        lenient().when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    }

    @Test
    void testCreateApplicationStatusNotification_Verified_WithRemarks() {
        when(notificationRepository.existsByRecipientAndRelatedEntityTypeAndRelatedEntityIdAndEventType(
                eq(passenger1), eq("APPLICATION"), eq(101L), eq("APPLICATION_VERIFIED")))
                .thenReturn(false);

        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> {
            Notification n = i.getArgument(0);
            n.setId(1L);
            n.setCreatedAt(LocalDateTime.now());
            return n;
        });

        Notification result = notificationService.createApplicationStatusNotification(
                application, ApplicationStatus.VERIFIED, "Student ID verified"
        );

        assertNotNull(result);
        assertEquals(passenger1, result.getRecipient());
        assertEquals("Pass Application Verified", result.getTitle());
        assertTrue(result.getMessage().contains("Student ID verified"));
        assertTrue(result.getMessage().contains("#101"));
        assertEquals("APPLICATION_STATUS", result.getType());
        assertEquals("APPLICATION_VERIFIED", result.getEventType());
        assertFalse(result.getIsRead());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testCreateApplicationStatusNotification_Rejected_WithRemarks() {
        when(notificationRepository.existsByRecipientAndRelatedEntityTypeAndRelatedEntityIdAndEventType(
                eq(passenger1), eq("APPLICATION"), eq(101L), eq("APPLICATION_REJECTED")))
                .thenReturn(false);

        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

        Notification result = notificationService.createApplicationStatusNotification(
                application, ApplicationStatus.REJECTED, "Invalid document"
        );

        assertNotNull(result);
        assertEquals("Pass Application Rejected", result.getTitle());
        assertTrue(result.getMessage().contains("Reason: Invalid document"));
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testCreateApplicationStatusNotification_DuplicatePrevented() {
        when(notificationRepository.existsByRecipientAndRelatedEntityTypeAndRelatedEntityIdAndEventType(
                eq(passenger1), eq("APPLICATION"), eq(101L), eq("APPLICATION_VERIFIED")))
                .thenReturn(true);

        Notification result = notificationService.createApplicationStatusNotification(
                application, ApplicationStatus.VERIFIED, "Student ID verified"
        );

        assertNull(result);
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void testCreatePassIssuedNotification_Success() {
        when(notificationRepository.existsByRecipientAndRelatedEntityTypeAndRelatedEntityIdAndEventType(
                eq(passenger1), eq("DIGITAL_PASS"), eq(201L), eq("PASS_ISSUED")))
                .thenReturn(false);

        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> {
            Notification n = i.getArgument(0);
            n.setId(2L);
            n.setCreatedAt(LocalDateTime.now());
            return n;
        });

        Notification result = notificationService.createPassIssuedNotification(digitalPass);

        assertNotNull(result);
        assertEquals("Digital Pass Issued", result.getTitle());
        assertTrue(result.getMessage().contains("TE-PASS-20261013-11223344"));
        assertEquals("PASS_ISSUED", result.getType());
        assertFalse(result.getIsRead());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testCreatePassIssuedNotification_DuplicatePrevented() {
        when(notificationRepository.existsByRecipientAndRelatedEntityTypeAndRelatedEntityIdAndEventType(
                eq(passenger1), eq("DIGITAL_PASS"), eq(201L), eq("PASS_ISSUED")))
                .thenReturn(true);

        Notification result = notificationService.createPassIssuedNotification(digitalPass);

        assertNull(result);
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void testGetMyNotifications_All() {
        authenticateAs(passenger1);

        Notification n = new Notification();
        n.setId(10L);
        n.setRecipient(passenger1);
        n.setTitle("Title");
        n.setMessage("Message");
        n.setIsRead(false);
        n.setCreatedAt(LocalDateTime.now());

        when(notificationRepository.findByRecipientOrderByCreatedAtDesc(passenger1))
                .thenReturn(List.of(n));

        List<NotificationResponse> responses = notificationService.getMyNotifications(false);

        assertEquals(1, responses.size());
        assertEquals("Title", responses.get(0).getTitle());
        verify(notificationRepository, times(1)).findByRecipientOrderByCreatedAtDesc(passenger1);
    }

    @Test
    void testGetMyNotifications_UnreadOnly() {
        authenticateAs(passenger1);

        Notification n = new Notification();
        n.setId(10L);
        n.setRecipient(passenger1);
        n.setTitle("Title Unread");
        n.setIsRead(false);
        n.setCreatedAt(LocalDateTime.now());

        when(notificationRepository.findByRecipientAndIsReadOrderByCreatedAtDesc(passenger1, false))
                .thenReturn(List.of(n));

        List<NotificationResponse> responses = notificationService.getMyNotifications(true);

        assertEquals(1, responses.size());
        assertEquals("Title Unread", responses.get(0).getTitle());
        verify(notificationRepository, times(1)).findByRecipientAndIsReadOrderByCreatedAtDesc(passenger1, false);
    }

    @Test
    void testGetUnreadCount() {
        authenticateAs(passenger1);

        when(notificationRepository.countByRecipientAndIsRead(passenger1, false)).thenReturn(5L);

        long count = notificationService.getUnreadCount();

        assertEquals(5L, count);
        verify(notificationRepository, times(1)).countByRecipientAndIsRead(passenger1, false);
    }

    @Test
    void testMarkAsRead_Success() {
        authenticateAs(passenger1);

        Notification n = new Notification();
        n.setId(15L);
        n.setRecipient(passenger1);
        n.setIsRead(false);

        when(notificationRepository.findById(15L)).thenReturn(Optional.of(n));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

        NotificationResponse response = notificationService.markAsRead(15L);

        assertNotNull(response);
        assertTrue(response.getIsRead());
        verify(notificationRepository, times(1)).save(n);
    }

    @Test
    void testMarkAsRead_AnotherPassenger_ThrowsAccessDeniedException() {
        authenticateAs(passenger1);

        Notification n = new Notification();
        n.setId(15L);
        n.setRecipient(passenger2); // Belongs to passenger 2
        n.setIsRead(false);

        when(notificationRepository.findById(15L)).thenReturn(Optional.of(n));

        assertThrows(AccessDeniedException.class, () -> notificationService.markAsRead(15L));
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void testMarkAsRead_NotFound_ThrowsResourceNotFoundException() {
        authenticateAs(passenger1);

        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> notificationService.markAsRead(999L));
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void testGetNotificationById_AnotherPassenger_ThrowsAccessDeniedException() {
        authenticateAs(passenger1);

        Notification n = new Notification();
        n.setId(15L);
        n.setRecipient(passenger2);

        when(notificationRepository.findById(15L)).thenReturn(Optional.of(n));

        assertThrows(AccessDeniedException.class, () -> notificationService.getNotificationById(15L));
    }

    @Test
    void testMarkAllAsRead_Success() {
        authenticateAs(passenger1);

        Notification n1 = new Notification();
        n1.setId(1L);
        n1.setIsRead(false);
        Notification n2 = new Notification();
        n2.setId(2L);
        n2.setIsRead(false);

        when(notificationRepository.findByRecipientAndIsReadOrderByCreatedAtDesc(passenger1, false))
                .thenReturn(List.of(n1, n2));

        notificationService.markAllAsRead();

        assertTrue(n1.getIsRead());
        assertTrue(n2.getIsRead());
        verify(notificationRepository, times(1)).saveAll(List.of(n1, n2));
    }
}
