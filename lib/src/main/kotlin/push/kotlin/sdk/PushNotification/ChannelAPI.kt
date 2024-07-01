package push.kotlin.sdk.PushNotification

import push.kotlin.sdk.ENV
import push.kotlin.sdk.Signer
import push.kotlin.sdk.channels.*

class ChannelAPI(
        private val account: String,
        val env: ENV,
        private val decryptedPgpPvtKey: String,
        private val signer: Signer,
) {

    data class ChannelInfoOptions(
            val channel: String,
            val page: Int = 1,
            val limit: Int = 10,
            val category: Int? = null,
            val setting: Boolean = false,
    )


    fun info(channel: String): PushChannel? {
        return Channel.getChannel(channel, env)
    }

    fun search(query: String, page: Int, limit: Int): Result<PushChannels> {
        return ChannelSearch.searchChannels(query, page, limit, "", env)
    }

    fun subscribers(options: ChannelInfoOptions): Result<ChannelSubscribers> {
        return ChannelSubscriber.getSubscribers(options.channel, options.page, options.limit, env)
    }

    fun notification(account: String, options: ChannelFeedsOptions = ChannelFeedsOptions()): Result<ChannelNotification.ChannelNotificationsData> {

        val option = ChannelNotification.GetChannelOptionsType(
                channel = account, env = env, page = options.page, limit = options.limit, raw = options.raw, filter = options.filter)

        return ChannelNotification.getChannelNotifications(option)
    }

    data class ChannelFeedsOptions(
            val page: Int = 1,
            val limit: Int = 10,
            val raw: Boolean = true,
            val filter: NotificationType? = null
    )

    fun list(){

    }
}