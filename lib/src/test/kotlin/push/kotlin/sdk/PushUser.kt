package push.kotlin.sdk

import getNewSinger
import getSignerFromPk
import push.kotlin.sdk.ProfileCreator.ProfileCreator
import kotlin.test.Test
import kotlin.test.assertEquals


class PushUserTest {
    @Test fun getUserTest() {
        val userAddress = "0xcA4D795c5e86E19Ef1B86948d7f5A3015dCE4a31"
        val privateKey = "182982e5fb82ed4ba2bc9ab7f436600cba14ac15bc4e7ec0c363fc9edd9209c0"
        
        val user = PushUser.getUser(userAddress, ENV.prod) ?: throw IllegalStateException("");
        val signer = getSignerFromPk(privateKey);
        val pgpPK = DecryptPgp.decryptPgpKey(user.encryptedPrivateKey, signer).getOrThrow()
        
        println(pgpPK)
        println("all good")
    }

    // @Test fun createUserTest() {
    //     val (address, signer) = getNewSinger()
    //     val user = PushUser.createUser(signer, ENV.staging).getOrThrow();

    //     assert(user.did.contains(address))
    // }

    // @Test fun createUserAndFetchTest() {
    //     val (address, signer) = getNewSinger()
    //     val user = PushUser.createUser(signer, ENV.staging).getOrThrow();

    //     val userGot = PushUser.getUser(address, ENV.staging) ?: throw  IllegalStateException("")

    //     assertEquals(user.did, userGot.did)
    // }

    // @Test fun creatingSameUserTwiceTest() {
    //     val (_, signer) = getNewSinger()
    //     PushUser.createUser(signer, ENV.staging).getOrThrow();

    //     val res = PushUser.createUser(signer, ENV.staging)
    //     assert(res.isFailure)

    // }

    // @Test fun createUserEmpty() {
    //     val (address, _) = getNewSinger()
    //     val res = ProfileCreator.createUserEmpty(address, ENV.staging)

    //     assert(res.isSuccess)
    //     assert(res.getOrThrow().did.contains(address))
    // }

    // @Test fun updateUserName(){
    //     val (userAddress, signer) = getNewSinger()
    //     val user = PushUser.createUser(signer, ENV.staging).getOrThrow();
    //     val pgpPK = DecryptPgp.decryptPgpKey(user.encryptedPrivateKey, signer).getOrThrow()

    //     user.profile.name = "Updated user $userAddress"
    //     PushUser.updateUser(userAddress, user.profile,pgpPK,ENV.staging).getOrThrow()

    //     val userAgain = PushUser.getUser(userAddress, ENV.staging) ?: throw  IllegalStateException("")

    //     assertEquals(userAgain.profile.name, "Updated user $userAddress")
    // }

    // @Test fun updateUserDesc(){
    //     val (userAddress, signer) = getNewSinger()
    //     val user = PushUser.createUser(signer, ENV.staging).getOrThrow();
    //     val pgpPK = DecryptPgp.decryptPgpKey(user.encryptedPrivateKey, signer).getOrThrow()

    //     user.profile.desc = "Updated user $userAddress"
    //     PushUser.updateUser(userAddress, user.profile,pgpPK,ENV.staging).getOrThrow()

    //     val userAgain = PushUser.getUser(userAddress, ENV.staging) ?: throw  IllegalStateException("")

    //     assertEquals(userAgain.profile.desc, "Updated user $userAddress")
    // }

    // @Test fun updateUserPic(){
    //     val (userAddress, signer) = getNewSinger()
    //     val user = PushUser.createUser(signer, ENV.staging).getOrThrow();
    //     val pgpPK = DecryptPgp.decryptPgpKey(user.encryptedPrivateKey, signer).getOrThrow()

    //     user.profile.picture = "Updated user $userAddress"
    //     PushUser.updateUser(userAddress, user.profile,pgpPK,ENV.staging).getOrThrow()

    //     val userAgain = PushUser.getUser(userAddress, ENV.staging) ?: throw  IllegalStateException("")

    //     assertEquals(userAgain.profile.picture, "Updated user $userAddress")
    // }

    // @Test fun userBlockUnBlock(){
    //     val (userAddress, signer) = getNewSinger()
    //     val user = PushUser.createUser(signer, ENV.staging).getOrThrow();
    //     val pgpPK = DecryptPgp.decryptPgpKey(user.encryptedPrivateKey, signer).getOrThrow()


    //     val (addrs1, _) = getNewSinger()
    //     val (addrs2, _) = getNewSinger()

    //     // Block tests
    //     PushUser.blockUser(userAddress, pgpPK, listOf(addrs1),ENV.staging).getOrThrow()
    //     var userAgain = PushUser.getUser(userAddress, ENV.staging) ?: throw  IllegalStateException("")
    //     assertEquals(userAgain.profile.blockedUsersList!!.size, 1)

    //     PushUser.blockUser(userAddress, pgpPK, listOf(addrs1),ENV.staging).getOrThrow()
    //     userAgain = PushUser.getUser(userAddress, ENV.staging) ?: throw  IllegalStateException("")
    //     assertEquals(userAgain.profile.blockedUsersList!!.size, 1)

    //     PushUser.blockUser(userAddress, pgpPK, listOf(addrs2),ENV.staging).getOrThrow()
    //     userAgain = PushUser.getUser(userAddress, ENV.staging) ?: throw  IllegalStateException("")
    //     assertEquals(userAgain.profile.blockedUsersList!!.size, 2)

    //     // Un Block tests
    //     PushUser.unblockUser(userAddress, pgpPK, listOf(addrs2),ENV.staging).getOrThrow()
    //     userAgain = PushUser.getUser(userAddress, ENV.staging) ?: throw  IllegalStateException("")
    //     assertEquals(userAgain.profile.blockedUsersList!!.size, 1)

    //     PushUser.unblockUser(userAddress, pgpPK, listOf(addrs2),ENV.staging).getOrThrow()
    //     userAgain = PushUser.getUser(userAddress, ENV.staging) ?: throw  IllegalStateException("")
    //     assertEquals(userAgain.profile.blockedUsersList!!.size, 1)

    //     PushUser.unblockUser(userAddress, pgpPK, listOf(addrs1),ENV.staging).getOrThrow()
    //     userAgain = PushUser.getUser(userAddress, ENV.staging) ?: throw  IllegalStateException("")
    //     assertEquals(userAgain.profile.blockedUsersList!!.size, 0)

    // }

}