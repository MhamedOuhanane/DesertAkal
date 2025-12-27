package com.desertakal.desertakal.repository;

import com.desertakal.desertakal.model.entity.User;
import com.desertakal.desertakal.model.entity.UserOAuth;
import com.desertakal.desertakal.model.enums.OauthProvider;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserOAuthRepository extends JpaRepository<UserOAuth, UUID> {

    Optional<UserOAuth> findByUserAndProvider(User user, OauthProvider provider);
    List<UserOAuth> findByUser(User user);

    boolean existsByUserAndProvider(User user, OauthProvider provider);
}
