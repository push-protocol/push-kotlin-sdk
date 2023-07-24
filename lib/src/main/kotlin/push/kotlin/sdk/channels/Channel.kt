package push.kotlin.sdk.channels

import push.kotlin.sdk.ENV
import push.kotlin.sdk.Helpers
import push.kotlin.sdk.PushURI
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class Channel {
    companion object {

        fun getChannel(channel: String, env: ENV, page: Number, limit: Number): String {
            val url = PushURI.getChannel(env, channel)
            val address = Helpers.walletToCAIP(channel)
            println("$address all address")

            val urlString = "$url/$address"

            try {
                println(urlString)
                val obj = URL(urlString)
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

        fun getAllChannels(env: ENV, page: Number, limit: Number): String {
            val url = PushURI.getChannels(page, limit, ENV.staging)
//            println("$address all address")

            val urlString = "$url"

            try {
                println(urlString)
                val obj = URL(urlString)
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