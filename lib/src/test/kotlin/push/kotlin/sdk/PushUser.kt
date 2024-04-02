package push.kotlin.sdk

import BASE_64_IMAGE
import getNewSinger
import getSingerWithKey
import kotlinx.serialization.json.JsonPrimitive
import push.kotlin.sdk.HahHelper.GenerateSHA256Hash
import push.kotlin.sdk.HahHelper.GenerateSHA256Hash_
import push.kotlin.sdk.JsonHelpers.GetJsonStringFromGenericKV
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
        val user = PushUser.createUser(signer, ENV.staging).getOrThrow();

        val pgpPK = DecryptPgp.decryptPgpKey(user.encryptedPrivateKey, signer).getOrThrow()

        val userData = PushUser.getUser(userAddress, ENV.staging) ?: throw IllegalStateException("")

        userData.profile.blockedUsersList = mutableListOf("eip155:0x1669d6484494eed4995bf4985e545245a280c38e")

        PushUser.updateUser(userAddress, userData.profile, pgpPK, ENV.staging).getOrThrow()

        val userAgain = PushUser.getUser(userAddress, ENV.staging) ?: throw IllegalStateException("")

//        assertEquals(userAgain.profile.picture, picture)

//        assertEquals(userAgain.profile.blockedUsersList?.size, 1)
    }

    @Test
    fun updateUserForSigner() {

        val (userAddress, signer) = getSingerWithKey("14908f59f935507f280b92f88127df9af92ec31cc7799dddb1e001a59de1d6fe")

        val userData = PushUser.getUser(userAddress, ENV.staging) ?: throw IllegalStateException("")
        val pgpPK = DecryptPgp.decryptPgpKey(userData.encryptedPrivateKey, signer).getOrThrow()

        val picture = BASE_64_IMAGE

        userData.profile.blockedUsersList = mutableListOf("eip155:0x1669d6484494eed4995bf4985e545245a280c38e")

        PushUser.updateUser(userAddress, userData.profile, pgpPK, ENV.staging).getOrThrow()

        val userAgain = PushUser.getUser(userAddress, ENV.staging) ?: throw IllegalStateException("")


        assertEquals(userAgain.profile.blockedUsersList?.size, 1)
    }

    @Test fun userBlockUnBlock(){
        val (userAddress, signer) = getNewSinger()

        val user = PushUser.createUser(signer, ENV.staging).getOrThrow();
        val pgpPK = DecryptPgp.decryptPgpKey(user.encryptedPrivateKey, signer).getOrThrow()

        val (address1, _) = getNewSinger()
        val (address2, _) = getNewSinger()

        // Block tests
        PushUser.blockUser(userAddress, pgpPK, listOf(address1), ENV.staging).getOrThrow()
        var userAgain = PushUser.getUser(userAddress, ENV.staging) ?: throw IllegalStateException("")
        assertEquals(userAgain.profile.blockedUsersList!!.size, 1)

        PushUser.blockUser(userAddress, pgpPK, listOf(address1), ENV.staging).getOrThrow()
        userAgain = PushUser.getUser(userAddress, ENV.staging) ?: throw IllegalStateException("")
        assertEquals(userAgain.profile.blockedUsersList!!.size, 1)

        PushUser.blockUser(userAddress, pgpPK, listOf(address2), ENV.staging).getOrThrow()
        userAgain = PushUser.getUser(userAddress, ENV.staging) ?: throw IllegalStateException("")
        assertEquals(userAgain.profile.blockedUsersList!!.size, 2)

        // Un Block tests
        PushUser.unblockUser(userAddress, pgpPK, listOf(address2), ENV.staging).getOrThrow()
        userAgain = PushUser.getUser(userAddress, ENV.staging) ?: throw IllegalStateException("")
        assertEquals(userAgain.profile.blockedUsersList!!.size, 1)

        PushUser.unblockUser(userAddress, pgpPK, listOf(address2), ENV.staging).getOrThrow()
        userAgain = PushUser.getUser(userAddress, ENV.staging) ?: throw IllegalStateException("")
        assertEquals(userAgain.profile.blockedUsersList!!.size, 1)

        PushUser.unblockUser(userAddress, pgpPK, listOf(address1), ENV.staging).getOrThrow()
        userAgain = PushUser.getUser(userAddress, ENV.staging) ?: throw IllegalStateException("")
        assertEquals(userAgain.profile.blockedUsersList!!.size, 0)

    }


}