package com.desertakal.desertakal.service.interfaces;

import com.desertakal.desertakal.model.dto.guide.GuideCreateDTO;
import com.desertakal.desertakal.model.dto.guide.GuideDTO;
import com.desertakal.desertakal.model.dto.guide.GuideFindDTO;
import com.desertakal.desertakal.model.dto.guide.GuideUpdateDTO;
import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface GuideService {
    GuideFindDTO create(@NonNull GuideCreateDTO dto);
    PaginationDTO findAll(String search, String language, @NonNull Pageable pageable);
    GuideFindDTO find(@NonNull UUID guideUuid);
    GuideFindDTO update(@NonNull UUID guideUuid, @NonNull GuideUpdateDTO dto);
    List<GuideDTO> findAvailable(@NonNull LocalDateTime startDate, @NonNull LocalDateTime endDate, String language);
    List<GuideDTO> findTop5();
}
