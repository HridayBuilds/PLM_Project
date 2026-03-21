package com.odoo.plm.repository;

import com.odoo.plm.entity.User;
import com.odoo.plm.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {

    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByUser(User user);

    void deleteByExpiryDateLessThan(LocalDateTime now);

    void deleteByUser(User user);
}
