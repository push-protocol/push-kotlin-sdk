package push.kotlin.sdk.channels

import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import push.kotlin.sdk.ENV
import push.kotlin.sdk.PushURI
import java.net.URL


class ChannelSearch {
    companion object {
        fun searchChannels(query: String,page: Number, limit: Number, order: String,env: ENV): Result<PushChannels> {
            val url = PushURI.searchChannels(env, page, limit, order, query)
            val obj = URL(url)

            val client = OkHttpClient()
            val request = Request.Builder().url(obj).build()
            val response = client.newCall(request).execute()

            return if(response.isSuccessful) {
                val jsonResponse = response.body?.string()
                val gson = Gson()
                val apiResponse = gson.fromJson(jsonResponse, PushChannels::class.java)
                Result.success(apiResponse)
            } else {
                Result.failure(IllegalStateException("Error ${response.code} ${response.message}"))
            }
        }
    }
}