package push.kotlin.sdk.GroupTests

import BASE_64_IMAGE
import getNewSinger
import org.junit.jupiter.api.Test
import push.kotlin.sdk.*
import push.kotlin.sdk.ChatFunctions.ApproveOptions
import push.kotlin.sdk.ChatFunctions.ChatApprover
import push.kotlin.sdk.Group.PushGroup
import push.kotlin.sdk.PushAPI.PushAPI
import kotlin.test.assertEquals

class UpdateGroupTest {

  @Test
  fun updateGroupName(){
    val (newAddress, signer) = getNewSinger()
    val newUser = PushUser.createUser(signer, ENV.staging).getOrThrow()
    val pgpPK = DecryptPgp.decryptPgpKey(newUser.encryptedPrivateKey, signer).getOrThrow()

    val (member1,_) = getNewSinger()

    // create group
    val createOptions = PushGroup.CreateGroupOptionsV2(
            name = "$newAddress group",
            description = "group made my the user $newAddress for testing",
            image = BASE_64_IMAGE,
            members = mutableListOf(member1),
            creatorAddress = newAddress,
            isPublic = false,
            creatorPgpPrivateKey = pgpPK,
            env = ENV.staging,
            admins = mutableListOf(),
            config = PushGroup.GroupConfig(),
            rules = mapOf()
    )

    val group = PushGroup.createGroupV2(createOptions).getOrThrow()
    group.groupName = "update $newAddress"


    val updatedGroup = PushGroup.updateGroupProfile(
            PushGroup.UpdateGroupProfileOptions(
                    groupDescription = group.groupName,
                    groupName = group.groupName,
                    chatId = group.chatId,
                    groupImage = group.groupImage,
                    pgpPrivateKey = pgpPK,
                    account = newAddress
            ),
            env = ENV.staging,
    ).getOrThrow()
    assertEquals(updatedGroup.groupName, group.groupName)
  }

  @Test
  fun removeGroupMember(){
    val (newAddress, signer) = getNewSinger()
    val newUser = PushUser.createUser(signer, ENV.staging).getOrThrow()
    val pgpPK = DecryptPgp.decryptPgpKey(newUser.encryptedPrivateKey, signer).getOrThrow()


    val (member1,_) = getNewSinger()
    val (member2,_) = getNewSinger()
    val (member3,_) = getNewSinger()


    val createOptions = PushGroup.CreateGroupOptionsV2(
            name = "$newAddress group",
            description = "group made my the user $newAddress for testing",
            image = BASE_64_IMAGE,
            members = mutableListOf(member1, member2),
            creatorAddress = newAddress,
            isPublic = false,
            creatorPgpPrivateKey = pgpPK,
            env = ENV.staging,
            admins = mutableListOf(member3),
            config = PushGroup.GroupConfig(),
            rules = mapOf()
    )

    val group = PushGroup.createGroupV2(createOptions).getOrThrow()

    val options = PushGroup.UpdateGroupMemberOptions(
            account = newAddress,
            chatId = group.chatId,
            pgpPrivateKey = pgpPK,
            remove = listOf(member2),
            upsert = PushGroup.UpsertData()
    )
    val updatedGroup = PushGroup.updateGroupMember(options, ENV.staging).getOrThrow()

    assertEquals(updatedGroup.chatId, group.chatId)
  }

  @Test
  fun insertGroupMember(){

    val (newAddress, signer) = getNewSinger()
    val newUser = PushUser.createUser(signer, ENV.staging).getOrThrow()
    val pgpPK = DecryptPgp.decryptPgpKey(newUser.encryptedPrivateKey, signer).getOrThrow()


    val (member1,_) = getNewSinger()
    val (member2,_) = getNewSinger()
    val (member3,_) = getNewSinger()


    val createOptions = PushGroup.CreateGroupOptionsV2(
            name = "$newAddress group",
            description = "group made my the user $newAddress for testing",
            image = BASE_64_IMAGE,
            members = mutableListOf(member1),
            creatorAddress = newAddress,
            isPublic = false,
            creatorPgpPrivateKey = pgpPK,
            env = ENV.staging,
            admins = mutableListOf(),
            config = PushGroup.GroupConfig(),
            rules = mapOf()
    )

    val group = PushGroup.createGroupV2(createOptions).getOrThrow()

    val options = PushGroup.UpdateGroupMemberOptions(
            account = newAddress,
            chatId = group.chatId,
            pgpPrivateKey = pgpPK,
            upsert = PushGroup.UpsertData(
                    members = listOf(member2),
                    admins = listOf(member3)
            )
    )
    val updatedGroup = PushGroup.updateGroupMember(options, ENV.staging).getOrThrow()

    assertEquals(updatedGroup.chatId, group.chatId)
  }

  @Test
  fun updateGroupDes(){
    val (newAddress, signer) = getNewSinger()
    val newUser = PushUser.createUser(signer, ENV.staging).getOrThrow()
    val pgpPK = DecryptPgp.decryptPgpKey(newUser.encryptedPrivateKey, signer).getOrThrow()

    val (member1,_) = getNewSinger()

    // create group
    val createOptions = PushGroup.CreateGroupOptionsV2(
      name = "$newAddress group",
      description = "group made my the user $newAddress for testing",
      image = BASE_64_IMAGE,
      members = mutableListOf(member1),
      creatorAddress = newAddress,
      isPublic = false,
      creatorPgpPrivateKey = pgpPK,
            env = ENV.staging,
            admins = mutableListOf(),
            config = PushGroup.GroupConfig(),
            rules = mapOf()
    )

    val group = PushGroup.createGroupV2(createOptions).getOrThrow()
    val newDescription = "update $newAddress"


    val updatedGroup = PushGroup.updateGroupProfile(
            PushGroup.UpdateGroupProfileOptions(
                    groupDescription = newDescription,
                    groupName = group.groupName,
                    chatId = group.chatId,
                    groupImage = group.groupImage,
                    pgpPrivateKey = pgpPK,
                    account = newAddress
            ),
            env = ENV.staging,
    ).getOrThrow()
    assertEquals(updatedGroup.groupDescription, newDescription)
  }

  @Test
  fun updateImage(){
    val (newAddress, signer) = getNewSinger()
    val newUser = PushUser.createUser(signer, ENV.staging).getOrThrow()
    val pgpPK = DecryptPgp.decryptPgpKey(newUser.encryptedPrivateKey, signer).getOrThrow()

    val (member1,_) = getNewSinger()

    // create group
    val createOptions = PushGroup.CreateGroupOptionsV2(
            name = "$newAddress group",
            description = "group made my the user $newAddress for testing",
            image = BASE_64_IMAGE,
            members = mutableListOf(member1),
            creatorAddress = newAddress,
            isPublic = false,
            creatorPgpPrivateKey = pgpPK,
            env = ENV.staging,
            admins = mutableListOf(),
            config = PushGroup.GroupConfig(),
            rules = mapOf()
    )

    val group = PushGroup.createGroupV2(createOptions).getOrThrow()
    val newImage = "update $newAddress"


    val updatedGroup = PushGroup.updateGroupProfile(
            PushGroup.UpdateGroupProfileOptions(
                    groupDescription = group.groupDescription,
                    groupName = group.groupName,
                    chatId = group.chatId,
                    groupImage = newImage,
                    pgpPrivateKey = pgpPK,
                    account = newAddress
            ),
            env = ENV.staging,
    ).getOrThrow()
    assertEquals(updatedGroup.groupImage, newImage)
  }

  @Test
  fun leaveGroup(){
    val (newAddress, signer) = getNewSinger()
    val newUser = PushUser.createUser(signer, ENV.staging).getOrThrow()
    val pgpPK = DecryptPgp.decryptPgpKey(newUser.encryptedPrivateKey, signer).getOrThrow()


    val createOptions = PushGroup.CreateGroupOptionsV2(
            name = "$newAddress group",
            description = "group made my the user $newAddress for testing",
            image = BASE_64_IMAGE,
            members = mutableListOf(PGP_LINKED_ADDRESS),
            creatorAddress = newAddress,
            isPublic = false,
            creatorPgpPrivateKey = pgpPK,
            env = ENV.staging,
            admins = mutableListOf(),
            config = PushGroup.GroupConfig(),
            rules = mapOf()
    )

    val group = PushGroup.createGroupV2(createOptions).getOrThrow()

    ChatApprover(ApproveOptions(
            requesterAddress = group.chatId,
            approverAddress = PGP_LINKED_ADDRESS,
            pgpPrivateKey = PGP_PK,
            env = ENV.staging
    )).approve().getOrThrow()


    PushGroup.leaveGroup(group.chatId, PGP_LINKED_ADDRESS, PGP_PK, ENV.staging).getOrThrow()

    val members = PushGroup.getAllGroupMembers(chatId = group.chatId, env = ENV.staging)
    assertEquals(members.size, 1)
    assertEquals(members[0].address, Helpers.walletToPCAIP(newAddress))
  }

  @Test
  fun testGroupMember_(){

    val (newAddress, signer) = getNewSinger()

    val newUser = PushUser.createUser(signer, ENV.staging).getOrThrow()
    val pgpPK = DecryptPgp.decryptPgpKey(newUser.encryptedPrivateKey, signer).getOrThrow()

    val userBob = PushAPI.initialize(signer= signer)
   val status =  userBob.chat.group.participants.status(chartId = "4c40c821f8486069e401817dd69a54ff404c7d3cdd03a26998960e09d359c6b4", overrideAccount = "0x941aB2Bc7E26BF5Df6e1d4c5a0f12bc5012B922B")
println("Status $status")

    val (member1,_) = getNewSinger()
    val (member2,_) = getNewSinger()
    val (member3,_) = getNewSinger()


    val createOptions = PushGroup.CreateGroupOptionsV2(
            name = "$newAddress group",
            description = "group made my the user $newAddress for testing",
            image = BASE_64_IMAGE,
            members = mutableListOf(member1),
            creatorAddress = newAddress,
            isPublic = false,
            creatorPgpPrivateKey = pgpPK,
            env = ENV.staging,
            admins = mutableListOf(),
            config = PushGroup.GroupConfig(),
            rules = mapOf()
    )

    val group = PushGroup.createGroupV2(createOptions).getOrThrow()

    val options = PushGroup.UpdateGroupMemberOptions(
            account = newAddress,
            chatId = group.chatId,
            pgpPrivateKey = pgpPK,
            upsert = PushGroup.UpsertData(
                    members = listOf(member2),
                    admins = listOf(member3)
            )
    )
    val updatedGroup = PushGroup.updateGroupMember(options, ENV.staging).getOrThrow()

    assertEquals(updatedGroup.chatId, group.chatId)
  }
}