package com.placement.portal.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.codec.ClientCodecConfigurer;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Reactive {@link WebClient} beans for external HTTP integrations.
 */
@Configuration
public class WebClientConfig {

    /**
     * Pre-configured {@link WebClient} for the transactional email provider.
     *
     * <ul>
     *   <li>Base URL: {@code app.email.provider-url}</li>
     *   <li>Default header: {@code Authorization: Bearer <api-key>}</li>
     *   <li>Max in-memory buffer: 1 MB (prevents OOM on large response bodies)</li>
     * </ul>
     *
     * @param providerUrl the base URL of the email service (e.g. SendGrid, Resend)
     * @param apiKey      the API key for authenticating with the email provider
     * @return a fully configured {@link WebClient} instance
     */
    @Bean
    public WebClient emailWebClient(
            @Value("${app.email.provider-url}") String providerUrl,
            @Value("${app.email.api-key}") String apiKey
    ) {
        return WebClient.builder()
                .baseUrl(providerUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .codecs(this::configureCodecs)
                .build();
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private void configureCodecs(ClientCodecConfigurer configurer) {
        // Allow up to 1 MB in-memory buffering for response bodies.
        configurer.defaultCodecs().maxInMemorySize(1024 * 1024);
    }
}
