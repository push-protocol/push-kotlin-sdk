# restapi
This package gives access to Push Protocol (Push Nodes) APIs. Visit [Developer Docs](https://docs.push.org/developers) or [Push.org](https://push.org) to learn more.

-----
## Channels

-----

### Get Channel

```kotlin
  val channelAddress = "0x2AEcb6DeE3652dA1dD6b54D5fd4f7D8F43DaEb78"
  val res =  Channel.getChannel(channelAddress, ENV.staging).getOrThrow()
```

-----

### Get Channels List
```kotlin
 val result = Channel.getAllChannels(page, limit, ENV.staging).getOrThrow()
```

----

### Search Channel

```kotlin
val query = "0x2AEcb6DeE3652dA1dD6b54D5fd4f7D8F43DaEb78"
val res = Search.searchChannels(ENV.staging, page, limit, type, "0x2AEcb6DeE3652dA1dD6b54D5fd4f7D8F43DaEb78").getOrThrow()
```

-----
### Get Subscribers
```kotlin
val channelAddress = "0x2AEcb6DeE3652dA1dD6b54D5fd4f7D8F43DaEb78"
val res = Subscribe.getSubscribers(channelAddress, ENV.staging,5, 1).getOrThrow()
```
-----
### Check if user Subscribed to a channel
```kotlin
val userAddress = "0x5d73D70EB962083eDED53F03e2D4fA7d7773c4CE"
val channelAddress = "0x2AEcb6DeE3652dA1dD6b54D5fd4f7D8F43DaEb78"
val res1 = Subscribe.IsSubscribed(userAddress , channelAddress, ENV.staging).getOrThrow()
```
