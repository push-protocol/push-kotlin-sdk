package push.kotlin.sdk.PushAPITest

import BASE_64_IMAGE
import getNewSinger
import push.kotlin.sdk.PushAPI.Chat
import push.kotlin.sdk.PushAPI.Group
import push.kotlin.sdk.PushAPI.PushAPI
import kotlin.test.Test
import kotlin.test.assertEquals

class ChatTest {
    @Test
    fun createGroup() {
        val (newAddress, signer) = getNewSinger()
        val (member1, _) = getNewSinger()
        val (member2, _) = getNewSinger()
        val pushAPI = PushAPI.initialize(signer)

        val name = "Push Kotlin"
        val options = Group.GroupCreationOptions(
                description = "Push Kotlin",
                image = BASE_64_IMAGE,
                admins = mutableListOf(member2),
                members = mutableListOf(member1)
        )
        val group = pushAPI.chat.group.create(name, options).getOrThrow()
        assertEquals(group.groupName, name)
    }

    @Test
    fun chatGroups() {
        val (aliceAddress, aliceSigner) = getNewSinger()
        val (bobAddress, bobSigner) = getNewSinger()
        val (johnAddress, johnSigner) = getNewSinger()
        val userAlice = PushAPI.initialize(aliceSigner)
        val userBob = PushAPI.initialize(bobSigner)
        val userJohn = PushAPI.initialize(johnSigner)

        val name = "Push Kotlin"
        val options = Group.GroupCreationOptions(
                description = "Push Kotlin",
                image = BASE_64_IMAGE,
                admins = mutableListOf(bobAddress),
                members = mutableListOf(johnAddress)
        )
        //Alice creates group
        val group = userAlice.chat.group.create(name, options).getOrThrow()
        val aliceChats = userAlice.chat.list(Chat.ChatListType.CHATS);
        assert(aliceChats?.size == 1)


        val bobRequests = userBob.chat.list(Chat.ChatListType.REQUESTS);
        assert(bobRequests?.size == 1)
        val bobChats = userBob.chat.list(Chat.ChatListType.CHATS);
        assert(bobChats?.size == 0)

        userBob.chat.accept(group.chatId).getOrThrow()

        val newBobChats = userBob.chat.list(Chat.ChatListType.CHATS);
        assert(newBobChats?.size == 1)

        val newBobRequests = userBob.chat.list(Chat.ChatListType.REQUESTS);
        assert(newBobRequests?.size == 0)

        userJohn.chat.reject(group.chatId)

        val johnRequests = userJohn.chat.list(Chat.ChatListType.REQUESTS);
        assert(johnRequests?.size == 0)

        val participants = userAlice.chat.group.participants.count(group.chatId);
        assert(participants.participants == 2)


        val message = "New message"
        userAlice.chat.send(group.chatId, message)
        val aliceLatest = userAlice.chat.latest(group.chatId)
        assertEquals(aliceLatest?.messageContent, message)
        val bobLatest = userBob.chat.latest(group.chatId)
        assertEquals(bobLatest?.messageContent, message)


    }

}