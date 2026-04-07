package com.placement.portal.config;

import tools.jackson.databind.ObjectMapper;
import com.placement.portal.security.AuditLoggingFilter;
import com.placement.portal.security.EmailUserDetailsService;
import com.placement.portal.security.RateLimitingFilter;
import com.placement.portal.security.jwt.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import java.io.IOException;
import java.util.Map;

/**
 * Central Spring Security configuration for the Placement Portal.
 *
 * <ul>
 *   <li>Stateless JWT-based session (no HTTP session)</li>
 *   <li>CSRF disabled (REST API + SameSite cookies)</li>
 *   <li>DaoAuthenticationProvider wired with email-based {@link EmailUserDetailsService}
 *       for the login flow</li>
 *   <li>Role hierarchy: ADMIN > PLACEMENT_OFFICER > FACULTY_MENTOR > STUDENT
 *       (EMPLOYER is a flat peer)</li>
 * </ul>
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitingFilter rateLimitingFilter;
    private final AuditLoggingFilter auditLoggingFilter;
    private final ObjectMapper objectMapper;

    /**
     * Used <em>only</em> for the login flow (email + password authentication).
     * JWT filter uses {@link com.placement.portal.security.UserDetailsServiceImpl} by id.
     */
    private final EmailUserDetailsService emailUserDetailsService;
    private final CorsConfigurationSource globalCorsConfigurationSource;

    // -----------------------------------------------------------------------
    // Security filter chain
    // -----------------------------------------------------------------------

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Stateless — no HTTP session
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Disable CSRF for REST APIs (protected via SameSite cookie + CORS)
                .csrf(AbstractHttpConfigurer::disable)

                // CORS
                .cors(cors -> cors.configurationSource(globalCorsConfigurationSource))

                // Custom 401 entry point
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint(authenticationEntryPoint()))

                // Route-level authorisation
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Public job and internship browsing endpoints
                        .requestMatchers(HttpMethod.GET, "/api/jobs", "/api/jobs/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/internships", "/api/internships/{id}").permitAll()
                        .anyRequest().authenticated()
                )

                // JWT filter runs before Spring's own username/password filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // Rate limiting runs before JWT so we can block replay spam early
                .addFilterBefore(rateLimitingFilter, JwtAuthenticationFilter.class)

                // Audit logging runs after the security chain (after response is committed)
                .addFilterAfter(auditLoggingFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    // -----------------------------------------------------------------------
    // Authentication entry point — returns 401 JSON instead of redirect
    // -----------------------------------------------------------------------

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new JwtAuthenticationEntryPoint(objectMapper);
    }

    /**
     * Returns HTTP 401 with a JSON body when an unauthenticated request hits a
     * protected resource.
     */
    private static class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

        private final ObjectMapper objectMapper;

        JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        public void commence(
                HttpServletRequest request,
                HttpServletResponse response,
                AuthenticationException authException
        ) throws IOException {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            Map<String, Object> body = Map.of(
                    "status", 401,
                    "error", "Unauthorized",
                    "message", "Authentication required"
            );
            response.getWriter().write(objectMapper.writeValueAsString(body));
        }
    }

    // -----------------------------------------------------------------------
    // Authentication provider — email-based for login
    // -----------------------------------------------------------------------

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(emailUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.authenticationProvider(daoAuthenticationProvider());
        return builder.build();
    }

    // -----------------------------------------------------------------------
    // Supporting beans
    // -----------------------------------------------------------------------

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Role hierarchy definition.
     *
     * <pre>
     * ROLE_ADMIN > ROLE_PLACEMENT_OFFICER > ROLE_FACULTY_MENTOR > ROLE_STUDENT
     * ROLE_EMPLOYER is a flat peer with no implied roles.
     * </pre>
     */
    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role("ADMIN").implies("PLACEMENT_OFFICER")
                .role("PLACEMENT_OFFICER").implies("FACULTY_MENTOR")
                .role("FACULTY_MENTOR").implies("STUDENT")
                .build();
    }
}
