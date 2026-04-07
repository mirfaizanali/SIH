package com.placement.portal.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Sends transactional emails via the configured email provider (SendGrid v3 API).
 *
 * <p>Two delivery modes are provided:
 * <ul>
 *   <li>{@link #sendAsync} — fire-and-forget; never blocks the calling thread</li>
 *   <li>{@link #sendBlocking} — waits up to 5 s for the provider to acknowledge</li>
 * </ul>
 *
 * <p>Both modes swallow errors after logging so that a failed email never
 * propagates an exception to the caller.</p>
 *
 * <p>The {@code emailWebClient} bean is defined in
 * {@link com.placement.portal.config.WebClientConfig} and is pre-configured
 * with the provider base URL and API-key header.</p>
 */
@Slf4j
@Service
public class EmailNotificationService {

    private final WebClient emailWebClient;
    private final String fromEmail;
    private final String fromName;

    // -----------------------------------------------------------------------
    // Constructor injection
    // -----------------------------------------------------------------------

    public EmailNotificationService(
            @Qualifier("emailWebClient") WebClient emailWebClient,
            @Value("${app.email.from-address}") String fromEmail,
            @Value("${app.email.from-name}") String fromName
    ) {
        this.emailWebClient = emailWebClient;
        this.fromEmail = fromEmail;
        this.fromName = fromName;
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Sends an email asynchronously (fire-and-forget).
     *
     * <p>The calling thread is never blocked. Any provider error is caught by
     * the reactive pipeline and logged at WARN level.</p>
     *
     * @param toEmail   recipient email address
     * @param toName    recipient display name
     * @param subject   email subject line
     * @param htmlBody  HTML content of the email body
     */
    public void sendAsync(String toEmail, String toName, String subject, String htmlBody) {
        log.debug("Queuing async email to={} subject={}", toEmail, subject);
        buildRequest(toEmail, toName, subject, htmlBody)
                .subscribe(
                        response -> log.debug("Email sent successfully to={}", toEmail),
                        error -> log.warn(
                                "Failed to send email to={} subject='{}': {}",
                                toEmail, subject, error.getMessage())
                );
    }

    /**
     * Sends an email and blocks until the provider acknowledges (max 5 seconds).
     *
     * <p>Any provider error or timeout is caught and logged; it is never
     * re-thrown to the caller.</p>
     *
     * @param toEmail   recipient email address
     * @param toName    recipient display name
     * @param subject   email subject line
     * @param htmlBody  HTML content of the email body
     */
    public void sendBlocking(String toEmail, String toName, String subject, String htmlBody) {
        log.debug("Sending blocking email to={} subject={}", toEmail, subject);
        try {
            buildRequest(toEmail, toName, subject, htmlBody)
                    .block(Duration.ofSeconds(5));
            log.debug("Blocking email sent successfully to={}", toEmail);
        } catch (Exception ex) {
            log.warn("Failed to send blocking email to={} subject='{}': {}",
                    toEmail, subject, ex.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    /**
     * Constructs the reactive request pipeline without subscribing.
     * Callers choose whether to subscribe asynchronously or block.
     */
    private Mono<Void> buildRequest(String toEmail, String toName,
                                    String subject, String htmlBody) {
        Map<String, Object> payload = buildPayload(toEmail, toName, subject, htmlBody);
        return emailWebClient.post()
                .uri("/v3/mail/send")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .onStatus(
                        status -> status.isError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException(
                                        "Email provider returned " + clientResponse.statusCode()
                                        + ": " + body)))
                )
                .toBodilessEntity()
                .then();
    }

    /**
     * Builds the SendGrid v3 {@code /mail/send} JSON payload.
     *
     * <pre>
     * {
     *   "personalizations": [{"to": [{"email":"...","name":"..."}]}],
     *   "from": {"email":"...","name":"..."},
     *   "subject": "...",
     *   "content": [{"type":"text/html","value":"..."}]
     * }
     * </pre>
     *
     * @param toEmail  recipient email address
     * @param toName   recipient display name
     * @param subject  email subject line
     * @param htmlBody HTML content
     * @return a {@link Map} that Jackson will serialise to the expected JSON structure
     */
    private Map<String, Object> buildPayload(String toEmail, String toName,
                                             String subject, String htmlBody) {
        Map<String, String> toAddress = Map.of(
                "email", toEmail,
                "name", toName != null ? toName : ""
        );

        Map<String, Object> personalization = Map.of(
                "to", List.of(toAddress)
        );

        Map<String, String> fromAddress = Map.of(
                "email", fromEmail,
                "name", fromName != null ? fromName : ""
        );

        Map<String, String> contentEntry = Map.of(
                "type", "text/html",
                "value", htmlBody != null ? htmlBody : ""
        );

        return Map.of(
                "personalizations", List.of(personalization),
                "from", fromAddress,
                "subject", subject != null ? subject : "",
                "content", List.of(contentEntry)
        );
    }
}
