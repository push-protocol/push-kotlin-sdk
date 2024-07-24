package push.kotlin.sdk.PushStream
open class EventEmitter<T> {
  private val listeners = mutableMapOf<T, MutableList<(Map<String, Any?>) -> Unit>>()

  // Function to subscribe to events
  fun onEvent(eventType: T, listener: (Map<String, Any?>) -> Unit) {
    if (!listeners.containsKey(eventType)) {
      listeners[eventType] = mutableListOf()
    }
    listeners[eventType]?.add(listener)
  }

  // Function to emit events with dynamic data
  fun emitEvent(eventType: T, eventData: Map<String, Any?>) {
    listeners[eventType]?.forEach { listener ->
      listener(eventData)
    }
  }

  // Optional: Function to unsubscribe from events
  fun removeListener(eventType: T, listener: (Map<String, Any?>) -> Unit) {
    listeners[eventType]?.remove(listener)
  }
}