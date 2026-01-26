package com.desertakal.desertakal.repository;

import com.desertakal.desertakal.model.entity.Tourist;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface TouristRepository extends JpaRepository<@NonNull Tourist, @NonNull UUID>, JpaSpecificationExecutor<@NonNull Tourist> {
    
}
