package push.kotlin.sdk.channels

import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import push.kotlin.sdk.ENV
import push.kotlin.sdk.Helpers
import push.kotlin.sdk.PrivateKeySigner
import push.kotlin.sdk.PushURI
import java.net.URL

data class OptRequest(
        val verificationProof: String,
        val message: String
) {
    companion object {
        fun optRequest(
                verificationProof: String,
                channel: String,
                subscriber: String,
                optIn: Boolean
        ): OptRequest {
            val message = if (optIn) {
                Opt.getOptInMessage(channel, subscriber, ENV.staging)
            } else {
                println("skkdj")
            }

            return OptRequest(verificationProof, message.toString())
        }
    }
}


class Opt {
    companion object {

        fun getOptInMessage(channel: String, subscriber: String, env: ENV): String {
            val _channel = channel.toLowerCase()
            val _subscriber = channel.toLowerCase()
            var _chainId = 5

            if(env == ENV.prod) {
                _chainId = 1
            }

            return """
                {"types":{"Subscribe":[{"name":"channel","type":"address"},{"name":"subscriber","type":"address"},{"name":"action","type":"string"}],"EIP712Domain":[{"name":"name","type":"string"},{"name":"chainId","type":"uint256"},{"name":"verifyingContract","type":"address"}]},"primaryType":"Subscribe","domain":{"name":"EPNS COMM V1","chainId":$_chainId,"verifyingContract":"0xb3971BCef2D791bc4027BbfedFb47319A4AAaaAa"},"message":{"channel":"$_channel","subscriber":"$_subscriber","action":"Subscribe"}}
            """.trimIndent()
        }


        fun OptInChannel(channel: String, subscriber: String, privateKey: String, env: ENV): Result<String> {

            val message = getOptInMessage(channel, subscriber, ENV.staging)
            println(message)
            val signatureFunc = PrivateKeySigner(privateKey)
            val userAddress = signatureFunc.getAddress()

            var messageToSign = getOptInMessage(
                    channel,
                    subscriber,
                    ENV.staging
            )
            val verificationProof = signatureFunc.getEip191Signature(messageToSign)
            val channelAddressCAIP = Helpers.walletToPCAIP(env, channel)
            val userAddressCAIP = Helpers.walletToCAIP(userAddress.toString())
            val requestBody = OptRequest.optRequest(verificationProof.toString(), channel, subscriber, true)
            println(requestBody)
            val url = PushURI.OptInChannel(env, channel)
            val obj = URL(url)
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val client = OkHttpClient()
            val requestBodyJson = Gson().toJson(requestBody)
            val request = Request.Builder().url(obj).post(requestBodyJson.toRequestBody(mediaType)).build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                // Handle the response here, if needed
                val jsonResponse = response.body?.string()
                // Do something with jsonResponse
                return Result.success(jsonResponse.toString())
            } else {
                return Result.failure(IllegalStateException("Error ${response.code} ${response.message}"))
            }
        }
    }
}