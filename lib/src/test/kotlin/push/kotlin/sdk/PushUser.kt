package push.kotlin.sdk

import getNewSinger
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


}