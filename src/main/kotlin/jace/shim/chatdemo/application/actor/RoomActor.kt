package jace.shim.chatdemo.application.actor

import jace.shim.webfluxfun.domain.idgenerate.number.NumberIdentifierGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap


sealed class RoomActorMessage

data class JoinRoom(val userId: String, val channel: SendChannel<UserActorMessage>) : RoomActorMessage()
data class LeftRoom(val userId: String) : RoomActorMessage()
data class IncomingMessage(val userId: String, val message: String) : RoomActorMessage()

fun roomActor(roomId: Long) = CoroutineScope(Dispatchers.Default).actor<RoomActorMessage> {
    val log = LoggerFactory.getLogger("roomActorLogger")

    val users = ConcurrentHashMap<String, SendChannel<UserActorMessage>>()

    suspend fun broadCast(outgoingMessage: UserOutgoingMessage) {
        users.forEach {
            it.value.send(outgoingMessage)
        }
//        users.filter { it.key != outgoingMessage.fromUserId }.forEach {
//            it.value.send(outgoingMessage)
//        }

        """
            {
            "type": "giftMessage",
            "data": {
                "message": ""
                }
            }
        """.trimIndent()
    }

    for (msg in channel) {
        when (msg) {
            is JoinRoom -> {
                users[msg.userId] = msg.channel
                broadCast(UserOutgoingMessage("admin", "${msg.userId} joined!"))
                log.info("${msg.userId} joined root $roomId, current user list : ${users.keys}")
            }
            is LeftRoom -> {
                users.remove(msg.userId)
                broadCast(UserOutgoingMessage("admin", "${msg.userId} left!"))
                log.info("${msg.userId} left room $roomId, current user list: ${users.keys}")
            }
            is IncomingMessage -> {
                broadCast(UserOutgoingMessage(msg.userId, msg.message))
                log.info("${msg.userId} send message: ${msg.message}")
            }
        }
    }
}
