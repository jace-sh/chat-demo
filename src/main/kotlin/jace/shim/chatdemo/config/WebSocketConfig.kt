package jace.shim.chatdemo.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter

@Configuration
class WebSocketConfig(
    private val webSocketHandler: WebSocketHandler,
) {

    @Bean
    fun handlerMapping(): HandlerMapping {
        val mapping = SimpleUrlHandlerMapping()
        mapping.urlMap = mapOf(
            "/chats/**" to webSocketHandler
        )
        mapping.order = 10
        mapping.setCorsConfigurations(
            mapOf("*" to CorsConfiguration().applyPermitDefaultValues())
        )

        return mapping
    }

//    @Bean
//    fun handlerAdapter(): WebSocketHandlerAdapter {
//        return WebSocketHandlerAdapter()
//    }
}