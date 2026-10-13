package com.transitease.service;

import com.transitease.dto.DigitalPassResponse;
import com.transitease.entity.*;
import com.transitease.repository.DigitalPassRepository;
import com.transitease.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
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
class DigitalPassServiceTest {

    @Mock
    private DigitalPassRepository digitalPassRepository;

    @Mock
    private UserRepository userRepository;

    @Spy
    private PdfGeneratorService pdfGeneratorService = new PdfGeneratorService();

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private DigitalPassService digitalPassService;

    private com.transitease.entity.User passenger1;
    private com.transitease.entity.User passenger2;
    private com.transitease.entity.User adminUser;
    private PassCategory category;
    private PassApplication application1;
    private DigitalPass pass1;

    @BeforeEach
    void setUp() {
        passenger1 = new com.transitease.entity.User();
        passenger1.setId(1L);
        passenger1.setFullName("Alice Smith");
        passenger1.setEmail("alice@example.com");
        passenger1.setRole(Role.PASSENGER);

        passenger2 = new com.transitease.entity.User();
        passenger2.setId(2L);
        passenger2.setFullName("Bob Jones");
        passenger2.setEmail("bob@example.com");
        passenger2.setRole(Role.PASSENGER);

        adminUser = new com.transitease.entity.User();
        adminUser.setId(99L);
        adminUser.setFullName("Admin User");
        adminUser.setEmail("admin@transitease.com");
        adminUser.setRole(Role.ADMIN);

        category = new PassCategory();
        category.setId(10L);
        category.setName("Monthly Pass");
        category.setValidityDays(30);
        category.setBaseFare(25.0);

        application1 = new PassApplication();
        application1.setId(100L);
        application1.setPassenger(passenger1);
        application1.setPassCategory(category);
        application1.setStatus(ApplicationStatus.APPROVED);
        application1.setApplicationDate(LocalDateTime.now());

        pass1 = new DigitalPass();
        pass1.setId(500L);
        pass1.setPassNumber("TE-PASS-20261013-ABCDEF12");
        pass1.setPassenger(passenger1);
        pass1.setApplication(application1);
        pass1.setIssueDate(LocalDate.now());
        pass1.setValidityStartDate(LocalDate.now());
        pass1.setValidityEndDate(LocalDate.now().plusDays(30));
        pass1.setStatus(PassStatus.ACTIVE);
    }

    private void authenticateAs(String email, String role) {
        UserDetails userDetails = new User(email, "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)));
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void testGeneratePassForApplication_Success() {
        when(digitalPassRepository.findByApplication(application1)).thenReturn(Optional.empty());
        when(digitalPassRepository.existsByPassNumber(any())).thenReturn(false);
        when(digitalPassRepository.save(any(DigitalPass.class))).thenAnswer(invocation -> {
            DigitalPass saved = invocation.getArgument(0);
            saved.setId(501L);
            return saved;
        });

        DigitalPass generated = digitalPassService.generatePassForApplication(application1);

        assertNotNull(generated);
        assertEquals(501L, generated.getId());
        assertNotNull(generated.getPassNumber());
        assertTrue(generated.getPassNumber().startsWith("TE-PASS-"));
        assertEquals(passenger1, generated.getPassenger());
        assertEquals(application1, generated.getApplication());
        assertEquals(LocalDate.now(), generated.getIssueDate());
        assertEquals(LocalDate.now(), generated.getValidityStartDate());
        assertEquals(LocalDate.now().plusDays(30), generated.getValidityEndDate());
        assertEquals(PassStatus.ACTIVE, generated.getStatus());

        verify(digitalPassRepository, times(1)).save(any(DigitalPass.class));
    }

    @Test
    void testGeneratePassForApplication_DuplicatePrevention_ReturnsExistingPass() {
        when(digitalPassRepository.findByApplication(application1)).thenReturn(Optional.of(pass1));

        DigitalPass result = digitalPassService.generatePassForApplication(application1);

        assertNotNull(result);
        assertEquals(pass1.getPassNumber(), result.getPassNumber());
        assertEquals(500L, result.getId());

        // Must NOT save again
        verify(digitalPassRepository, never()).save(any(DigitalPass.class));
    }

    @Test
    void testGetMyPasses_ReturnsPassengerPasses() {
        authenticateAs("alice@example.com", "PASSENGER");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(passenger1));
        when(digitalPassRepository.findByPassengerOrderByIssueDateDesc(passenger1)).thenReturn(List.of(pass1));

        List<DigitalPassResponse> responses = digitalPassService.getMyPasses();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("TE-PASS-20261013-ABCDEF12", responses.get(0).getPassNumber());
        assertEquals("Alice Smith", responses.get(0).getPassengerName());
    }

    @Test
    void testGetPassById_OwnerPassenger_AccessAllowed() {
        authenticateAs("alice@example.com", "PASSENGER");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(passenger1));
        when(digitalPassRepository.findById(500L)).thenReturn(Optional.of(pass1));

        DigitalPassResponse response = digitalPassService.getPassById(500L);

        assertNotNull(response);
        assertEquals(500L, response.getId());
        assertEquals("TE-PASS-20261013-ABCDEF12", response.getPassNumber());
    }

    @Test
    void testGetPassById_DifferentPassenger_AccessDeniedThrows403() {
        authenticateAs("bob@example.com", "PASSENGER");
        when(userRepository.findByEmail("bob@example.com")).thenReturn(Optional.of(passenger2));
        when(digitalPassRepository.findById(500L)).thenReturn(Optional.of(pass1)); // pass1 belongs to alice

        assertThrows(AccessDeniedException.class, () -> digitalPassService.getPassById(500L));
    }

    @Test
    void testGetPassById_Admin_AccessAllowedForAnyPass() {
        authenticateAs("admin@transitease.com", "ADMIN");
        when(userRepository.findByEmail("admin@transitease.com")).thenReturn(Optional.of(adminUser));
        when(digitalPassRepository.findById(500L)).thenReturn(Optional.of(pass1));

        DigitalPassResponse response = digitalPassService.getPassById(500L);

        assertNotNull(response);
        assertEquals(500L, response.getId());
        assertEquals("TE-PASS-20261013-ABCDEF12", response.getPassNumber());
    }

    @Test
    void testDownloadPassPdf_OwnerPassenger_GeneratesValidPdfBytes() {
        authenticateAs("alice@example.com", "PASSENGER");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(passenger1));
        when(digitalPassRepository.findById(500L)).thenReturn(Optional.of(pass1));

        byte[] pdfBytes = digitalPassService.downloadPassPdf(500L);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        String pdfHeader = new String(pdfBytes, 0, 8);
        assertEquals("%PDF-1.4", pdfHeader);
    }
}
