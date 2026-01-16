package com.desertakal.desertakal.repository;

import com.desertakal.desertakal.model.entity.Role;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<@NonNull Role, @NonNull UUID> {
    Optional<Role> findByUuid(UUID uuid);
    Optional<Role> findByName(String name);

    @EntityGraph(attributePaths = {"role.permissions"})
    Optional<Role> findRoleByUuid(UUID uuid);
}
