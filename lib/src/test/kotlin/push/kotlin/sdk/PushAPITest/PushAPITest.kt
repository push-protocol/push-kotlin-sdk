package push.kotlin.sdk.PushAPITest

import getNewSinger
import push.kotlin.sdk.PushAPI.PushAPI
import kotlin.test.Test
import kotlin.test.assertEquals

class PushAPITest {
    @Test
    fun initializeTest() {
        val (newAddress, signer) = getNewSinger()
        val pushAPI = PushAPI.initialize(signer)

        val info = pushAPI.info();
        assertEquals("eip155:$newAddress", info?.did)
    }
}