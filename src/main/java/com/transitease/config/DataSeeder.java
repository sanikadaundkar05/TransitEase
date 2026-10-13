package com.transitease.config;

import com.transitease.entity.PassCategory;
import com.transitease.entity.Role;
import com.transitease.entity.Route;
import com.transitease.entity.User;
import com.transitease.repository.PassCategoryRepository;
import com.transitease.repository.RouteRepository;
import com.transitease.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PassCategoryRepository passCategoryRepository;
    private final RouteRepository routeRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      PassCategoryRepository passCategoryRepository,
                      RouteRepository routeRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passCategoryRepository = passCategoryRepository;
        this.routeRepository = routeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        seedUsers();
        seedPmpmlPassCategories();
        seedPmpmlRoutes();
    }

    private void seedUsers() {
        if (!userRepository.existsByEmail("admin@transitease.com")) {
            User admin = new User();
            admin.setFullName("System Admin");
            admin.setEmail("admin@transitease.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
        }

        if (!userRepository.existsByEmail("officer@transitease.com")) {
            User officer = new User();
            officer.setFullName("Pass Approval Officer");
            officer.setEmail("officer@transitease.com");
            officer.setPassword(passwordEncoder.encode("officer123"));
            officer.setRole(Role.PASS_APPROVAL_OFFICER);
            userRepository.save(officer);
        }

        if (!userRepository.existsByEmail("verifier@transitease.com")) {
            User verifier = new User();
            verifier.setFullName("Document Verification Officer");
            verifier.setEmail("verifier@transitease.com");
            verifier.setPassword(passwordEncoder.encode("verifier123"));
            verifier.setRole(Role.DOCUMENT_VERIFICATION_OFFICER);
            userRepository.save(verifier);
        }
    }

    private void seedPmpmlPassCategories() {
        // A. General Passenger Passes
        upsertCategory(
                "PMC & PCMC Area Daily Pass",
                "GENERAL",
                "24-hour unlimited validity on all standard PMPML bus routes within Pune Municipal Corporation (PMC) and Pimpri Chinchwad (PCMC) limits.",
                1,
                70.0,
                "Open to all general passengers travelling within PMC and PCMC limits.",
                "",
                "Government-issued photo ID (if requested for on-board inspection)",
                "FIXED",
                "Standard 24-hour municipal commuter daily pass"
        );

        upsertCategory(
                "All-PMPML / PMRDA Area Daily Pass",
                "GENERAL",
                "24-hour unlimited travel across entire PMPML network including PMRDA extended rural and metro feeder routes.",
                1,
                150.0,
                "Open to all general passengers travelling across the entire Pune Metropolitan Region.",
                "",
                "Government-issued photo ID (if requested for on-board inspection)",
                "FIXED",
                "Full-network 24-hour unrestricted daily transit pass"
        );

        upsertCategory(
                "General Monthly Pass",
                "GENERAL",
                "Unlimited travel on all PMPML city routes within PMC and PCMC limits for 30 consecutive days.",
                30,
                1500.0,
                "Regular commuters residing or working in the Pune/Pimpri-Chinchwad municipal limits.",
                "Completed application form,Government-issued photo ID (Aadhaar / Voter ID / Passport / Driving Licence),Recent passport-size photograph",
                "Proof of residence (Electricity bill / Rental agreement)",
                "FIXED",
                "Monthly commuter pass with unlimited daily trips"
        );

        // B. Student Passes
        upsertCategory(
                "Monthly Student Pass",
                "STUDENT",
                "Concessional monthly bus pass for bonafide students studying in recognized schools, colleges, and institutes.",
                30,
                750.0,
                "Regular students of recognized schools, colleges, ITIs, or universities.",
                "Current school/college ID or valid bonafide certificate,Recent passport-size photograph",
                "Proof of age,Proof of residence,Current fee receipt,Application or verification signed by institution/parent/guardian",
                "FIXED",
                "Concessional student pass subsidized by municipal transport policy"
        );

        upsertCategory(
                "Quarterly Student Pass",
                "STUDENT",
                "Concessional 3-month (90 days) bus pass for students covering regular academic semesters.",
                90,
                3000.0,
                "Regular students of recognized schools, colleges, and educational institutes.",
                "Current school/college ID or valid bonafide certificate,Recent passport-size photograph",
                "Proof of age,Proof of residence,Current fee receipt,Application or verification signed by institution/parent/guardian",
                "FIXED",
                "Quarterly term pass for academic students"
        );

        upsertCategory(
                "Half-Yearly Student Pass",
                "STUDENT",
                "Concessional 6-month (180 days) transit pass for students covering a complete academic term.",
                180,
                4000.0,
                "Regular students of recognized educational institutions.",
                "Current school/college ID or valid bonafide certificate,Recent passport-size photograph",
                "Proof of age,Proof of residence,Current fee receipt,Application or verification signed by institution/parent/guardian",
                "FIXED",
                "Half-yearly semester pass with high student travel subsidy"
        );

        upsertCategory(
                "Annual Student Pass",
                "STUDENT",
                "Comprehensive 1-year (365 days) unlimited transit pass for full-time students across the academic year.",
                365,
                5000.0,
                "Full-time students enrolled in recognized educational institutions.",
                "Current school/college ID or valid bonafide certificate,Recent passport-size photograph",
                "Proof of age,Proof of residence,Current fee receipt,Application or verification signed by institution/parent/guardian",
                "FIXED",
                "Full academic session annual pass with maximum student concession"
        );

        upsertCategory(
                "Student Punching Pass",
                "STUDENT",
                "Stage-based punching concession pass with applicable discount coupons based on commuting stages.",
                180,
                0.0,
                "Bonafide students commuting between fixed residence and educational institution stages.",
                "Current school/college ID or valid bonafide certificate,Recent passport-size photograph",
                "Current fee receipt,Application or verification signed by institution/parent/guardian",
                "CONFIGURABLE",
                "Applicable punching pass concession (variable discount based on stage count/coupon strip as per PMPML rules)"
        );

        // C. Senior Citizen Passes
        upsertCategory(
                "Senior Citizen Monthly Pass",
                "SENIOR_CITIZEN",
                "Concessional monthly pass for elderly citizens with special subsidized boarding across PMPML buses.",
                30,
                0.0,
                "Senior citizens aged 65 and above residing in PMC/PCMC/PMRDA jurisdictions.",
                "Government-issued age/identity proof (Aadhaar / Voter ID / Senior Citizen Card)",
                "Recent passport-size photograph,Completed application form",
                "CONFIRMATION_REQUIRED",
                "Fare confirmed upon verification of age and residency proof as per municipal concessional rules"
        );

        upsertCategory(
                "Senior Citizen Daily/Concessional Pass",
                "SENIOR_CITIZEN",
                "Concessional single-day transit pass for senior citizens with verified age proof.",
                1,
                0.0,
                "Senior citizens aged 65 and above with valid government age proof.",
                "Government-issued age/identity proof",
                "Senior citizen identity card",
                "CONFIRMATION_REQUIRED",
                "Discounted single-day fare determined per municipal resolution upon proof verification"
        );

        // D. Other Concessionary Passes
        upsertCategory(
                "Person with Disability Pass",
                "CONCESSIONARY",
                "Concessional transit pass for individuals with certified benchmark disability.",
                365,
                0.0,
                "Persons with benchmark physical disability (40% and above) certified by competent medical authority.",
                "Government-issued identity proof,Applicable disability certificate or officially accepted eligibility document",
                "Photograph",
                "CONCESSIONAL",
                "Subsidized / 100% concession as per Municipal Corporation social welfare policy"
        );

        upsertCategory(
                "Visually Impaired Passenger Pass",
                "CONCESSIONARY",
                "Concessional / free travel pass for visually impaired individuals and certified escort.",
                365,
                0.0,
                "Individuals with certified benchmark visual impairment with valid medical board certificate.",
                "Government-issued identity proof,Applicable disability certificate or officially accepted eligibility document",
                "Photograph",
                "CONCESSIONAL",
                "Full travel concession / free travel as per government and PMPML guidelines"
        );

        upsertCategory(
                "Journalist/Reporter Pass",
                "CONCESSIONARY",
                "Concessional transit pass for accredited media representatives and field journalists.",
                365,
                0.0,
                "Accredited journalists, press photographers, and field reporters recognized by DGIPR / State Government.",
                "Government-issued identity proof,Valid press accreditation or accepted journalist ID",
                "Other configured eligibility documents",
                "CONCESSIONAL",
                "Concessional media boarding privileges as per government media pass quota"
        );

        upsertCategory(
                "Freedom Fighter Pass",
                "CONCESSIONARY",
                "Honorary free transit pass for recognized freedom fighters and eligible surviving spouses.",
                365,
                0.0,
                "Recognized Freedom Fighters holding central/state government pension sanad or identity authorization.",
                "Government-issued identity proof,Official freedom fighter certificate or accepted eligibility proof",
                "Other configured documents",
                "CONCESSIONAL",
                "100% honorary fare waiver provided under civic honors"
        );
    }

    private void upsertCategory(String name, String group, String description, int validityDays,
                                Double baseFare, String eligibility, String mandatoryDocs,
                                String optionalDocs, String fareType, String discountDetails) {
        PassCategory category = passCategoryRepository.findByName(name).orElse(null);
        if (category == null) {
            category = new PassCategory();
            category.setName(name);
        }
        category.setCategoryGroup(group);
        category.setDescription(description);
        category.setValidityDays(validityDays);
        category.setBaseFare(baseFare);
        category.setEligibility(eligibility);
        category.setMandatoryDocuments(mandatoryDocs);
        category.setOptionalDocuments(optionalDocs);
        category.setFareType(fareType);
        category.setDiscountDetails(discountDetails);
        passCategoryRepository.save(category);
    }

    private void seedPmpmlRoutes() {
        if (routeRepository.count() == 0) {
            Route r1 = new Route(null, "101-SWAR", "Katraj Hub", "Swargate", "Via Padmavati, Bibwewadi", true, 15.0, 7.5, "Katraj, Padmavati, Dhankawadi, City Pride, Swargate");
            Route r2 = new Route(null, "202-HINJ", "Pune Railway Station", "Hinjawadi Phase 3", "IT Park Fast Express", true, 35.0, 24.0, "Pune Station, Shivajinagar, University Circle, Aundh, Wakad, Hinjawadi Ph 1, Hinjawadi Ph 3");
            Route r3 = new Route(null, "303-KOTH", "Hadapsar Gadital", "Kothrud Depot", "East-West Cross City Corridor", true, 25.0, 16.5, "Hadapsar, Fatima Nagar, Swargate, Deccan Gymkhana, Karve Road, Kothrud Depot");
            Route r4 = new Route(null, "404-NIGD", "Nigdi", "Pune Railway Station", "Old Pune-Mumbai Highway Trunk Line", true, 30.0, 21.0, "Nigdi, Akurdi, Chinchwad, Pimpri, Kasarwadi, Khadki, Shivajinagar, Pune Station");
            routeRepository.save(r1);
            routeRepository.save(r2);
            routeRepository.save(r3);
            routeRepository.save(r4);
        }
    }
}
