package push.kotlin.sdk.channels

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.Request
import push.kotlin.sdk.ENV
import push.kotlin.sdk.Helpers
import push.kotlin.sdk.PushURI
import java.net.URL

enum class NotificationType(val value: Int) {
    BROADCAST(1),
    TARGETED(3),
    SUBSET(4)
}
class ChannelNotification {
    companion object {
        fun getChannelNotifications(options: GetChannelOptionsType): Result<ChannelNotificationsData> {
            val channel = Helpers.walletToCAIP(options.env, options.channel)
            val queryParams = Helpers. getQueryParams(
                    if (options.filter != null) {
                        mapOf(
                                "page" to options.page.toString(),
                                "limit" to options.limit.toString(),
                                "notificationType" to options.filter.toString(),
                                "raw" to options.raw.toString()
                        )
                    } else {
                        mapOf(
                                "page" to options.page.toString(),
                                "limit" to options.limit.toString(),
                                "raw" to options.raw.toString()
                        )
                    }
            )

            val url = PushURI.getChannelNotifications( channel, queryParams, options.env)
            val obj = URL(url)

            val client = OkHttpClient()
            val request = Request.Builder().url(obj).build()
            val response = client.newCall(request).execute()

            return if(response.isSuccessful) {
                val jsonResponse = response.body?.string()
                val gson = Gson()
                val apiResponse = gson.fromJson(jsonResponse, ChannelNotificationsData::class.java)
                Result.success(apiResponse)
            } else {
                Result.failure(IllegalStateException("Error ${response.code} ${response.message}"))
            }
        }
    }

    data class GetChannelOptionsType(
            val channel: String,
            val env: ENV,
            val page: Int = 1,
            val limit: Int = 10,
            val filter: NotificationType? = null,
            val raw: Boolean = true
    )



    data class Notification(
            val timestamp: String,
            val from: String,
            val to: List<String>,
            @SerializedName("notifID") val notifId: Long,
            val channel: Channel,
            val meta: Meta,
            val message: Message,
            val config: Config,
            val source: String,
            val raw: Raw
    )

    data class Channel(
            val name: String,
            val icon: String,
            val url: String
    )

    data class Meta(
            val type: String
    )

    data class Message(
            val notification: NotificationData,
            val payload: Payload
    )

    data class NotificationData(
            val title: String,
            val body: String
    )

    data class Payload(
            val title: String,
            val body: String,
            val cta: String,
            val embed: String,
            val meta: MetaData
    )

    data class MetaData(
            val domain: String
    )

    data class Config(
            val expiry: String?, // Adjust the type if expiry is not a String
            val silent: Boolean,
            val hidden: Boolean
    )

    data class Raw(
            val verificationProof: String
    )

    data class ChannelNotificationsData(
            val notifications: List<Notification>,
            val total: Int
    )

}