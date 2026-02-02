package com.desertakal.desertakal.controller;

import com.desertakal.desertakal.model.dto.city.CityCreateDTO;
import com.desertakal.desertakal.model.dto.city.CityFindDTO;
import com.desertakal.desertakal.model.dto.responce.StandardResponseDTO;
import com.desertakal.desertakal.service.interfaces.CityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/cities")
@RequiredArgsConstructor
@Slf4j
public class CityController {
    private final CityService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull CityFindDTO>> create(
            @Valid @RequestBody CityCreateDTO dto,
            HttpServletRequest request
    ) {
        log.info("REST request to create a new City with name: {}", dto.getName());

        var result = service.create(dto);

        var response = StandardResponseDTO.<CityFindDTO>builder()
                .timestamp(LocalDateTime.now())
                .message("City account has been created successfully.")
                .status(201)
                .data(result)
                .path(request.getServletPath())
                .build();

        log.info("Successfully created City with UUID: {}", result.getUuid());

        return ResponseEntity.status(201).body(response);
    }

}
