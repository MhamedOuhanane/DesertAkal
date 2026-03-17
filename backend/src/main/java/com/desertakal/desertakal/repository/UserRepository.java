package com.desertakal.desertakal.repository;

import com.desertakal.desertakal.model.entity.Role;
import com.desertakal.desertakal.model.entity.User;
import com.desertakal.desertakal.model.enums.UserStatus;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<@NonNull User, @NonNull UUID>, JpaSpecificationExecutor<@NonNull User> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    Optional<User> findByUuid(UUID uuid);

    boolean existsByUuid(@NonNull UUID uuid);

    @EntityGraph(attributePaths = {"role", "role.permissions"})
    @Query("SELECT u FROM User u WHERE u.email = :identifier OR u.username = :identifier")
    Optional<User> findByEmailOrUsernameWithSecurity(@Param("identifier") String identifier);

    @EntityGraph(attributePaths = {"role", "role.permissions"})
    @Query("""
        SELECT u FROM User u
        JOIN FETCH u.role r
        LEFT JOIN FETCH r.permissions
        WHERE u.uuid = :uuid
    """)
    Optional<User> findWithSecurityByUuid(@Param("uuid") UUID uuid);

    long countByRole_NameAndStatus(String roleName, UserStatus status);

    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
