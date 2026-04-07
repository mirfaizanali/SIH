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
 * {@link UserDetailsService} implementation that performs lookup by <em>email address</em>.
 *
 * <p>This is used exclusively by the {@code DaoAuthenticationProvider} during
 * the login flow where Spring Security calls
 * {@code loadUserByUsername(email)} using the credential supplied by the client.</p>
 *
 * <p>Note the contrast with {@link UserDetailsServiceImpl} which looks up users by
 * UUID (used during JWT filter processing of authenticated requests).</p>
 */
@Slf4j
@Service("emailUserDetailsService")
@RequiredArgsConstructor
public class EmailUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads user details by email for authentication.
     *
     * @param email the user's email address (the {@code username} in Spring Security terminology)
     * @return fully populated {@link UserDetails}
     * @throws UsernameNotFoundException if no active user exists with this email
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "No user found with email: " + email));

        if (!user.isActive()) {
            throw new UsernameNotFoundException(
                    "User account is deactivated for email: " + email);
        }

        String password = StringUtils.hasText(user.getPasswordHash())
                ? user.getPasswordHash()
                : "";

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),   // username = email (for authentication only)
                password,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
