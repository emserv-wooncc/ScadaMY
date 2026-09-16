package my.com.emserv.web.ws.config;

import my.com.emserv.web.ws.security.WebSocketAuthHandshakeHandler;
import my.com.emserv.web.ws.security.WebSocketAuthHandshakeInterceptor;
import my.com.emserv.web.ws.security.WebSocketSecurityChannelInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.util.UrlPathHelper;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        UrlPathHelper urlPathHelper = new UrlPathHelper();
        urlPathHelper.setAlwaysUseFullPath(true);
        registry.setUrlPathHelper(urlPathHelper);
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*")
                .addInterceptors(new WebSocketAuthHandshakeInterceptor())
                .setHandshakeHandler(new WebSocketAuthHandshakeHandler())
                .withSockJS()
                .setInterceptors(new WebSocketAuthHandshakeInterceptor());
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.setInterceptors(new WebSocketSecurityChannelInterceptor());
    }
}
