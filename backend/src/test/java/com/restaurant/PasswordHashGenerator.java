package com.restaurant;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Run once to generate a BCrypt hash for seed data: mvn test -Dtest=PasswordHashGenerator#generateSeedHash
 * Then update V2__seed_data.sql to use the printed hash for all user passwords.
 */
class PasswordHashGenerator {

    @Test
    void generateSeedHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode("password123");
        System.out.println("BCrypt hash for 'password123' (use in V2__seed_data.sql):");
        System.out.println(hash);
        assert encoder.matches("password123", hash);
    }
}
