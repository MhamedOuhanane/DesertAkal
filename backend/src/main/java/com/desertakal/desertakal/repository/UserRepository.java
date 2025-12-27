package com.desertakal.desertakal.repository;

import com.desertakal.desertakal.model.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    @EntityGraph(attributePaths = {"role", "role.permissions"})
    @Query("Select u From User u Where u.email = :identifier Or u.username = :identifier")
    Optional<User> findByEmailOrUsername(@Param("identifier") String identifier);

    @EntityGraph(attributePaths = {"role, role.permissions"})
    Optional<User> findByUuid(UUID uuid);

    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
