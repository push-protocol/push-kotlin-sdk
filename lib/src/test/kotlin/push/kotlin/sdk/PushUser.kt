package push.kotlin.sdk

import getNewSinger
import getSingerWithKey
import push.kotlin.sdk.ProfileCreator.ProfileCreator
import kotlin.test.Test
import kotlin.test.assertEquals


class PushUserTest {
    @Test fun getUserTest() {
        val userAddress = "0xD26A7BF7fa0f8F1f3f73B056c9A67565A6aFE63c"
        val user = PushUser.getUser(userAddress, ENV.staging) ?: throw IllegalStateException("");
        assertEquals(user.did, "eip155:$userAddress")
    }

    @Test fun createUserTest() {
        val (address, signer) = getNewSinger()
        val user = PushUser.createUser(signer, ENV.staging).getOrThrow();

        assert(user.did.contains(address))
    }

    @Test fun createUserAndFetchTest() {
        val (address, signer) = getNewSinger()
        val user = PushUser.createUser(signer, ENV.staging).getOrThrow();

        val userGot = PushUser.getUser(address, ENV.staging) ?: throw  IllegalStateException("")

        assertEquals(user.did, userGot.did)
    }

    @Test fun creatingSameUserTwiceTest() {
        val (_, signer) = getNewSinger()
        PushUser.createUser(signer, ENV.staging).getOrThrow();

        val res = PushUser.createUser(signer, ENV.staging)
        assert(res.isFailure)

    }

    @Test fun createUserEmpty() {
        val (address, _) = getNewSinger()
        val res = ProfileCreator.createUserEmpty(address, ENV.staging)

        assert(res.isSuccess)
        assert(res.getOrThrow().did.contains(address))
    }

    @Test fun updateUserName(){
        val (userAddress, signer) = getNewSinger()
        val user = PushUser.createUser(signer, ENV.staging).getOrThrow();
        val pgpPK = DecryptPgp.decryptPgpKey(user.encryptedPrivateKey, signer).getOrThrow()

        user.profile.name = "Updated user $userAddress"
        PushUser.updateUser(userAddress, user.profile,pgpPK,ENV.staging).getOrThrow()

        val userAgain = PushUser.getUser(userAddress, ENV.staging) ?: throw  IllegalStateException("")

        assertEquals(userAgain.profile.name, "Updated user $userAddress")
    }

    @Test fun updateUserDesc(){
        val (userAddress, signer) = getNewSinger()
        val user = PushUser.createUser(signer, ENV.staging).getOrThrow();
        val pgpPK = DecryptPgp.decryptPgpKey(user.encryptedPrivateKey, signer).getOrThrow()

        user.profile.desc = "Updated user $userAddress"
        PushUser.updateUser(userAddress, user.profile,pgpPK,ENV.staging).getOrThrow()

        val userAgain = PushUser.getUser(userAddress, ENV.staging) ?: throw  IllegalStateException("")

        assertEquals(userAgain.profile.desc, "Updated user $userAddress")
    }

    @Test fun updateUserPic(){
        val (userAddress, signer) = getNewSinger()
        val user = PushUser.createUser(signer, ENV.staging).getOrThrow();
        val pgpPK = DecryptPgp.decryptPgpKey(user.encryptedPrivateKey, signer).getOrThrow()

        user.profile.picture = "Updated user $userAddress"
        PushUser.updateUser(userAddress, user.profile,pgpPK,ENV.staging).getOrThrow()

        val userAgain = PushUser.getUser(userAddress, ENV.staging) ?: throw  IllegalStateException("")

        assertEquals(userAgain.profile.picture, "Updated user $userAddress")
    }

    @Test
    fun updateUserUpdate() {
        val (userAddress, signer) = getNewSinger()
//        val (userAddress, signer) = //getSingerWithKey("e2d9d13fa1f37e786596ac57f6be8beb3b9e388e9a5f28d7f69a787b9360e29e")
        val user = PushUser.createUser(signer, ENV.staging).getOrThrow();
        val pgpPK = DecryptPgp.decryptPgpKey(user.encryptedPrivateKey, signer).getOrThrow()

        user.profile.picture = "picture"
        user.profile.name = "null"
        user.profile.desc = "null"
        println("####${user.profile}")
        user.profile.blockedUsersList = listOf("eip155:0x1669d6484494eed4995bf4985e545245a280c38e")

//        user.profile.blockedUsersList = mutableListOf("eip155:0x1669d6484494eed4995bf4985e545245a280c38e")

        PushUser.updateUser(userAddress, user.profile, pgpPK, ENV.staging).getOrThrow()

        val userAgain = PushUser.getUser(userAddress, ENV.staging) ?: throw IllegalStateException("")

//        assertEquals(userAgain.profile.picture, "picture")
    }

    @Test fun userBlockUnBlock(){
        val (userAddress, signer) = getNewSinger()

        val user = PushUser.createUser(signer, ENV.staging).getOrThrow();
        val pgpPK = DecryptPgp.decryptPgpKey(user.encryptedPrivateKey, signer).getOrThrow()



        val (addrs1, _) = getNewSinger()
        val (addrs2, _) = getNewSinger()


        // Block tests
        PushUser.blockUser(userAddress, pgpPK, listOf(addrs1),ENV.staging).getOrThrow()
//        var userAgain = PushUser.getUser(userAddress, ENV.staging) ?: throw  IllegalStateException("")
//        assertEquals(userAgain.profile.blockedUsersList!!.size, 1)
        return
//        PushUser.blockUser(userAddress, pgpPK, listOf(addrs1),ENV.staging).getOrThrow()
//        userAgain = PushUser.getUser(userAddress, ENV.staging) ?: throw  IllegalStateException("")
//        assertEquals(userAgain.profile.blockedUsersList!!.size, 1)

//        PushUser.blockUser(userAddress, pgpPK, listOf(addrs2),ENV.staging).getOrThrow()
//        userAgain = PushUser.getUser(userAddress, ENV.staging) ?: throw  IllegalStateException("")
//        assertEquals(userAgain.profile.blockedUsersList!!.size, 2)
//
//        // Un Block tests
//        PushUser.unblockUser(userAddress, pgpPK, listOf(addrs2),ENV.staging).getOrThrow()
//        userAgain = PushUser.getUser(userAddress, ENV.staging) ?: throw  IllegalStateException("")
//        assertEquals(userAgain.profile.blockedUsersList!!.size, 1)
//
//        PushUser.unblockUser(userAddress, pgpPK, listOf(addrs2),ENV.staging).getOrThrow()
//        userAgain = PushUser.getUser(userAddress, ENV.staging) ?: throw  IllegalStateException("")
//        assertEquals(userAgain.profile.blockedUsersList!!.size, 1)
//
//        PushUser.unblockUser(userAddress, pgpPK, listOf(addrs1),ENV.staging).getOrThrow()
//        userAgain = PushUser.getUser(userAddress, ENV.staging) ?: throw  IllegalStateException("")
//        assertEquals(userAgain.profile.blockedUsersList!!.size, 0)

    }

}