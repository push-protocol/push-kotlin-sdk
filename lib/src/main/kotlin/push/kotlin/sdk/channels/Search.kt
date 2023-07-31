package push.kotlin.sdk.channels

import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import push.kotlin.sdk.ENV
import push.kotlin.sdk.PushURI
import java.net.URL

//data class SearchedChannels(
//        val channels
//)

class Search {
    companion object {
        fun searchChannels(env: ENV, page: Number, limit: Number, query: String): Result<AllChannelOptions> {
            val url = PushURI.searchChannels(env, page, limit, query)
            val obj = URL(url)

            val client = OkHttpClient()
            val request = Request.Builder().url(obj).build()
            val response = client.newCall(request).execute()
            if(response.isSuccessful) {
                val jsonResponse = response.body?.string()
                val gson = Gson()
                val apiResponse = gson.fromJson(jsonResponse, AllChannelOptions::class.java)
                return Result.success(apiResponse)
            } else {
                return Result.failure(IllegalStateException("Error ${response.code} ${response.message}"))
            }
        }
    }
}