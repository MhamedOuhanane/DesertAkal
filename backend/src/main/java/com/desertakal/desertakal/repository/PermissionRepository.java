package com.desertakal.desertakal.repository;

import com.desertakal.desertakal.model.entity.Permission;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository extends JpaRepository<@NonNull Permission, @NonNull UUID>, JpaSpecificationExecutor<@NonNull Permission> {
    Optional<Permission> findByUuid(UUID uuid);
    Optional<Permission> findByName(String name);

    List<@NonNull Permission> findDistinctByUuidIn(List<@NonNull UUID> uuids);
}
