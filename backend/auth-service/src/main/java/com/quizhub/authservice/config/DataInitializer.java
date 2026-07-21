package com.quizhub.authservice.config;

import com.quizhub.authservice.entity.auth.Role;
import com.quizhub.authservice.entity.auth.RoleType;
import com.quizhub.authservice.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {

        createRoleIfNotExists(RoleType.ROLE_USER);
        createRoleIfNotExists(RoleType.ROLE_ADMIN);

    }

    private void createRoleIfNotExists(RoleType roleType) {

        if (roleRepository.findByName(roleType).isEmpty()) {

            Role role = Role.builder()
                    .name(roleType)
                    .build();

            roleRepository.save(role);

            System.out.println("Created Role : " + roleType);
        }
    }
}