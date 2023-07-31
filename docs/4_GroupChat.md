# restapi
This package gives access to Push Protocol (Push Nodes) APIs. Visit [Developer Docs](https://docs.push.org/developers) or [Push.org](https://push.org) to learn more.

-----
## For Group Chat

-----

### ***Create Group***
```kotlin
import push.kotlin.sdk.Group.PushGroup


val createOptions = PushGroup.CreateGroupOptions(
    name = "GROUP NAME",
    description = "GROUP DESCRIPTION",
    image = GROUP_BASE_64_IMAGE,
    members = listOf(MEMBER_1_ETH_ADDRESS, MEMBER_2_ETH_ADDRESS),
    creatorAddress = GROUP_CREATOR_ETH_ADDRESS,
    isPublic = false,
    creatorPgpPrivateKey = GROUP_CREATOR_PGP_PRIVATE_KEY
    env = ENV.staging
)

val group:Result<PushGroupProfile> = PushGroup.createGroup(createOptions)
```

---

### ***Get Group***
```kotlin
import push.kotlin.sdk.Group.PushGroup

val group:PushGroupProfile? = PushGroup.getGroup(GROUP_CHAT_ID, ENV.staging)
```

---

### **Join Group**
```kotlin
import push.kotlin.sdk.Group.PushGroup
import push.kotlin.sdk.ChatFunctions.ApproveOptions
import push.kotlin.sdk.ChatFunctions.ChatApprover

val approved:Result<String> = ChatApprover(ApproveOptions(
    requesterAddress = GROUP_CHAT_ID,
    approverAddress = USER_ETH_ADDRESS,
    pgpPrivateKey = USER_PGP_PK,
    env = ENV.staging
)).approve()
```

---

### **Send Group Message**
```kotlin
import push.kotlin.sdk.ChatFunctions.SendOptions
import push.kotlin.sdk.ChatFunctions.ChatSender

val senderOptions = SendOptions(
    messageContent = "Hello Group",
    messageType = "Text",
    receiverAddress = GROUP_CHAT_ID,
    senderAddress = SENDER_ETH_ADDRESS,
    senderPgpPrivateKey = SENDER_PGP_PRIVATE_KEY,
    env = ENV.staging,
)

val sentMessage:Result<PushChat.Message> = ChatSender(senderOptions).sendChat()
```

----

### **Update Group**

* only group admin can update the grouo

```kotlin
import push.kotlin.sdk.Group.PushGroup


// get group
val group:PushGroupProfile = PushGroup.getGroup(GROUP_CHAT_ID, ENV.staging)

// update the group
group.groupName = "New group name"
group.groupDescription = "New group desc"
group.groupImage = "New image base64"


val updatedGroup:Result<PushGroup.PushGroupProfile> = PushGroup.updateGroup(
    GROUP_CHAT_ID, 
    GROUP_ADMIN_ETH_ADDRESS, 
    GROUP_ADMIN_PGP_PRIVATE_KEY, 
    ENV.staging
)
```

----

### **Leave Group**

```kotlin
import push.kotlin.sdk.Group.PushGroup

PushGroup.leaveGroup(
    GROUP_CHAT_ID, 
    USER_ETH_ADDRESS, 
    PGP_PRIVATE_KEY, 
    ENV.staging
).
```