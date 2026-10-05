package lab.java.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import lab.java.demo.Models.Role;
import lab.java.demo.service.UserService;

/**
 * Issue 21: admin-only endpoints ({@code POST /api/users}) need at least
 * one ADMIN account to exist before anyone can create further accounts
 * -- otherwise there's no way to create the very first user. This seeds
 * exactly one admin account at startup, if one doesn't already exist,
 * using credentials read from configuration ({@code app.admin.username}
 * / {@code app.admin.password} in application.properties, overridable
 * with the {@code APP_ADMIN_USERNAME} / {@code APP_ADMIN_PASSWORD}
 * environment variables) rather than a literal written into Java source.
 *
 * <p>The default values shipped in application.properties are for local
 * development only; a real deployment overrides them with its own
 * environment variables before the admin account is ever created.
 */
@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UserService userService;
    private final String adminUsername;
    private final String adminPassword;

    public AdminBootstrap(UserService userService,
                           @Value("${app.admin.username}") String adminUsername,
                           @Value("${app.admin.password}") String adminPassword) {
        this.userService = userService;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userService.findByUsername(adminUsername).isPresent()) {
            log.info("Bootstrap admin account '{}' already exists; skipping seeding", adminUsername);
            return;
        }
        userService.createUser(adminUsername, adminPassword, Role.ADMIN);
        log.info("Seeded bootstrap admin account '{}'. Log in via POST /api/auth/login, "
                + "then create additional accounts via POST /api/users.", adminUsername);
    }
}
