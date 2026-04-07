package com.placement.portal.config;

import com.placement.portal.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

/**
 * STOMP/WebSocket configuration for real-time notifications and chat.
 *
 * <p>Endpoint: {@code /ws} (SockJS fallback enabled)</p>
 * <p>Simple in-memory broker on {@code /topic} (broadcast) and {@code /queue} (point-to-point).</p>
 * <p>Application messages are prefixed with {@code /app}.</p>
 * <p>User-specific destinations are prefixed with {@code /user}.</p>
 *
 * <p>JWT is validated on every STOMP {@code CONNECT} frame before the connection
 * is accepted.</p>
 */
@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.websocket.allowed-origins}")
    private String allowedOrigins;

    // -----------------------------------------------------------------------
    // Endpoint registration
    // -----------------------------------------------------------------------

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins(allowedOrigins.split(","))
                .withSockJS();
    }

    // -----------------------------------------------------------------------
    // Message broker
    // -----------------------------------------------------------------------

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    // -----------------------------------------------------------------------
    // Inbound channel — JWT validation on CONNECT
    // -----------------------------------------------------------------------

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {

            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor == null) {
                    return message;
                }

                // Only validate on CONNECT frames; subsequent frames inherit the user.
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String authHeader = accessor.getFirstNativeHeader("Authorization");

                    if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);

                        if (jwtTokenProvider.validateToken(token)) {
                            String userId = jwtTokenProvider.getUserIdFromToken(token);
                            String role = jwtTokenProvider.getRoleFromToken(token);

                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(
                                            userId,
                                            null,
                                            List.of(new SimpleGrantedAuthority("ROLE_" + role))
                                    );

                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            accessor.setUser(authentication);

                            log.debug("WebSocket CONNECT authenticated for userId={}", userId);
                        } else {
                            log.warn("WebSocket CONNECT rejected — invalid JWT");
                            // Returning null rejects the message / disconnects the client.
                            return null;
                        }
                    } else {
                        log.warn("WebSocket CONNECT rejected — missing Authorization header");
                        return null;
                    }
                }

                return message;
            }
        });
    }
}
