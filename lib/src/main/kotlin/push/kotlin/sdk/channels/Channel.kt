package push.kotlin.sdk.channels

import push.kotlin.sdk.ENV
import push.kotlin.sdk.Helpers
import push.kotlin.sdk.PushURI
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class ChannelOptions(
        val channel: String,
        val env: ENV,
        val page: Number,
        val limit: Number
)

class Channel {
    companion object {
        fun getChannel(options: ChannelOptions): ChannelOptions? {
            val channelCAIP = Helpers.walletToPCAIP(options.env, options.channel)
            val url = PushURI.getChannel(options.env, channelCAIP)

            try {
                println(url)
                val obj = URL(url)
                val connection = obj.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val responseReader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = StringBuilder()

                    var inputLine: String?
                    while (responseReader.readLine().also { inputLine = it } != null) {
                        response.append(inputLine)
                    }
                    responseReader.close()
                    if (response.length != 0) {
                        // Instead of returning the response string, return the ChannelOptions object
                        // Assuming you need to create a new ChannelOptions object here
                        return ChannelOptions(
                                channel = response.toString(),
                                env = options.env,
                                page = options.page,
                                limit = options.limit
                        )
                    } else {
                        return null
                    }

                } else {
                    println("Failed to fetch channel object. Response code: $responseCode")
                    // You might want to handle other response codes accordingly
                    return null
                }
            } catch (e: Exception) {
                println("Error while fetching channel object: ${e.message}")
                return null
            }
        }
        fun getAllChannels(options: ChannelOptions): String {
            val url = PushURI.getChannels(options.page, options.limit, options.env)
            try {
                println(url)
                val obj = URL(url)
                val connection = obj.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val responseReader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = StringBuilder()

                    var inputLine: String?
                    while (responseReader.readLine().also { inputLine = it } != null) {
                        response.append(inputLine)
                    }
                    responseReader.close()

                    return response.toString()
                } else {
                    println("Failed to fetch channel object. Response code: $responseCode")
                    // You might want to handle other response codes accordingly
                    return ""
                }
            } catch (e: Exception) {
                println("Error while fetching channel object: ${e.message}")
                return ""
            }
        }
    }
}
