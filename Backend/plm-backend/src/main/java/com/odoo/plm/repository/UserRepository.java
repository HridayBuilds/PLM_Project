package com.odoo.plm.repository;

import com.odoo.plm.entity.User;
import com.odoo.plm.enums.Role;
import com.odoo.plm.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByLoginId(String loginId);

    Optional<User> findByEmail(String email);

    Boolean existsByLoginId(String loginId);

    Boolean existsByEmail(String email);

    Optional<User> findByLoginIdOrEmail(String loginId, String email);

    List<User> findByStatus(UserStatus status);

    List<User> findByStatusNot(UserStatus status);

    List<User> findByRole(Role role);
}
