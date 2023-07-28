package push.kotlin.sdk.channels

import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import push.kotlin.sdk.ENV
import push.kotlin.sdk.Helpers
import push.kotlin.sdk.PushURI
import java.net.URL
import java.util.*

data class ChannelOptions(
        val id: Int?,
        val channel: String?,
        val ipfshash: String?,
        val name: String?,
        val info: String?,
        val url: String?,
        val icon: String?,
        val processed: Int?,
        val attempts: Int?,
        val alias_address: String?,
        val alias_verification_event: String?,
        val is_alias_verified: Int?,
        val alias_blockchain_id: String?,
        val activation_Status: Int?,
        val verified_status: Int?,
        val timestamp: Date?,
        val blocked: Int?,
        val counter: String?,
        val subgraph_details: String?,
        val subgraph_attempts: Int?,
        val channel_settings: String?,
        val subscriber_count: Int?
)
//class ChannelsArray {
    data class AllChannelOptions (
            val itemcount: Int,
            val channels: List<ChannelOptions>,
    )

//}
class Channel {
    companion object {
        fun getChannel(channel: String, env: ENV): Result<ChannelOptions> {
            val channelCAIP = Helpers.walletToPCAIP(env, channel)
            val url = PushURI.getChannel(env, channelCAIP)
                println(url)
                val obj = URL(url)
                val client = OkHttpClient()
                val request = Request.Builder().url(obj).get().build()
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val jsonResponse = response.body?.string()
                    val gson = Gson()
                    println(jsonResponse)
                    val apiResponse = gson.fromJson(jsonResponse, ChannelOptions::class.java)
                    return Result.success(apiResponse)
                } else {
                    return Result.failure(IllegalStateException("Error ${response.code} ${response.message}"))
                }
        }
        fun getAllChannels(page: Int, limit: Int, env: ENV): Result<AllChannelOptions> {
            val url = PushURI.getChannels(page, limit, env)
            val obj = URL(url)
            val client = OkHttpClient()
            val request = Request.Builder().url(obj).get().build()
            val response = client.newCall(request).execute()
            if(response.isSuccessful) {
                val jsonResponse = response.body?.string()
                val gson = Gson()
                val apiResponse = gson.fromJson(jsonResponse, AllChannelOptions::class.java)
                println(jsonResponse)
                return Result.success(apiResponse)
            } else {
                return Result.failure(IllegalStateException("Error ${response.code} ${response.message}"))
            }

        }
    }
}
