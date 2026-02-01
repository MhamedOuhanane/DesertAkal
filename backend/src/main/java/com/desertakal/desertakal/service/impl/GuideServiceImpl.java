package com.desertakal.desertakal.service.impl;

import com.desertakal.desertakal.exception.custom.BadRequestException;
import com.desertakal.desertakal.exception.custom.DuplicateResourceException;
import com.desertakal.desertakal.exception.custom.ResourceNotFoundException;
import com.desertakal.desertakal.model.dto.guide.GuideCreateDTO;
import com.desertakal.desertakal.model.dto.guide.GuideFindDTO;
import com.desertakal.desertakal.model.dto.guide.GuideUpdateDTO;
import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.entity.Guide;
import com.desertakal.desertakal.model.entity.Language;
import com.desertakal.desertakal.model.entity.Role;
import com.desertakal.desertakal.model.mapper.GuideMapper;
import com.desertakal.desertakal.repository.GuideRepository;
import com.desertakal.desertakal.repository.LanguageRepository;
import com.desertakal.desertakal.repository.RoleRepository;
import com.desertakal.desertakal.repository.UserRepository;
import com.desertakal.desertakal.service.interfaces.EmailVerificationTokenService;
import com.desertakal.desertakal.service.interfaces.GuideService;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GuideServiceImpl implements GuideService {
    private final GuideRepository repository;
    private final GuideMapper mapper;
    private final UserRepository userRepository;
    private final LanguageRepository languageRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationTokenService emailVerificationTokenService;
    private final MailService mailService;

    @Override
    public GuideFindDTO create(@NonNull GuideCreateDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            log.warn("Create failed: Email {} is already registered", dto.getEmail());
            throw new DuplicateResourceException("User", "Email", dto.getEmail());
        }
        if (userRepository.existsByUsername(dto.getUsername())) {
            log.warn("Create failed: Username {} is already taken", dto.getUsername());
            throw new DuplicateResourceException("User", "Username", dto.getEmail());
        }

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            log.warn("Create failed: Password confirmation does not match for email: {}", dto.getEmail());
            throw new BadRequestException("Passwords do not match. Please ensure both passwords are identical.");
        }

        log.debug("Fetching role with UUID: {}", dto.getRoleUuid());
        Role role = roleRepository.findByName("GUIDE")
                .orElseThrow(() -> {
                    log.error("Create failed: Role UUID {} not found", "GUIDE");
                    return new ResourceNotFoundException("Role", "name", "GUIDE");
                });

        Guide guide = mapper.toEntity(dto);
        String rawPassword = dto.getPassword();
        guide.setPassword(passwordEncoder.encode(rawPassword));
        guide.setRole(role);

        log.debug("Create languages for guide '{}'. New count requested: {}", guide.getEmail(), dto.getLanguageUsUuids().size());

        var languages = languageRepository.findDistinctByUuidIn(dto.getLanguageUsUuids());

        if (!Objects.equals(languages.size(), dto.getLanguageUsUuids().size())) {
            log.error("Create failed: Some language UUIDs are invalid for guide {}", guide.getEmail());
            throw new ResourceNotFoundException("Languages", "uuids", dto.getLanguageUsUuids().toString());
        }

        guide.setLanguages(languages);

        repository.save(guide);
        emailVerificationTokenService.createVerificationToken(guide.getEmail());

        mailService.sendGuideWelcomeEmail(guide.getEmail(), rawPassword);

        log.info("Guide created successfully and welcome email sent to: {}", guide.getEmail());

        return mapper.toFindDto(guide);
    }

    @Override
    public PaginationDTO findAll(String search, String language, @NonNull Pageable pageable) {
        log.info("Fetching guides list - Page: {}, Size: {}, Sort: {}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        Specification<@NonNull Guide> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                Expression<String> fullName = cb.concat(cb.concat(root.get("firstName"), " "), root.get("lastName"));
                predicates.add(cb.like(cb.lower(fullName), "%" + search.toLowerCase() + "%"));
            }

            if (language != null && !language.isBlank()) {
                Join<Guide, Language> languageJoin = root.join("languages");
                predicates.add(cb.like(cb.lower(languageJoin.get("name")), "%" + language.toLowerCase() + "%"));

                query.distinct(true);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        var guidePages = repository.findAll(spec, pageable);


        log.debug("Successfully retrieved {} guides from database", guidePages.getNumberOfElements());

        return PaginationDTO.builder()
                .content(mapper.toDtos(guidePages.getContent()))
                .page(guidePages.getNumber())
                .size(guidePages.getSize())
                .totalElements(guidePages.getTotalElements())
                .totalPages(guidePages.getTotalPages())
                .isFirst(guidePages.isFirst())
                .isLast(guidePages.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GuideFindDTO find(@NonNull UUID guideUuid) {
        log.info("Attempting to find Guide with UUID: {}", guideUuid);

        Guide guide = repository.findByUuid(guideUuid)
                .orElseThrow(() -> {
                    log.warn("Found failed: Guide {} not found", guideUuid);
                    return new ResourceNotFoundException("Guide", "identifier", guideUuid.toString());
                });

        log.debug("Guide successfully retrieved: {} (UUID: {})", guide.getEmail(), guideUuid);

        return mapper.toFindDto(guide);
    }

    @Override
    @Transactional
    public GuideFindDTO update(@NonNull UUID guideUuid, @NonNull GuideUpdateDTO dto) {
        log.info("Starting update process for Guide with UUID: {}", guideUuid);

        Guide guide = repository.findByUuid(guideUuid)
                .orElseThrow(() -> {
                    log.warn("Update failed: Guide with UUID {} not found", guideUuid);
                    return new ResourceNotFoundException("Guide", "identifier", guideUuid.toString());
                });

        if (dto.getEmail() != null && userRepository.existsByEmail(dto.getEmail())) {
            log.warn("Update failed: Guide email '{}' already exists", dto.getEmail());
            throw new DuplicateResourceException("Guide", "Email", dto.getEmail());
        }

        log.debug("Mapping UpdateDTO to Guide entity for UUID: {}", guideUuid);

        mapper.updateEntityFromDto(dto, guide);

        if (dto.getLanguageUsUuids() != null && !dto.getLanguageUsUuids().isEmpty()) {
            log.debug("Updating languages for guide '{}'. New count requested: {}", guide.getEmail(), dto.getLanguageUsUuids().size());

            var newLanguages = languageRepository.findDistinctByUuidIn(dto.getLanguageUsUuids());

            if (!Objects.equals(newLanguages.size(), dto.getLanguageUsUuids().size())) {
                log.error("Update failed: Some language UUIDs are invalid for guide {}", guideUuid);
                throw new ResourceNotFoundException("Languages", "uuids", dto.getLanguageUsUuids().toString());
            }

            guide.getLanguages().clear();
            guide.getLanguages().addAll(newLanguages);
        }

        log.info("Guide with UUID: {} successfully updated", guideUuid);

        return mapper.toFindDto(guide);
    }

    @Override
    public void delete(@NonNull UUID guideUuid) {

    }
}
