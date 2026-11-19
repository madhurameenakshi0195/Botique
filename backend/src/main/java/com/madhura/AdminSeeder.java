package com.madhura;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/** Creates or updates the single admin account from ADMIN_EMAIL and ADMIN_PASSWORD on every start. */
@Component
public class AdminSeeder implements ApplicationRunner {
    private final AdminRepository admins;
    private final String email;
    private final String password;

    public AdminSeeder(AdminRepository admins,
                       @Value("${app.admin-email}") String email,
                       @Value("${app.admin-password}") String password) {
        this.admins = admins;
        this.email = email.toLowerCase().trim();
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email.isEmpty() || password.isEmpty()) return;
        Admin a = admins.findByEmail(email).orElseGet(Admin::new);
        a.setEmail(email);
        a.setPasswordHash(new BCryptPasswordEncoder().encode(password));
        admins.save(a);
    }
}
