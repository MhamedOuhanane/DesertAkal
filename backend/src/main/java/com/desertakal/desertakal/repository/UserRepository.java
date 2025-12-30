package com.desertakal.desertakal.repository;

import com.desertakal.desertakal.model.entity.User;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<@NonNull User, @NonNull UUID> {

    @EntityGraph(attributePaths = {"role", "role.permissions"})
    @Query("Select u From User u Where u.email = :identifier Or u.username = :identifier")
    Optional<User> findByEmailOrUsername(@Param("identifier") String identifier);

    @EntityGraph(attributePaths = {"role, role.permissions"})
    Optional<User> findByUuid(UUID uuid);

    @EntityGraph(attributePaths = {"role", "role.permissions"})
    @Query("SELECT u FROM User u WHERE u.email = :identifier OR u.username = :identifier")
    Optional<User> findByEmailOrUsernameWithSecurity(@Param("identifier") String identifier);

    @EntityGraph(attributePaths = {"role", "role.permissions"})
    Optional<User> findWithSecurityByUuid(UUID uuid);

    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
