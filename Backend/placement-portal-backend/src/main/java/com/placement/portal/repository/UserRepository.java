package com.placement.portal.repository;

import com.placement.portal.domain.User;
import com.placement.portal.domain.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByOauth2ProviderAndOauth2Id(String oauth2Provider, String oauth2Id);

    // ---------------------------------------------------------------------------
    // Admin user management queries
    // ---------------------------------------------------------------------------

    Page<User> findByRole(Role role, Pageable pageable);

    Page<User> findByIsActive(boolean isActive, Pageable pageable);

    Page<User> findByRoleAndIsActive(Role role, boolean isActive, Pageable pageable);
}
