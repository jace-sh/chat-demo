package jace.shim.chatdemo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ChatDemoApplication

fun main(args: Array<String>) {
    runApplication<ChatDemoApplication>(*args)
}
