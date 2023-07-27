package push.kotlin.sdk.channels

import push.kotlin.sdk.ENV
import push.kotlin.sdk.PushURI
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class Search {
    companion object {
        fun searchChannels(env: ENV, page: Number, limit: Number, order: String, query: String): String? {
            val url = PushURI.searchChannels(env, page, limit, order, query)
            try {
                val obj = URL(url)
                println(url)
                val connection = obj.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode
                if(responseCode == HttpURLConnection.HTTP_OK) {
                    val responseReader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = StringBuilder()

                    var inputLine: String?
                    while (responseReader.readLine().also { inputLine = it } !=null) {
                        response.append(inputLine)
                    }
                    responseReader.close()
                    if (response.length !=0) {
                        return response.toString()
                    } else {
                        return null
                    }
                } else {
                    println("Failed to fetch channel. Response code $responseCode")

                    return null
                }
            } catch (e: Exception) {
                println("Error while fetching channel object: ${e.message}")
                return null
            }
        }
    }
}