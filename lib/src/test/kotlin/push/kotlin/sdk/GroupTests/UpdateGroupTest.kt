package push.kotlin.sdk.GroupTests

import BASE_64_IMAGE
import com.google.gson.Gson
import getNewSinger
import getSingerWithKey
import org.junit.jupiter.api.Test
import push.kotlin.sdk.*
import push.kotlin.sdk.ChatFunctions.ApproveOptions
import push.kotlin.sdk.ChatFunctions.ChatApprover
import push.kotlin.sdk.Group.PushGroup
import push.kotlin.sdk.HahHelper.GenerateSHA256Hash
import kotlin.test.assertEquals

class UpdateGroupTest {

  @Test
  fun updateGroupName(){
    val (newAddress, signer) = getNewSinger()
    val newUser = PushUser.createUser(signer, ENV.staging).getOrThrow()
    val pgpPK = DecryptPgp.decryptPgpKey(newUser.encryptedPrivateKey, signer).getOrThrow()

    val (member1,_) = getNewSinger()

    // create group
    val createOptions = PushGroup.CreateGroupOptions(
      name = "$newAddress group",
      description = "group made my the user $newAddress for testing",
      image = BASE_64_IMAGE,
      members = mutableListOf(member1),
      creatorAddress = newAddress,
      isPublic = false,
      creatorPgpPrivateKey = pgpPK,
      env = ENV.staging
    )

    val group = PushGroup.createGroup(createOptions).getOrThrow()
    group.groupName = "update $newAddress"

    val updatedGroup = PushGroup.updateGroup(group, newAddress, pgpPK, ENV.staging).getOrThrow()
    assertEquals(updatedGroup.groupName, group.groupName)
  }

  @Test
  fun updateGroupNameV2(){
    val (newAddress, signer) = getNewSinger()
    val newUser = PushUser.createUser(signer, ENV.staging).getOrThrow()
    val pgpPK = DecryptPgp.decryptPgpKey(newUser.encryptedPrivateKey, signer).getOrThrow()

    val (member1,_) = getNewSinger()

    // create group
    val createOptions = PushGroup.CreateGroupOptions(
            name = "$newAddress group",
            description = "group made my the user $newAddress for testing",
            image = BASE_64_IMAGE,
            members = mutableListOf(member1),
            creatorAddress = newAddress,
            isPublic = false,
            creatorPgpPrivateKey = pgpPK,
            env = ENV.staging
    )

    val group = PushGroup.createGroup(createOptions).getOrThrow()
    println(Gson().toJson(group))
    val newName = "update 23"

    val options = PushGroup.UpdateGroupProfileOptions(
            groupName = newName,
            chatId = group.chatId,
            pgpPrivateKey = pgpPK,
            account = newAddress,
            groupDescription = group.groupDescription,
            groupImage = group.groupImage
    )
    val updatedGroup = PushGroup.updateGroupProfile(options, ENV.staging).getOrThrow()
    println(Gson().toJson(updatedGroup))
    assertEquals(updatedGroup.groupName, newName)
  }

  @Test
  fun updateGroupNameV2_(){
    val (newAddress, signer) = getSingerWithKey("c41b72d56258e50595baa969eb0949c5cee9926ac55f7bad21fe327236772e0c")
    val newUser = PushUser.createUser(signer, ENV.staging).getOrThrow()
    val pgpPK = DecryptPgp.decryptPgpKey(newUser.encryptedPrivateKey, signer).getOrThrow()

    val (member1,_) = getNewSinger()

    println("creator address: $newAddress")
    val newName = "update 23"

    val group = PushGroup.getGroup("8053d0f8d9d0275f22ce881bb3859af029dfad8647fe616f4d2e0787521607c9",ENV.staging )
    val options = PushGroup.UpdateGroupProfileOptions(
            groupName = newName,
            chatId = group?.chatId!!,
            pgpPrivateKey = pgpPK,
            account = newAddress,
            groupDescription = group.groupDescription,
            groupImage = group.groupImage
    )
    val updatedGroup = PushGroup.updateGroupProfile(options, ENV.staging).getOrThrow()
    assertEquals(updatedGroup.groupName, newName)
  }
  @Test
  fun hashTest(){
    val result = GenerateSHA256Hash("table")
    println("result: $result")
  }

  @Test
  fun updateGroupDes(){
    val (newAddress, signer) = getNewSinger()
    val newUser = PushUser.createUser(signer, ENV.staging).getOrThrow()
    val pgpPK = DecryptPgp.decryptPgpKey(newUser.encryptedPrivateKey, signer).getOrThrow()

    val (member1,_) = getNewSinger()

    // create group
    val createOptions = PushGroup.CreateGroupOptions(
      name = "$newAddress group",
      description = "group made my the user $newAddress for testing",
      image = BASE_64_IMAGE,
      members = mutableListOf(member1),
      creatorAddress = newAddress,
      isPublic = false,
      creatorPgpPrivateKey = pgpPK,
      env = ENV.staging
    )

    val group = PushGroup.createGroup(createOptions).getOrThrow()
    group.groupDescription = "update $newAddress"

    val updatedGroup = PushGroup.updateGroup(group, newAddress, pgpPK, ENV.staging).getOrThrow()
    assertEquals(updatedGroup.groupDescription, group.groupDescription)
  }

  @Test
  fun updateImage(){
    val (newAddress, signer) = getNewSinger()
    val newUser = PushUser.createUser(signer, ENV.staging).getOrThrow()
    val pgpPK = DecryptPgp.decryptPgpKey(newUser.encryptedPrivateKey, signer).getOrThrow()

    val (member1,_) = getNewSinger()

    // create group
    val createOptions = PushGroup.CreateGroupOptions(
            name = "$newAddress group",
            description = "group made my the user $newAddress for testing",
            image = BASE_64_IMAGE,
            members = mutableListOf(member1),
            creatorAddress = newAddress,
            isPublic = false,
            creatorPgpPrivateKey = pgpPK,
            env = ENV.staging
    )

    val group = PushGroup.createGroup(createOptions).getOrThrow()
    group.groupImage = "update $newAddress"

    val updatedGroup = PushGroup.updateGroup(group, newAddress, pgpPK, ENV.staging).getOrThrow()
    assertEquals(updatedGroup.groupImage, group.groupImage)
  }

  @Test
  fun leaveGroup(){
    val (newAddress, signer) = getNewSinger()
    val newUser = PushUser.createUser(signer, ENV.staging).getOrThrow()
    val pgpPK = DecryptPgp.decryptPgpKey(newUser.encryptedPrivateKey, signer).getOrThrow()

    val (member1,_) = getNewSinger()

    val createOptions = PushGroup.CreateGroupOptions(
            name = "$newAddress group",
            description = "group made my the user $newAddress for testing",
            image = BASE_64_IMAGE,
            members = mutableListOf(member1, PGP_LINKED_ADDRESS),
            creatorAddress = newAddress,
            isPublic = false,
            creatorPgpPrivateKey = pgpPK,
            env = ENV.staging
    )

    val group = PushGroup.createGroup(createOptions).getOrThrow()

    ChatApprover(ApproveOptions(
            requesterAddress = group.chatId,
            approverAddress = PGP_LINKED_ADDRESS,
            pgpPrivateKey = PGP_PK,
            env = ENV.staging
    )).approve().getOrThrow()


    val newGroup = PushGroup.leaveGroup(group.chatId, PGP_LINKED_ADDRESS, PGP_PK, ENV.staging).getOrThrow()

    assertEquals(newGroup.members.size ,1)
    assertEquals(newGroup.members[0].wallet, Helpers.walletToPCAIP(newAddress))
  }
}