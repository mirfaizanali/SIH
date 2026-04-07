package com.placement.portal.placement_portal_backend;

import com.placement.portal.security.jwt.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Entry point for the Campus Placement Portal backend.
 *
 * <p>{@link EnableConfigurationProperties} binds {@link JwtProperties} to the
 * {@code app.jwt.*} namespace in {@code application.yml}.</p>
 *
 * <p>{@link EnableMethodSecurity} activates {@code @PreAuthorize} / {@code @PostAuthorize}
 * annotations used for fine-grained method-level access control across services and
 * controllers.</p>
 *
 * <p>{@link EnableAsync} enables Spring's asynchronous method execution support, used
 * by the audit logging infrastructure to write log entries without blocking request
 * processing.</p>
 */
@SpringBootApplication(scanBasePackages = "com.placement.portal")
@EntityScan(basePackages = "com.placement.portal.domain")
@EnableJpaRepositories(basePackages = "com.placement.portal.repository")
@EnableConfigurationProperties(JwtProperties.class)
@EnableMethodSecurity(prePostEnabled = true)
@EnableAsync
public class PlacementPortalBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlacementPortalBackendApplication.class, args);
	}
}
