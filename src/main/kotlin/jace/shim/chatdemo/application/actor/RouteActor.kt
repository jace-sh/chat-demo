package jace.shim.chatdemo.application.actor

import com.fasterxml.jackson.module.kotlin.jsonMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.kotlin.core.publisher.toMono

fun routeActor(session: WebSocketSession) = CoroutineScope(Dispatchers.Default).actor<UserOutgoingMessage> {
    val jsonMapper = jsonMapper()

    for (msg in channel) {
        session.handshakeInfo.headers
        session.send(
            session.textMessage(jsonMapper.writeValueAsString(msg)).toMono()
        ).awaitSingleOrNull()
    }
}