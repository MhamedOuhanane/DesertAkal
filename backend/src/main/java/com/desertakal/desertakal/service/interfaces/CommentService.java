package com.desertakal.desertakal.service.interfaces;

import com.desertakal.desertakal.model.dto.comment.CommentCreateDTO;
import com.desertakal.desertakal.model.dto.comment.CommentDTO;
import com.desertakal.desertakal.model.dto.comment.CommentUpdateDTO;
import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CommentService {
    CommentDTO create(@NonNull CommentCreateDTO dto, @NonNull UUID authorUuid);

    CommentDTO update(UUID commentUuid, @NonNull CommentUpdateDTO dto, @NonNull UUID currentUserUuid);

    void delete(UUID commentUuid, @NonNull UUID currentUserUuid, boolean isAdmin);

    CommentDTO getByUuid(UUID commentUuid);

    PaginationDTO getByArticle(UUID articleUuid, @NonNull Pageable pageable);

    PaginationDTO getByUser(UUID userUuid, @NonNull Pageable pageable);
}
