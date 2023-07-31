package push.kotlin.sdk.GroupTests

import BASE_64_IMAGE
import getNewSinger
import org.junit.jupiter.api.Test
import push.kotlin.sdk.*
import push.kotlin.sdk.ChatFunctions.ApproveOptions
import push.kotlin.sdk.ChatFunctions.ChatApprover
import push.kotlin.sdk.Group.PushGroup
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