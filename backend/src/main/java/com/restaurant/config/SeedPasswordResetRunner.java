package com.restaurant.config;

import com.restaurant.entity.User;
import com.restaurant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * When app.reset-seed-passwords=true, resets all user passwords to "password123" (BCrypt).
 * Use this once if login fails with seed credentials (e.g. seed hash was for a different password).
 * Set in application.yml or env: RESET_SEED_PASSWORDS=true, then restart and remove it.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SeedPasswordResetRunner implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.reset-seed-passwords:false}")
    private boolean resetSeedPasswords;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!resetSeedPasswords) return;

        List<User> users = userRepository.findAll();
        if (users.isEmpty()) return;

        String encoded = passwordEncoder.encode("password123");
        for (User user : users) {
            user.setPassword(encoded);
            userRepository.save(user);
        }
        log.info("Reset passwords for {} users to 'password123'. Set app.reset-seed-passwords=false and restart.", users.size());
    }
}
