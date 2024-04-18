package push.kotlin.sdk.PushAPI

import push.kotlin.sdk.ChatFunctions.ApproveOptions
import push.kotlin.sdk.ChatFunctions.ChatApprover
import push.kotlin.sdk.ChatFunctions.ChatSender
import push.kotlin.sdk.ChatFunctions.SendOptions
import push.kotlin.sdk.ENV
import push.kotlin.sdk.PushChat
import push.kotlin.sdk.PushUser
import push.kotlin.sdk.Signer

class Chat(
        private val account: String,
        val env: ENV,
        private val decryptedPgpPvtKey: String,
        private val signer: Signer,
) {
    var group: Group

    init {
        group = Group(account, env, decryptedPgpPvtKey, signer)
    }

    enum class ChatListType {
        CHATS,
        REQUESTS
    }

    @Throws(IllegalArgumentException::class)
    fun list(
            type: ChatListType,
            page: Int? = null,
            limit: Int? = null,
            overrideAccount: String? = null
    ): Array<PushChat.Feed>? {
        val accountToUse: String = overrideAccount ?: account
        val option = PushChat.GetChatsOptions(
                accountToUse,
                decryptedPgpPvtKey,
                true,
                page ?: 1,
                limit ?: 10,
                env
        )

        return when (type) {
            ChatListType.CHATS -> {
                PushChat.getChats(option)
            }

            ChatListType.REQUESTS -> {
                PushChat.getChatRequests(option)
            }

        }
    }

    fun latest(target: String): PushChat.Message? {
        val threadHash = PushChat.getConversationHash(target, account, env) ?: return null
        return PushChat.getLatestMessage(threadHash, decryptedPgpPvtKey, env)
    }

    fun history(target: String, reference: String? = null, limit: Int = 10): Array<PushChat.Message> {
        var ref = reference;
        if (reference == null) {
            val threadHash = PushChat.getConversationHash(target, account, env)
            ref = threadHash
        }
        if (ref == null) {
            return emptyArray()
        }

        return PushChat.getConversationHistory(
                ref,
                limit,
                decryptedPgpPvtKey,
                true,
                env
        )
    }

    fun accept(target: String): Result<String> {
        return ChatApprover(ApproveOptions(target, account, decryptedPgpPvtKey, env)).approve()
    }

    fun reject(target: String): Result<String> {
        return ChatApprover(ApproveOptions(target, account, decryptedPgpPvtKey, env)).reject()
    }

    fun block(users: List<String>): Result<Boolean> {
        return PushUser.blockUser(account, decryptedPgpPvtKey, users, env)
    }

    fun unblock(users: List<String>): Result<Boolean> {
        return PushUser.unblockUser(account, decryptedPgpPvtKey, users, env)
    }

    fun send(target: String, messageContent: String, messageType: String = "Text"): Result<PushChat.Message> {
        val options = SendOptions(
                messageType = messageType,
                messageContent = messageContent,
                senderAddress = account,
                receiverAddress = target,
                senderPgpPrivateKey = decryptedPgpPvtKey,
                env = env
        )
        return ChatSender(options).send()
    }
}