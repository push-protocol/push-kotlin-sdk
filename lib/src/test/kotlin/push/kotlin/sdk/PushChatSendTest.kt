package push.kotlin.sdk

import getNewSinger
import org.junit.jupiter.api.Test
import push.kotlin.sdk.ChatFunctions.ChatSender
import push.kotlin.sdk.ChatFunctions.SendOptions
import kotlin.test.assertEquals

class PushChatSendTest {
  @Test
  fun sendIntentTest() {
    val (newAddress, _) = getNewSinger()

    val senderOptions = SendOptions(
      messageContent = "Hello user $newAddress",
      messageType = "Text",
      receiverAddress = newAddress,
      senderAddress = PGP_LINKED_ADDRESS,
      senderPgpPrivateKey = PGP_PK,
      env = ENV.staging,
    )

    val res = ChatSender(senderOptions).sendIntent()
    assert(res.isSuccess)

    val requests = PushChat.getChatRequests(PushChat.GetChatsOptions(newAddress, "",false, 1, 10, ENV.staging)) ?: throw IllegalStateException()
    val msg = requests[0].msg.messageContent

    assertEquals(msg, "Hello user $newAddress")

  }
}