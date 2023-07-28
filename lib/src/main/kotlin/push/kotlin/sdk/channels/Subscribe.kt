package push.kotlin.sdk.channels

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import push.kotlin.sdk.ENV
import push.kotlin.sdk.Helpers
import push.kotlin.sdk.PushURI
import java.net.URL

data class RequestStruct(
        @SerializedName("subscriber") val subscriber: String,
        @SerializedName("channel") val channel: String,
        @SerializedName("op") val op: String
)

data class Subscribers(
        val itemcount: Int?,
        val subscribers: List<String>
)

class Subscribe {
    companion object {
        fun getSubscribers(channel: String, env: ENV, page: Number, limit: Number): Result<Subscribers> {
            val channelCAPI = Helpers.walletToPCAIP(env, channel)
            val url = PushURI.getSubscribers(channelCAPI, page, limit, env)

            println(url)
            val obj = URL(url)
            val client = OkHttpClient()
            val request = Request.Builder().url(obj).get().build()
            val response = client.newCall(request).execute()
            if(response.isSuccessful) {
                val jsonResponse = response.body?.string()
                val gson = Gson()
                println(jsonResponse)
                val apiResponse = gson.fromJson(jsonResponse,Subscribers::class.java)
                return Result.success(apiResponse)
            } else {
                return Result.failure(IllegalStateException("Error ${response.code} ${response.message}"))
            }
        }

        fun IsSubscribed(userAddress: String, channelAddress: String, env: ENV): Result<Boolean> {
            val requestBody = RequestStruct(subscriber = userAddress, channel = channelAddress, op = "read")

            // Convert the requestBody to JSON using Gson
            val gson = Gson()
            val requestBodyJson = gson.toJson(requestBody)

            val url = PushURI.isUserSubscribed(env)
            val obj = URL(url)
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val client = OkHttpClient()

            val request = Request.Builder().url(obj).post(requestBodyJson.toRequestBody(mediaType)).build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                // Handle the response here, if needed
                val jsonResponse = response.body?.string()
                // Do something with jsonResponse
                return Result.success(jsonResponse.toBoolean())
            } else {
                return Result.failure(IllegalStateException("Error ${response.code} ${response.message}"))
            }
        }
    }
}