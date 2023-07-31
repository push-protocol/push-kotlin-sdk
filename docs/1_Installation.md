# Push Kotlin Sdk
This package gives access to Push Protocol (Push Nodes) APIs. Visit [Developer Docs](https://docs.push.org/developers) or [Push.org](https://push.org) to learn more.

-----
## Setting Up
In `buid.gradle` add:
```kotlin
repositories {
    mavenCentral()
    // new
    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    implementation("com.github.ethereum-push-notification-service:push-kotlin-sdk:SDK_VERSION")
}
```

Using the sdk
## Import the package as:
```kotlin
import push.kotlin.sdk
import push.kotlin.sdk.PushUser
import push.kotlin.sdk.ENV

fun main() {
    val PGP_PRIVATE_KEY = "PGP PRIVATE KEY ARMOR"
    val USER_ADDRESS = "USET_ETH_ADDRESS"
    val feeds = PushChat.getChats(
        PushChat.GetChatsOptions(
            USER_ADDRESS
            PGP_PRIVATE_KEY,
            true, 
            1,
            10,
            ENV.staging
        )
    ) ?: throw IllegalStateException()

    feeds.forEach { feed -> println(feed.msg.messageContent)  }
}
```