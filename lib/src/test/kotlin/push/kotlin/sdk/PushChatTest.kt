package push.kotlin.sdk

import kotlin.test.Test
import kotlin.test.assertEquals

class PushChatTest {
    @Test fun resolvesIPFSHash() {
        val cid = "bafyreidhqkzwewbmuof7onnm3jzr3frgyzyxb7afyjjrprf2kcpfjb4hqq"

        val message = PushChat.resolveIpfs(cid, ENV.staging) ?: throw IllegalStateException("");

        assertEquals(message.fromDID, "eip155:0xD26A7BF7fa0f8F1f3f73B056c9A67565A6aFE63c")
        assertEquals(message.toDID, "eip155:0x03fAD591aEb926bFD95FE1E38D51811167a5ad5c")
    }

    @Test fun chatFeedTest() {
        val feedOptions = PushChat.GetChatsOptions(
    "0xD26A7BF7fa0f8F1f3f73B056c9A67565A6aFE63c",
            PGP_PK, false,1, 10, ENV.staging
        ) ?: throw  IllegalStateException("")

        val feeds = PushChat.getChats(feedOptions) ?: throw  IllegalStateException("")
        assertEquals(feeds.count(), 10)
    }

    @Test fun chatFeedTestDecrypt() {
        val feedOptions = PushChat.GetChatsOptions(
                "0xD26A7BF7fa0f8F1f3f73B056c9A67565A6aFE63c",
                PGP_PK, true,1, 3, ENV.staging
        ) ?: throw  IllegalStateException("")

        val feeds = PushChat.getChats(feedOptions) ?: throw  IllegalStateException("")
        feeds.forEach {feed -> assert(feed.msg.messageContent.isNotEmpty()) }
    }

    @Test fun chatMessageDecryptionTest() {
        val cid = "bafyreidhqkzwewbmuof7onnm3jzr3frgyzyxb7afyjjrprf2kcpfjb4hqq"
        val message = PushChat.resolveIpfs(cid, ENV.staging) ?: throw IllegalStateException("");
    }

    @Test fun chatRequestTest() {
        val feedOptions = PushChat.GetChatsOptions(
                "0xD26A7BF7fa0f8F1f3f73B056c9A67565A6aFE63c",
                PGP_PK, false,1, 10, ENV.staging
        ) ?: throw  IllegalStateException("")

        val feeds = PushChat.getChatRequests(feedOptions) ?: throw  IllegalStateException("")
        assertEquals(feeds.count(), 10)

        feeds.forEach { feed ->
            assert(feed.msg.messageContent.isNotEmpty())
        }
    }
    @Test fun chatRequestTestDecrypt() {
        val feedOptions = PushChat.GetChatsOptions(
                "0xD26A7BF7fa0f8F1f3f73B056c9A67565A6aFE63c",
                PGP_PK, true,1, 2, ENV.staging
        ) ?: throw  IllegalStateException("")

        val feeds = PushChat.getChatRequests(feedOptions) ?: throw  IllegalStateException("")
        feeds.forEach {feed -> assert(feed.msg.messageContent.isNotEmpty()) }
    }

    @Test fun latestChatHashTest() {
        val hash = PushChat.getConversationHash("0x03fAD591aEb926bFD95FE1E38D51811167a5ad5c","0xD26A7BF7fa0f8F1f3f73B056c9A67565A6aFE63c", ENV.staging) ?: throw IllegalStateException()
        assertEquals(hash.length, "bafyreidhqkzwewbmuof7onnm3jzr3frgyzyxb7afyjjrprf2kcpfjb4hqq".length)
    }

    @Test fun latestChatHashNullTest() {
        val hash = PushChat.getConversationHash("0x03fAD591aEb926bFD95FE1E38D51811167a5ad5d","0xD26A7BF7fa0f8F1f3f73B056c9A67565A6aFE63c", ENV.staging)
        assert(hash == null)
    }

    @Test fun latestMessageTest() {
        val hash = PushChat.getConversationHash("0x03fAD591aEb926bFD95FE1E38D51811167a5ad5c","0xD26A7BF7fa0f8F1f3f73B056c9A67565A6aFE63c", ENV.staging) ?: throw IllegalStateException()
        val message = PushChat.getLatestMessage(hash, PGP_PK, ENV.staging)
        assert(message.messageContent.isNotEmpty())
    }

    @Test fun conversationHistoryTest() {
        val hash = PushChat.getConversationHash("0x03fAD591aEb926bFD95FE1E38D51811167a5ad5c","0xD26A7BF7fa0f8F1f3f73B056c9A67565A6aFE63c", ENV.staging) ?: throw IllegalStateException()
        val messages = PushChat.getConversationHistory(hash, 5, PGP_PK, false, ENV.staging)

        messages.forEach { message -> assert((message.messageContent.isNotEmpty())) }
    }

    @Test fun conversationHistoryWithDecryptTest() {
        val hash = PushChat.getConversationHash("0x03fAD591aEb926bFD95FE1E38D51811167a5ad5c","0xD26A7BF7fa0f8F1f3f73B056c9A67565A6aFE63c", ENV.staging) ?: throw IllegalStateException()
        val messages = PushChat.getConversationHistory(hash, 5, PGP_PK, true, ENV.staging)

        messages.forEach { message -> assert(message.messageContent.isNotEmpty()) }
    }
}