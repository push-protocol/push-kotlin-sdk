package push.kotlin.sdk.PushStream


class PushStream : EventEmitter<String>() {

    // Function to establish connection and emit "connected" event
    fun connect() {
        // Emit "connected" event with data as a map
        val eventData = mapOf("data" to "you are connected")
        emitEvent("connected", eventData)
    }
}