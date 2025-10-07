package com.multi.loyaltybackend.auth.repository;

import com.multi.loyaltybackend.auth.model.ERole;
import com.multi.loyaltybackend.auth.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(ERole name);
}
