package push.kotlin.sdk.channels

import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import push.kotlin.sdk.ENV
import push.kotlin.sdk.Helpers
import push.kotlin.sdk.PushURI
import java.net.URL
import java.util.*

data class PushChannel(
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
    data class PushChannels (
            val itemcount: Int,
            val channels: List<PushChannel>,
    )

class Channel {
    companion object {
        fun getChannel(channel: String, env: ENV): PushChannel? {
            val channelCAIP = Helpers.walletToCAIP(env, channel)
            val url = PushURI.getChannel(env, channelCAIP)
            val obj = URL(url)
            val client = OkHttpClient()
            val request = Request.Builder().url(obj).get().build()
            val response = client.newCall(request).execute()

            return if (response.isSuccessful) {
                val jsonResponse = response.body?.string()
                val gson = Gson()
                val apiResponse = gson.fromJson(jsonResponse, PushChannel::class.java)
                apiResponse
            } else {
                null
            }
        }
        fun getAllChannels(page: Int, limit: Int, env: ENV): PushChannels {
            val url = PushURI.getChannels(page, limit, env)
            val obj = URL(url)
            val client = OkHttpClient()
            val request = Request.Builder().url(obj).get().build()
            val response = client.newCall(request).execute()

            return if(response.isSuccessful) {
                val jsonResponse = response.body?.string()
                val gson = Gson()
                val apiResponse = gson.fromJson(jsonResponse, PushChannels::class.java)
                apiResponse
            } else {
                PushChannels(0, emptyList())
            }

        }
    }
}
