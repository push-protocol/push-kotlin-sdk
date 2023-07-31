# restapi
This package gives access to Push Protocol (Push Nodes) APIs. Visit [Developer Docs](https://docs.push.org/developers) or [Push.org](https://push.org) to learn more.

-----
## Channels

-----

```kotlin
// imports
import push.kotlin.sdk.channels.Channel
import push.kotlin.sdk.channels.ChannelOpt
import push.kotlin.sdk.channels.ChannelSubscriber
import push.kotlin.sdk.channels.ChannelSearch
```


### Get Channel

```kotlin
  val channelAddress = "0x2AEcb6DeE3652dA1dD6b54D5fd4f7D8F43DaEb78"
  val channel:PushChannel? =  Channel.getChannel(channelAddress, ENV.staging)
```

-----

### Get Channels List
```kotlin
 val channels:PushChannels? = Channel.getAllChannels(page, limit, ENV.staging)
```

----

### Search Channel

```kotlin
val channels:PushChannels = ChannelSearch.searchChannels(
  query = "channel name or channel address",
  page = 1, 
  limit = 10, 
  order = "desc", 
  env = ENV.staging 
)
```

-----

### Get Subscribers
```kotlin
val subscribers:Result<ChannelSubscribers> = ChannelSubscriber.getSubscribers(
  channel = "0x2AEcb6DeE3652dA1dD6b54D5fd4f7D8F43DaEb78", 
  page = 5, 
  limit = 1, 
  env = ENV.staging
)
```

---

### Check if user Subscribed to a channel
```kotlin
val res:Result<Boolean> = ChannelSubscriber.IsSubscribed(
  userAddress = "0x5d73D70EB962083eDED53F03e2D4fA7d7773c4CE", 
  channelAddress = "0x2AEcb6DeE3652dA1dD6b54D5fd4f7D8F43DaEb78", 
  env = ENV.staging
)
```

---


### Subscribe/Unsubscribe

### Singer
To subscribe/unsubscribe `TypedSigner` needs to be implemented.

```kotlin
abstract class TypedSinger{
    abstract fun getEip712Signature(message: String): Result<String>
    abstract fun getAddress(): Result<String>
}
```

`message` string is message to sign containing type information, a domain separator, and data complying with WalletConnect `eth_signTypedData` rpc message format.

<details>
  <summary><b>for opt in `message` following string will be passed:</b></summary>

  ```json
  {
    "types":{
      "Subscribe":[
        {
          "name":"channel",
          "type":"address"
        },
        {
          "name":"subscriber",
          "type":"address"
        },
        {
          "name":"action",
          "type":"string"
        }
      ],
      "EIP712Domain":[
        {
          "name":"name",
          "type":"string"
        },
        {
          "name":"chainId",
          "type":"uint256"
        },
        {
          "name":"verifyingContract",
          "type":"address"
        }
      ]
    },
    "primaryType":"Subscribe",
    "domain":{
      "name":"EPNS COMM V1",
      "chainId":5,
      "verifyingContract":"0xb3971BCef2D791bc4027BbfedFb47319A4AAaaAa"
    },
    "message":{
      "channel":"Channel Address",
      "subscriber":"Subscriber Address",
      "action":"Subscribe"
    }
  }
  ```
</details>


### Subscribe
```kotlin
  var isSuccess:Result<Boolean> = ChannelOpt.subscribe(
    channel = "0x2AEcb6DeE3652dA1dD6b54D5fd4f7D8F43DaEb78", 
    subscriber = userAddress, 
    signer = optInSigner, 
    env = ENV.staging
  )
```


### Unubscribe
```kotlin
  var isSuccess:Result<Boolean> = ChannelOpt.unsubscribe(
    channel = "0x2AEcb6DeE3652dA1dD6b54D5fd4f7D8F43DaEb78", 
    subscriber = userAddress, 
    signer = optInSigner, 
    env = ENV.staging
  )
```