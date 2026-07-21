package com.quizhub.authservice.repository;

import com.quizhub.authservice.entity.auth.Role;
import com.quizhub.authservice.entity.auth.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository  extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(RoleType name);
}
