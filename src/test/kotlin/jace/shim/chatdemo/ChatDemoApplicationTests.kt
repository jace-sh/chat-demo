package jace.shim.chatdemo

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

//@SpringBootTest
class ChatDemoApplicationTests {

    @Test
    fun contextLoads() {
    }

    @Test
    internal fun ctest() {
        runBlocking {
            val channels = Channel<Int>()
            launch {
                for (x in 1..5) channels.send(x)
                channels.close()
            }

            for (y in channels) println(y)

            println("Done~!")
        }

    }
}
