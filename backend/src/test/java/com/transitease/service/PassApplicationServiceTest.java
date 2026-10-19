package com.transitease.service;

import com.transitease.dto.PassApplicationResponse;
import com.transitease.entity.ApplicationStatus;
import com.transitease.entity.PassApplication;
import com.transitease.entity.PassCategory;
import com.transitease.entity.Role;
import com.transitease.entity.User;
import com.transitease.exception.ResourceNotFoundException;
import com.transitease.repository.PassApplicationRepository;
import com.transitease.repository.PassCategoryRepository;
import com.transitease.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PassApplicationServiceTest {

    @Mock
    private PassApplicationRepository applicationRepository;

    @Mock
    private PassCategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DigitalPassService digitalPassService;

    @Mock
    private com.transitease.repository.DigitalPassRepository digitalPassRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PassApplicationService applicationService;

    private PassApplication pendingApplication;
    private PassApplication verifiedApplication;
    private User passenger;
    private PassCategory category;

    @BeforeEach
    void setUp() {
        passenger = new User();
        passenger.setId(10L);
        passenger.setFullName("John Passenger");
        passenger.setEmail("passenger@example.com");
        passenger.setRole(Role.PASSENGER);

        category = new PassCategory();
        category.setId(1L);
        category.setName("Student Pass");
        category.setValidityDays(30);
        category.setBaseFare(20.0);

        pendingApplication = new PassApplication();
        pendingApplication.setId(100L);
        pendingApplication.setPassenger(passenger);
        pendingApplication.setPassCategory(category);
        pendingApplication.setDocumentUrl("https://example.com/doc.pdf");
        pendingApplication.setStatus(ApplicationStatus.PENDING);
        pendingApplication.setApplicationDate(LocalDateTime.now());

        verifiedApplication = new PassApplication();
        verifiedApplication.setId(101L);
        verifiedApplication.setPassenger(passenger);
        verifiedApplication.setPassCategory(category);
        verifiedApplication.setDocumentUrl("https://example.com/doc.pdf");
        verifiedApplication.setStatus(ApplicationStatus.VERIFIED);
        verifiedApplication.setRemarks("Documents look good");
        verifiedApplication.setApplicationDate(LocalDateTime.now());
    }

    @Test
    void testVerifyApplication_Success_WithRemarks() {
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(pendingApplication));
        when(applicationRepository.save(any(PassApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PassApplicationResponse response = applicationService.updateApplicationStatus(100L, ApplicationStatus.VERIFIED, "Student ID card verified");

        assertNotNull(response);
        assertEquals("VERIFIED", response.getStatus());
        assertEquals("Student ID card verified", response.getRemarks());
        assertEquals("Student ID card verified", response.getRejectionReason());
        verify(applicationRepository, times(1)).save(pendingApplication);
    }

    @Test
    void testApproveApplication_Success_FromVerified() {
        when(applicationRepository.findById(101L)).thenReturn(Optional.of(verifiedApplication));
        when(applicationRepository.save(any(PassApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PassApplicationResponse response = applicationService.updateApplicationStatus(101L, ApplicationStatus.APPROVED, "Final approval granted");

        assertNotNull(response);
        assertEquals("APPROVED", response.getStatus());
        assertEquals("Final approval granted", response.getRemarks());
        verify(applicationRepository, times(1)).save(verifiedApplication);
        verify(digitalPassService, times(1)).generatePassForApplication(verifiedApplication);
    }

    @Test
    void testRejectApplication_FromPending_Success_WithRemarks() {
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(pendingApplication));
        when(applicationRepository.save(any(PassApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PassApplicationResponse response = applicationService.updateApplicationStatus(100L, ApplicationStatus.REJECTED, "Invalid institutional ID card");

        assertNotNull(response);
        assertEquals("REJECTED", response.getStatus());
        assertEquals("Invalid institutional ID card", response.getRemarks());
        assertEquals("Invalid institutional ID card", response.getRejectionReason());
        verify(applicationRepository, times(1)).save(pendingApplication);
    }

    @Test
    void testRejectApplication_FromVerified_Success_WithRemarks() {
        when(applicationRepository.findById(101L)).thenReturn(Optional.of(verifiedApplication));
        when(applicationRepository.save(any(PassApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PassApplicationResponse response = applicationService.updateApplicationStatus(101L, ApplicationStatus.REJECTED, "Quota exceeded for route");

        assertNotNull(response);
        assertEquals("REJECTED", response.getStatus());
        assertEquals("Quota exceeded for route", response.getRemarks());
        verify(applicationRepository, times(1)).save(verifiedApplication);
    }

    @Test
    void testApproveApplication_DirectlyFromPending_ThrowsIllegalStateException() {
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(pendingApplication));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                applicationService.updateApplicationStatus(100L, ApplicationStatus.APPROVED, "Attempting early approval")
        );

        assertTrue(exception.getMessage().contains("Cannot approve an application before document verification"));
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void testTransition_FromApproved_ThrowsIllegalStateException() {
        PassApplication approvedApp = new PassApplication();
        approvedApp.setId(102L);
        approvedApp.setStatus(ApplicationStatus.APPROVED);

        when(applicationRepository.findById(102L)).thenReturn(Optional.of(approvedApp));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                applicationService.updateApplicationStatus(102L, ApplicationStatus.REJECTED, "Cannot reject approved")
        );

        assertTrue(exception.getMessage().contains("already APPROVED"));
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void testTransition_FromRejected_ThrowsIllegalStateException() {
        PassApplication rejectedApp = new PassApplication();
        rejectedApp.setId(103L);
        rejectedApp.setStatus(ApplicationStatus.REJECTED);

        when(applicationRepository.findById(103L)).thenReturn(Optional.of(rejectedApp));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                applicationService.updateApplicationStatus(103L, ApplicationStatus.VERIFIED, "Cannot verify rejected")
        );

        assertTrue(exception.getMessage().contains("already REJECTED"));
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void testTransition_SameStatus_ThrowsIllegalStateException() {
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(pendingApplication));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                applicationService.updateApplicationStatus(100L, ApplicationStatus.PENDING, "Same status")
        );

        assertTrue(exception.getMessage().contains("already in status: PENDING"));
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void testTransition_ToNullStatus_ThrowsIllegalArgumentException() {
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(pendingApplication));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                applicationService.updateApplicationStatus(100L, null, "Remarks")
        );

        assertTrue(exception.getMessage().contains("Target status cannot be null"));
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void testUpdateStatus_ApplicationNotFound_ThrowsResourceNotFoundException() {
        when(applicationRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                applicationService.updateApplicationStatus(999L, ApplicationStatus.VERIFIED, "Remarks")
        );

        assertTrue(exception.getMessage().contains("Application not found with id: 999"));
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void testRenewPass_Success() {
        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User("passenger@example.com", "pass",
                        java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_PASSENGER")));
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );

        when(userRepository.findByEmail("passenger@example.com")).thenReturn(Optional.of(passenger));

        com.transitease.entity.DigitalPass existingPass = new com.transitease.entity.DigitalPass();
        existingPass.setId(200L);
        existingPass.setPassenger(passenger);
        existingPass.setApplication(verifiedApplication);

        when(digitalPassRepository.findById(200L)).thenReturn(Optional.of(existingPass));
        when(applicationRepository.existsByRenewedPassIdAndStatusIn(eq(200L), any())).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(applicationRepository.save(any(PassApplication.class))).thenAnswer(invocation -> {
            PassApplication app = invocation.getArgument(0);
            app.setId(105L);
            return app;
        });

        com.transitease.dto.PassRenewalRequest renewalRequest = new com.transitease.dto.PassRenewalRequest(1L, "https://example.com/new_doc.pdf");
        com.transitease.dto.PassApplicationResponse response = applicationService.renewPass(200L, renewalRequest);

        assertNotNull(response);
        assertEquals(105L, response.getId());
        assertEquals("PENDING", response.getStatus());
        assertTrue(response.getIsRenewal());
        assertEquals(200L, response.getRenewedPassId());

        verify(applicationRepository, times(1)).save(any(PassApplication.class));
    }

    @Test
    void testRenewPass_AnotherPassenger_ThrowsAccessDeniedException() {
        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User("passenger@example.com", "pass",
                        java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_PASSENGER")));
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );

        when(userRepository.findByEmail("passenger@example.com")).thenReturn(Optional.of(passenger));

        User otherPassenger = new User();
        otherPassenger.setId(99L);
        com.transitease.entity.DigitalPass otherPass = new com.transitease.entity.DigitalPass();
        otherPass.setId(300L);
        otherPass.setPassenger(otherPassenger);

        when(digitalPassRepository.findById(300L)).thenReturn(Optional.of(otherPass));

        assertThrows(org.springframework.security.access.AccessDeniedException.class, () ->
                applicationService.renewPass(300L, new com.transitease.dto.PassRenewalRequest())
        );

        verify(applicationRepository, never()).save(any());
    }

    @Test
    void testRenewPass_DuplicatePending_ThrowsIllegalStateException() {
        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User("passenger@example.com", "pass",
                        java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_PASSENGER")));
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );

        when(userRepository.findByEmail("passenger@example.com")).thenReturn(Optional.of(passenger));

        com.transitease.entity.DigitalPass pass = new com.transitease.entity.DigitalPass();
        pass.setId(200L);
        pass.setPassenger(passenger);

        when(digitalPassRepository.findById(200L)).thenReturn(Optional.of(pass));
        when(applicationRepository.existsByRenewedPassIdAndStatusIn(eq(200L), any())).thenReturn(true);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                applicationService.renewPass(200L, new com.transitease.dto.PassRenewalRequest())
        );

        assertTrue(exception.getMessage().contains("already pending or under review"));
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void testGetMyApplications_ReturnsChronologicalOrder() {
        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User("passenger@example.com", "pass",
                        java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_PASSENGER")));
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );

        when(userRepository.findByEmail("passenger@example.com")).thenReturn(Optional.of(passenger));
        when(applicationRepository.findByPassengerOrderByApplicationDateDesc(passenger))
                .thenReturn(java.util.List.of(verifiedApplication, pendingApplication));

        java.util.List<com.transitease.dto.PassApplicationResponse> result = applicationService.getMyApplications();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(applicationRepository, times(1)).findByPassengerOrderByApplicationDateDesc(passenger);
    }

    @Test
    void testGetPassengerHistory_ReturnsChronologicalHistory() {
        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User("passenger@example.com", "pass",
                        java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_PASSENGER")));
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );

        when(userRepository.findByEmail("passenger@example.com")).thenReturn(Optional.of(passenger));
        when(applicationRepository.findByPassengerOrderByApplicationDateDesc(passenger))
                .thenReturn(java.util.List.of(verifiedApplication));

        com.transitease.entity.DigitalPass pass = new com.transitease.entity.DigitalPass();
        pass.setId(200L);
        pass.setPassNumber("TE-PASS-20261013-11223344");
        pass.setPassenger(passenger);
        pass.setApplication(verifiedApplication);

        when(digitalPassRepository.findByPassengerOrderByIssueDateDesc(passenger))
                .thenReturn(java.util.List.of(pass));

        com.transitease.dto.PassengerHistoryResponse history = applicationService.getPassengerHistory();

        assertNotNull(history);
        assertEquals(1, history.getApplications().size());
        assertEquals(1, history.getPasses().size());
        assertEquals("TE-PASS-20261013-11223344", history.getPasses().get(0).getPassNumber());
    }

    @Test
    void testApplyForPass_WithPersonalDetails_PersistsAllDetails() {
        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User("passenger@example.com", "pass",
                        java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_PASSENGER")));
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );

        when(userRepository.findByEmail("passenger@example.com")).thenReturn(Optional.of(passenger));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(applicationRepository.save(any(PassApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        com.transitease.dto.PassApplicationRequest req = new com.transitease.dto.PassApplicationRequest();
        req.setPassCategoryId(1L);
        req.setFullName("Rahul Ashok Sharma");
        req.setDob("2000-05-15");
        req.setGender("MALE");
        req.setMobileNumber("9876543210");
        req.setEmail("rahul@example.com");
        req.setResidentialAddress("Flat 101, Shivneri Apts");
        req.setCity("Pune");
        req.setPincode("411001");
        req.setPassengerCategory("STUDENT");
        req.setSourceStop("Swargate");
        req.setDestinationStop("Shivajinagar");
        req.setInstitutionName("COEP Technological University");
        req.setEmergencyContact("9822001122");

        PassApplicationResponse resp = applicationService.applyForPass(req);

        assertNotNull(resp);
        assertEquals("Rahul Ashok Sharma", resp.getFullName());
        assertEquals("2000-05-15", resp.getDob());
        assertEquals("MALE", resp.getGender());
        assertEquals("9876543210", resp.getMobileNumber());
        assertEquals("rahul@example.com", resp.getEmail());
        assertEquals("Flat 101, Shivneri Apts", resp.getResidentialAddress());
        assertEquals("Pune", resp.getCity());
        assertEquals("411001", resp.getPincode());
        assertEquals("STUDENT", resp.getPassengerCategory());
        assertEquals("Swargate", resp.getSourceStop());
        assertEquals("Shivajinagar", resp.getDestinationStop());
        assertEquals("COEP Technological University", resp.getInstitutionName());
        assertEquals("9822001122", resp.getEmergencyContact());
    }

    @Test
    void testApplyForPass_OmittedNameEmail_PrefillsFromPassenger() {
        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User("passenger@example.com", "pass",
                        java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_PASSENGER")));
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );

        when(userRepository.findByEmail("passenger@example.com")).thenReturn(Optional.of(passenger));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(applicationRepository.save(any(PassApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        com.transitease.dto.PassApplicationRequest req = new com.transitease.dto.PassApplicationRequest();
        req.setPassCategoryId(1L);

        PassApplicationResponse resp = applicationService.applyForPass(req);

        assertNotNull(resp);
        assertEquals("John Passenger", resp.getFullName());
        assertEquals("passenger@example.com", resp.getEmail());
    }

    @Test
    void testTransition_VerifiedToPending_ThrowsIllegalStateException() {
        PassApplication app = new PassApplication();
        app.setId(102L);
        app.setStatus(ApplicationStatus.VERIFIED);
        when(applicationRepository.findById(102L)).thenReturn(Optional.of(app));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> applicationService.updateApplicationStatus(102L, ApplicationStatus.PENDING, "Revert to pending"));
        assertTrue(ex.getMessage().contains("Invalid status transition from VERIFIED to PENDING"));
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void testRenewPass_OtherPassenger_ThrowsAccessDeniedException() {
        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User("passenger@example.com", "pass",
                        java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_PASSENGER")));
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );

        when(userRepository.findByEmail("passenger@example.com")).thenReturn(Optional.of(passenger));

        User otherPassenger = new User();
        otherPassenger.setId(99L);
        otherPassenger.setEmail("other@example.com");
        otherPassenger.setRole(Role.PASSENGER);

        com.transitease.entity.DigitalPass passOfOther = new com.transitease.entity.DigitalPass();
        passOfOther.setId(300L);
        passOfOther.setPassenger(otherPassenger);

        when(digitalPassRepository.findById(300L)).thenReturn(Optional.of(passOfOther));

        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> applicationService.renewPass(300L, new com.transitease.dto.PassRenewalRequest(1L, "doc.pdf")));
    }

    @Test
    void testRenewPass_AlreadyPendingRenewal_ThrowsIllegalStateException() {
        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User("passenger@example.com", "pass",
                        java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_PASSENGER")));
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );

        when(userRepository.findByEmail("passenger@example.com")).thenReturn(Optional.of(passenger));

        com.transitease.entity.DigitalPass pass = new com.transitease.entity.DigitalPass();
        pass.setId(200L);
        pass.setPassenger(passenger);

        when(digitalPassRepository.findById(200L)).thenReturn(Optional.of(pass));
        when(applicationRepository.existsByRenewedPassIdAndStatusIn(eq(200L), any())).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> applicationService.renewPass(200L, new com.transitease.dto.PassRenewalRequest(1L, "doc.pdf")));
        assertTrue(ex.getMessage().contains("already pending or under review"));
    }

    @Test
    void testGetAllApplications_ReturnsMappedList() {
        when(applicationRepository.findAll()).thenReturn(List.of(pendingApplication));

        List<PassApplicationResponse> all = applicationService.getAllApplications();

        assertNotNull(all);
        assertEquals(1, all.size());
        assertEquals(pendingApplication.getId(), all.get(0).getId());
    }
}
