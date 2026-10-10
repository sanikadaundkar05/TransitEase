package com.transitease.config;

import com.transitease.entity.PassCategory;
import com.transitease.entity.Role;
import com.transitease.entity.User;
import com.transitease.repository.PassCategoryRepository;
import com.transitease.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PassCategoryRepository passCategoryRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PassCategoryRepository passCategoryRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passCategoryRepository = passCategoryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
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

        if (!passCategoryRepository.existsByName("Monthly Student Pass")) {
            PassCategory studentPass = new PassCategory();
            studentPass.setName("Monthly Student Pass");
            studentPass.setDescription("Valid for 30 days for students");
            studentPass.setValidityDays(30);
            studentPass.setBaseFare(15.0);
            passCategoryRepository.save(studentPass);
        }

        if (!passCategoryRepository.existsByName("Weekly Commuter Pass")) {
            PassCategory weeklyPass = new PassCategory();
            weeklyPass.setName("Weekly Commuter Pass");
            weeklyPass.setDescription("Valid for 7 days");
            weeklyPass.setValidityDays(7);
            weeklyPass.setBaseFare(10.0);
            passCategoryRepository.save(weeklyPass);
        }
    }
}
