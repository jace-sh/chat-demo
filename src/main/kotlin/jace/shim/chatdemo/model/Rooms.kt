package jace.shim.chatdemo.model

import jace.shim.chatdemo.application.actor.RoomActorMessage
import jace.shim.chatdemo.application.actor.roomActor
import kotlinx.coroutines.channels.SendChannel
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class Rooms {

    private val rooms: ConcurrentHashMap<Long, SendChannel<RoomActorMessage>> = ConcurrentHashMap()

    fun findOrCreate(chatId: Long, roomId: Long): SendChannel<RoomActorMessage> {
        return rooms[roomId] ?: createNewRoom(roomId)
    }

    private fun createNewRoom(roomId: Long): SendChannel<RoomActorMessage> {
        return roomActor(roomId).also { actor -> rooms[roomId] = actor }
    }
}