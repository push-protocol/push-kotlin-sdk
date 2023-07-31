package push.kotlin.sdk.Channels

import MockEIP712OptOutSigner
import MockEIP712OptinSigner
import org.junit.jupiter.api.Test
import push.kotlin.sdk.ENV
import push.kotlin.sdk.channels.Channel
import push.kotlin.sdk.channels.ChannelOpt
import push.kotlin.sdk.channels.ChannelSearch
import push.kotlin.sdk.channels.ChannelSubscriber
import kotlin.test.assertEquals

class ChannelTests {
    @Test
    fun GettingChannel(){
        val channelAddress = "0x2AEcb6DeE3652dA1dD6b54D5fd4f7D8F43DaEb78"
        val res =  Channel.getChannel(channelAddress, ENV.staging)!!
        assertEquals(channelAddress, res.channel)
    }

    @Test
    fun GettingAllChannels () {
        val result = Channel.getAllChannels(1, 10, ENV.staging)
        val expectedCount = 10
        assertEquals(expectedCount, result.channels.size)
    }

    @Test
    fun NonExistingChannels() {
        val userAddress = "0xcD23560D4F9F816Ffb3D790e5ac3f6A316c559Ea"
        val res = Channel.getChannel(userAddress, ENV.staging)
        assert(res == null)
    }

    @Test
    fun SearchChannelByName() {
        val res = ChannelSearch.searchChannels(query ="rayan", page= 1, limit = 10, order = "desc", env = ENV.staging ).getOrThrow()
        val actual = res.itemcount
        assertEquals(true, actual > 1)
    }

    @Test
    fun SearchChannelByAddress() {
        val res = ChannelSearch.searchChannels("0x2AEcb6DeE3652dA1dD6b54D5fd4f7D8F43DaEb78", 1, 10, "desc", ENV.staging).getOrThrow()
        val jsonObject = res.channels[0]
        val actual = jsonObject.channel
        assertEquals("0x2AEcb6DeE3652dA1dD6b54D5fd4f7D8F43DaEb78", actual)
    }

    @Test
    fun testNoexistingChannelsOnSearch() {
        val res = ChannelSearch.searchChannels("rayansdsdsdsd",1, 10, "desc", ENV.staging).getOrThrow()
        val asExpected = 0
        assertEquals(asExpected, res.itemcount)
    }

    @Test
    fun getSubscriberTest() {
        val res = ChannelSubscriber.getSubscribers(channel = "0x2AEcb6DeE3652dA1dD6b54D5fd4f7D8F43DaEb78", page = 5, limit = 1, env = ENV.staging).getOrThrow()
        val actual = res.subscribers[0]
        val expected = "0x02b24ac2239b344fbc4577801f7000901e7a3944"
        assertEquals(expected, actual)
    }

    @Test
    fun isSubscribed() {
        val res1 = ChannelSubscriber.IsSubscribed(userAddress = "0x5d73D70EB962083eDED53F03e2D4fA7d7773c4CE", channelAddress = "0x2AEcb6DeE3652dA1dD6b54D5fd4f7D8F43DaEb78", env = ENV.staging).getOrThrow()
        val res2 = ChannelSubscriber.IsSubscribed("0x361158064636d05198b23389c75ee32fa10b26bd", "0x2AEcb6DeE3652dA1dD6b54D5fd4f7D8F43DaEb78", ENV.staging).getOrThrow()
        assertEquals(false, res1)
        assertEquals(true, res2)
    }

    @Test
    fun OptingINAndOutchannels() {
        val optInSigner = MockEIP712OptinSigner()
        val optOutSinger = MockEIP712OptOutSigner()

        val userAddress = optInSigner.getAddress().getOrThrow()
        var success = ChannelOpt.subscribe( "0x2AEcb6DeE3652dA1dD6b54D5fd4f7D8F43DaEb78", userAddress, optInSigner, ENV.staging).getOrThrow()
        assert(success)

        success = ChannelOpt.unsubscribe( "0x2AEcb6DeE3652dA1dD6b54D5fd4f7D8F43DaEb78", userAddress, optOutSinger, ENV.staging).getOrThrow()
        assert(success)

        success = ChannelOpt.subscribe( "0x2AEcb6DeE3652dA1dD6b54D5fd4f7D8F43DaEb78", userAddress, optInSigner, ENV.staging).getOrThrow()
        assert(success)

        success = ChannelOpt.unsubscribe( "0x2AEcb6DeE3652dA1dD6b54D5fd4f7D8F43DaEb78", userAddress, optOutSinger, ENV.staging).getOrThrow()
        assert(success)
    }
}