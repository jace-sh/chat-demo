package jace.shim.chatdemo.config

import jace.shim.chatdemo.application.actor.*
import jace.shim.chatdemo.model.Rooms
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.mono
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import java.net.URI
import java.net.URLDecoder


@Component
class WebSocketHandler(
    private val rooms: Rooms,
) : WebSocketHandler {
    override fun handle(session: WebSocketSession): Mono<Void> =
        mono {
            chatHandler(session)
        }.then()

    suspend fun chatHandler(session: WebSocketSession) {
        val chatId = getChatId(session.handshakeInfo.uri)
        val roomId = parseQueryString(session.handshakeInfo.uri)["roomId"]!!.toLong()
        val userId = parseQueryString(session.handshakeInfo.uri)["userId"]!!

        val roomActor = rooms.findOrCreate(chatId, roomId)
        val userActor = userActor(roomActor)
        val routeActor = routeActor(session)

        val connectedMsg = Connected(routeActor, userId)
        userActor.send(connectedMsg)

        session.receive()
            .log()
            .map { it.retain() }
            .asFlow()
//        .flowOn(Dispatchers.Default)
            .onCompletion { userActor.send(Completed) }
            .collect {
                val userIncomingMessage = UserIncomingMessage(userId, it.payloadAsText)
                userActor.send(userIncomingMessage)
            }
    }
}

private fun getChatId(url: URI): Long {
    return url.path.split("/").get(2).toLong()
}

private fun getToken(url: URI): String {
    return parseQueryString(url)["token"] ?: ""
}

private fun parseQueryString(url: URI): Map<String, String> {
    val queryPairs = mutableMapOf<String, String>()

    val query: String = url.query
    val pairs = query.split("&")

    val defaultEncoding = "UTF-8"
    for (pair in pairs) {
        val idx = pair.indexOf("=")
        queryPairs[URLDecoder.decode(pair.substring(0, idx), defaultEncoding)] =
            URLDecoder.decode(pair.substring(idx + 1), defaultEncoding)
    }

    return queryPairs
}
