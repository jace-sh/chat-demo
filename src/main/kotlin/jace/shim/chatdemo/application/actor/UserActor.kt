package jace.shim.chatdemo.application.actor

import jace.shim.webfluxfun.domain.idgenerate.number.NumberIdentifierGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import org.springframework.stereotype.Component


sealed class UserActorMessage

class Connected(val routeActor: SendChannel<UserOutgoingMessage>, val userId: String) : UserActorMessage()
//class Unauthorized(val token: String) : UserActorMessage()
object Completed : UserActorMessage()
data class UserIncomingMessage(val userId: String, val message: String) : UserActorMessage()
data class UserOutgoingMessage(val userId: String, val message: String) : UserActorMessage()

fun userActor(roomActor: SendChannel<RoomActorMessage>) = CoroutineScope(Dispatchers.Default).actor<UserActorMessage> {
    lateinit var routeActor: SendChannel<UserOutgoingMessage>
    lateinit var userId: String
    val roomActor = roomActor

    for (msg in channel) {
        when (msg) {
            is Connected -> {
                roomActor.send(JoinRoom(msg.userId, this.channel))
                routeActor = msg.routeActor
                userId = msg.userId
            }
            is Completed -> {
                roomActor.send(LeftRoom(userId))
            }
            is UserIncomingMessage -> {
                roomActor.send(IncomingMessage(msg.userId, msg.message))
            }
            is UserOutgoingMessage -> {
                routeActor.send(msg)
            }
        }
    }
}