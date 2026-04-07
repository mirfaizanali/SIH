package com.placement.portal.security;

import com.placement.portal.domain.User;
import com.placement.portal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Loads a {@link UserDetails} instance by the user's UUID (not email).
 *
 * <p>The principal's username is the user's UUID string so that the
 * {@link org.springframework.security.core.Authentication} name can be
 * used directly as a database FK without a second look-up.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Looks up the user by their UUID id (the JWT {@code sub} claim).
     *
     * @param userId the user's UUID string
     * @return a fully populated {@link UserDetails} instance
     * @throws UsernameNotFoundException if no active user exists with this id
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with id: " + userId));

        if (!user.isActive()) {
            throw new UsernameNotFoundException(
                    "User account is deactivated for id: " + userId);
        }

        // OAuth2 users may not have a password hash stored.
        String password = StringUtils.hasText(user.getPasswordHash())
                ? user.getPasswordHash()
                : "";

        return new org.springframework.security.core.userdetails.User(
                user.getId(),
                password,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
