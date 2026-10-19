package com.transitease.service;

import com.transitease.dto.PassCategoryRequest;
import com.transitease.dto.PassCategoryResponse;
import com.transitease.entity.PassCategory;
import com.transitease.exception.ResourceNotFoundException;
import com.transitease.repository.PassCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PassCategoryServiceTest {

    @Mock
    private PassCategoryRepository passCategoryRepository;

    @InjectMocks
    private PassCategoryService passCategoryService;

    private PassCategory sampleCategory;

    @BeforeEach
    void setUp() {
        sampleCategory = new PassCategory();
        sampleCategory.setId(1L);
        sampleCategory.setName("PMC & PCMC Area Daily Pass");
        sampleCategory.setCategoryGroup("GENERAL");
        sampleCategory.setDescription("24-hour unlimited validity on standard PMPML bus routes");
        sampleCategory.setValidityDays(1);
        sampleCategory.setBaseFare(70.0);
        sampleCategory.setEligibility("Open to all general commuters");
        sampleCategory.setMandatoryDocuments("");
        sampleCategory.setOptionalDocuments("Government-issued photo ID");
        sampleCategory.setFareType("FIXED");
        sampleCategory.setDiscountDetails("Standard daily pass");
    }

    @Test
    void testCreatePassCategory_GeneralDailyPass_Success() {
        PassCategoryRequest request = new PassCategoryRequest(
                "PMC & PCMC Area Daily Pass",
                "24-hour unlimited validity on standard PMPML bus routes",
                1,
                70.0,
                "GENERAL",
                "Open to all general commuters",
                "",
                "Government-issued photo ID",
                "FIXED",
                "Standard daily pass"
        );

        when(passCategoryRepository.existsByName("PMC & PCMC Area Daily Pass")).thenReturn(false);
        when(passCategoryRepository.save(any(PassCategory.class))).thenReturn(sampleCategory);

        PassCategoryResponse response = passCategoryService.createPassCategory(request);

        assertNotNull(response);
        assertEquals("PMC & PCMC Area Daily Pass", response.getName());
        assertEquals("GENERAL", response.getCategoryGroup());
        assertEquals(70.0, response.getBaseFare());
        assertEquals(1, response.getValidityDays());
        assertTrue(response.getMandatoryDocumentList().isEmpty());
        assertEquals(1, response.getOptionalDocumentList().size());
        assertEquals("Government-issued photo ID", response.getOptionalDocumentList().get(0));
    }

    @Test
    void testCreatePassCategory_StudentMonthlyPass_WithDocumentChecklist() {
        PassCategory studentCategory = new PassCategory();
        studentCategory.setId(2L);
        studentCategory.setName("Monthly Student Pass");
        studentCategory.setCategoryGroup("STUDENT");
        studentCategory.setDescription("Concessional 30-day student pass");
        studentCategory.setValidityDays(30);
        studentCategory.setBaseFare(750.0);
        studentCategory.setEligibility("Regular bonafide students");
        studentCategory.setMandatoryDocuments("Current school/college ID or bonafide certificate, Recent passport photograph");
        studentCategory.setOptionalDocuments("Proof of age, Fee receipt");
        studentCategory.setFareType("FIXED");
        studentCategory.setDiscountDetails("Municipal student subsidy");

        PassCategoryRequest request = new PassCategoryRequest(
                "Monthly Student Pass",
                "Concessional 30-day student pass",
                30,
                750.0,
                "STUDENT",
                "Regular bonafide students",
                "Current school/college ID or bonafide certificate, Recent passport photograph",
                "Proof of age, Fee receipt",
                "FIXED",
                "Municipal student subsidy"
        );

        when(passCategoryRepository.existsByName("Monthly Student Pass")).thenReturn(false);
        when(passCategoryRepository.save(any(PassCategory.class))).thenReturn(studentCategory);

        PassCategoryResponse response = passCategoryService.createPassCategory(request);

        assertNotNull(response);
        assertEquals("Monthly Student Pass", response.getName());
        assertEquals("STUDENT", response.getCategoryGroup());
        assertEquals(750.0, response.getBaseFare());
        assertEquals(30, response.getValidityDays());
        assertEquals(2, response.getMandatoryDocumentList().size());
        assertEquals("Current school/college ID or bonafide certificate", response.getMandatoryDocumentList().get(0));
        assertEquals("Recent passport photograph", response.getMandatoryDocumentList().get(1));
        assertEquals(2, response.getOptionalDocumentList().size());
    }

    @Test
    void testCreatePassCategory_SeniorCitizen_ConfirmationRequired() {
        PassCategory seniorCat = new PassCategory();
        seniorCat.setId(3L);
        seniorCat.setName("Senior Citizen Monthly Pass");
        seniorCat.setCategoryGroup("SENIOR_CITIZEN");
        seniorCat.setValidityDays(30);
        seniorCat.setBaseFare(0.0);
        seniorCat.setFareType("CONFIRMATION_REQUIRED");
        seniorCat.setEligibility("Aged 65 and above");
        seniorCat.setMandatoryDocuments("Government-issued age/identity proof");
        seniorCat.setOptionalDocuments("Recent photo");

        PassCategoryRequest request = new PassCategoryRequest(
                "Senior Citizen Monthly Pass",
                "Concessional senior pass",
                30,
                0.0,
                "SENIOR_CITIZEN",
                "Aged 65 and above",
                "Government-issued age/identity proof",
                "Recent photo",
                "CONFIRMATION_REQUIRED",
                "Fare on confirmation"
        );

        when(passCategoryRepository.existsByName("Senior Citizen Monthly Pass")).thenReturn(false);
        when(passCategoryRepository.save(any(PassCategory.class))).thenReturn(seniorCat);

        PassCategoryResponse response = passCategoryService.createPassCategory(request);

        assertNotNull(response);
        assertEquals("SENIOR_CITIZEN", response.getCategoryGroup());
        assertEquals("CONFIRMATION_REQUIRED", response.getFareType());
        assertEquals(0.0, response.getBaseFare());
        assertEquals(1, response.getMandatoryDocumentList().size());
        assertEquals("Government-issued age/identity proof", response.getMandatoryDocumentList().get(0));
    }

    @Test
    void testUpdatePassCategory_UpdateDocuments_Success() {
        PassCategoryRequest updateReq = new PassCategoryRequest(
                "PMC & PCMC Area Daily Pass",
                "Updated description",
                1,
                70.0,
                "GENERAL",
                "Updated eligibility",
                "Aadhaar Card",
                "",
                "FIXED",
                "Updated details"
        );

        when(passCategoryRepository.findById(1L)).thenReturn(Optional.of(sampleCategory));
        when(passCategoryRepository.existsByNameAndIdNot("PMC & PCMC Area Daily Pass", 1L)).thenReturn(false);
        when(passCategoryRepository.save(any(PassCategory.class))).thenReturn(sampleCategory);

        PassCategoryResponse response = passCategoryService.updatePassCategory(1L, updateReq);

        assertNotNull(response);
        verify(passCategoryRepository).save(sampleCategory);
    }

    @Test
    void testGetAllPassCategories_ReturnsAllWithFormattedDocumentLists() {
        when(passCategoryRepository.findAll()).thenReturn(List.of(sampleCategory));

        List<PassCategoryResponse> result = passCategoryService.getAllPassCategories();

        assertEquals(1, result.size());
        assertEquals("PMC & PCMC Area Daily Pass", result.get(0).getName());
        assertEquals("GENERAL", result.get(0).getCategoryGroup());
        assertEquals(70.0, result.get(0).getBaseFare());
    }

    @Test
    void testGetPassCategoryById_NotFound_ThrowsException() {
        when(passCategoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> passCategoryService.getPassCategoryById(99L));
    }

    @Test
    void testCreatePassCategory_DuplicateName_ThrowsException() {
        PassCategoryRequest req = new PassCategoryRequest();
        req.setName("PMC & PCMC Area Daily Pass");
        when(passCategoryRepository.existsByName("PMC & PCMC Area Daily Pass")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> passCategoryService.createPassCategory(req));
    }

    @Test
    void testUpdatePassCategory_DuplicateNameOnAnotherCategory_ThrowsException() {
        PassCategoryRequest req = new PassCategoryRequest();
        req.setName("Duplicate Name");
        when(passCategoryRepository.findById(1L)).thenReturn(Optional.of(sampleCategory));
        when(passCategoryRepository.existsByNameAndIdNot("Duplicate Name", 1L)).thenReturn(true);

        assertThrows(RuntimeException.class, () -> passCategoryService.updatePassCategory(1L, req));
        verify(passCategoryRepository, never()).save(any());
    }

    @Test
    void testUpdatePassCategory_NotFound_ThrowsException() {
        PassCategoryRequest req = new PassCategoryRequest();
        req.setName("Non existent");
        when(passCategoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> passCategoryService.updatePassCategory(999L, req));
    }

    @Test
    void testDeletePassCategory_Success() {
        when(passCategoryRepository.findById(1L)).thenReturn(Optional.of(sampleCategory));
        doNothing().when(passCategoryRepository).delete(sampleCategory);

        assertDoesNotThrow(() -> passCategoryService.deletePassCategory(1L));
        verify(passCategoryRepository).delete(sampleCategory);
    }

    @Test
    void testDeletePassCategory_NotFound_ThrowsException() {
        when(passCategoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> passCategoryService.deletePassCategory(999L));
        verify(passCategoryRepository, never()).delete(any());
    }

    @Test
    void testCreatePassCategory_NullBaseFareAndFareType_DefaultsProperly() {
        PassCategoryRequest request = new PassCategoryRequest();
        request.setName("Boundary Pass");
        request.setBaseFare(null);
        request.setFareType(null);

        when(passCategoryRepository.existsByName("Boundary Pass")).thenReturn(false);
        when(passCategoryRepository.save(any(PassCategory.class))).thenAnswer(invocation -> {
            PassCategory cat = invocation.getArgument(0);
            cat.setId(50L);
            return cat;
        });

        PassCategoryResponse response = passCategoryService.createPassCategory(request);

        assertNotNull(response);
        assertEquals(0.0, response.getBaseFare());
        assertEquals("FIXED", response.getFareType());
    }
}
