package push.kotlin.sdk.PushStreamTest

import push.kotlin.sdk.PushStream.PushStream
import kotlin.test.Test

class PushStreamTest {
    @Test
    fun emitterTest(){
        // Create an instance of PushStream
        val pushStream = PushStream()

        // Subscribe to "connected" event
        pushStream.onEvent("connected") { eventData ->
            println("Received 'connected' event with data: $eventData")
        }

        // Connect and emit "connected" event
        pushStream.connect()
    }
}