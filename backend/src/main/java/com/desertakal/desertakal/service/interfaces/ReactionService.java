package com.desertakal.desertakal.service.interfaces;

import com.desertakal.desertakal.model.dto.reaction.ReactionCreateDTO;
import com.desertakal.desertakal.model.dto.reaction.ReactionSummaryDTO;
import com.desertakal.desertakal.model.dto.reaction.ReactionToggleResponseDTO;
import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.enums.ReactionEnum;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ReactionService {
    ReactionToggleResponseDTO toggle(@NonNull ReactionCreateDTO dto, @NonNull UUID userUuid);

    ReactionSummaryDTO getSummary(@NonNull UUID articleUuid, @NonNull UUID currentUserUuid);

    PaginationDTO getByArticle(@NonNull UUID articleUuid, ReactionEnum type, @NonNull Pageable pageable);
}
