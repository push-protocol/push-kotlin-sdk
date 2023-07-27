package push.kotlin.sdk.channels

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import push.kotlin.sdk.ENV
import push.kotlin.sdk.Helpers
import push.kotlin.sdk.PushURI
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class RequestStruct(
        @SerializedName("subscriber") val subscriber: String,
        @SerializedName("channel") val channel: String,
        @SerializedName("op") val op: String
)

class Subscribe {
    companion object {
        fun getSubscribers(channel: String, env: ENV, page: Number, limit: Number): String? {
            val channelCAPI = Helpers.walletToPCAIP(env, channel)
            val url = PushURI.getSubscribers(channelCAPI, page, limit, env)

            println(url)
            val obj = URL(url)
            val connection = obj.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val responseCode = connection.responseCode

            if(responseCode == HttpURLConnection.HTTP_OK) {
                val responseReader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()

                var inputLine: String?
                while (responseReader.readLine().also { inputLine = it } != null) {
                    response.append(inputLine)
                }
                responseReader.close()
                if (response.length != 0) {
                    return response.toString()
                } else {
                    return null
                }
            }
            return ""
        }

        fun IsSubscribed(userAddress: String, channelAddress: String, env: ENV): Boolean? {
            val requestBody = RequestStruct(subscriber = userAddress, channel = channelAddress, op = "read")
            val url = PushURI.isUserSubscribed(env)

            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            try {
                val outputStream = connection.outputStream
                val requestBodyJson = Gson().toJson(requestBody)
                println("$requestBodyJson resss")
                outputStream.write(requestBodyJson.toByteArray())
                outputStream.flush()

                println("${connection.responseCode} resss")

                if (connection.responseCode in 200..299) {
                    val inputStream = connection.inputStream
                    val responseData = inputStream.bufferedReader().use { it.readText() }
                    val result = Gson().fromJson(responseData, Boolean::class.java)
                    inputStream.close()
                    return result
                } else {
                    throw Exception("Bad server response")
                }
            } catch (e: Exception) {
                println("Error during request: ${e.message}")
                e.printStackTrace()
                throw e
            } finally {
                connection.disconnect()
            }
        }
    }
}