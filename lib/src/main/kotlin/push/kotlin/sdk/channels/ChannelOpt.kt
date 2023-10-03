package push.kotlin.sdk.channels

import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import push.kotlin.sdk.ENV
import push.kotlin.sdk.Helpers
import push.kotlin.sdk.PushURI
import push.kotlin.sdk.TypedSinger


data class OptMessage(val action:String, val channel: String, val subscriber:String?, val unsubscriber:String?)
data class OptInPayload(val verificationProof: String, val message: OptMessage)

class ChannelOpt {
    companion object {

        fun getOptInMessage(channel: String, subscriber: String, env: ENV): String {
            val _channel = channel.lowercase()
            val _subscriber = subscriber.lowercase()
            var _chainId = 5

            if(env == ENV.prod) {
                _chainId = 1
            }

            return """
                {"types":{"Subscribe":[{"name":"channel","type":"address"},{"name":"subscriber","type":"address"},{"name":"action","type":"string"}],"EIP712Domain":[{"name":"name","type":"string"},{"name":"chainId","type":"uint256"},{"name":"verifyingContract","type":"address"}]},"primaryType":"Subscribe","domain":{"name":"EPNS COMM V1","chainId":$_chainId,"verifyingContract":"0xb3971BCef2D791bc4027BbfedFb47319A4AAaaAa"},"message":{"channel":"$_channel","subscriber":"$_subscriber","action":"Subscribe"}}
            """.trimIndent()
        }

        fun getOptOutMessage(channel: String, subscriber: String, env: ENV): String {
            val _channel = channel.lowercase()
            val _subscriber = subscriber.lowercase()
            var _chainId = 5

            if(env == ENV.prod) {
                _chainId = 1
            }

            return """
                {"types":{"Unsubscribe":[{"name":"channel","type":"address"},{"name":"unsubscriber","type":"address"},{"name":"action","type":"string"}],"EIP712Domain":[{"name":"name","type":"string"},{"name":"chainId","type":"uint256"},{"name":"verifyingContract","type":"address"}]},"primaryType":"Unsubscribe","domain":{"name":"EPNS COMM V1","chainId":$_chainId,"verifyingContract":"0xb3971BCef2D791bc4027BbfedFb47319A4AAaaAa"},"message":{"channel":"$_channel","unsubscriber":"$_subscriber","action":"Unsubscribe"}}
            """.trimIndent()
        }


        fun subscribe(channel: String, subscriber: String, signer: TypedSinger, env:ENV): Result<Boolean> {

            val messageToSign = getOptInMessage(
                channel,
                subscriber,
                env
            )

            val verificationProof = signer.getEip712Signature(messageToSign).getOrElse { exception -> return Result.failure(IllegalStateException(exception)) }
            val channelAddressCAIP = Helpers.walletToCAIP(env,channel)
            val userAddressCAIP = Helpers.walletToCAIP(env,subscriber)
            val payload = OptInPayload(
                verificationProof = verificationProof,
                message = OptMessage(
                    action = "Subscribe",
                    channel = channelAddressCAIP,
                    subscriber = userAddressCAIP,
                    unsubscriber = null
                )
            )

            val url = PushURI.OptInChannel(env, Helpers.walletToCAIP(env,channel))
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = Gson().toJson(payload).toRequestBody(mediaType)

            val client = OkHttpClient()
            val request = Request.Builder().url(url).post(body).build()
            client.newCall(request).execute()

            val isSubscribed =  ChannelSubscriber.IsSubscribed(subscriber, channel, env).getOrElse { e -> return  Result.failure(e) }

            return  if (isSubscribed){
                Result.success(true)
            }else{
                Result.failure(IllegalStateException("Request failed"))
            }
        }

        fun unsubscribe(channel: String, subscriber: String, signer: TypedSinger, env:ENV): Result<Boolean> {

            val messageToSign = getOptOutMessage(
                channel,
                subscriber,
                env
            )

            val verificationProof = signer.getEip712Signature(messageToSign).getOrElse { exception -> return Result.failure(IllegalStateException(exception)) }
            val channelAddressCAIP = Helpers.walletToCAIP(env,channel)
            val userAddressCAIP = Helpers.walletToCAIP(env,subscriber)
            val payload = OptInPayload(
                    verificationProof = verificationProof,
                    message = OptMessage(
                            action = "Unsubscribe",
                            channel = channelAddressCAIP,
                            subscriber = null,
                            unsubscriber = userAddressCAIP
                    )
            )

            val url = PushURI.OptOutChannel(env, Helpers.walletToCAIP(env,channel))
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = Gson().toJson(payload).toRequestBody(mediaType)

            val client = OkHttpClient()
            val request = Request.Builder().url(url).post(body).build()
            client.newCall(request).execute()

            val isSubscribed =  ChannelSubscriber.IsSubscribed(subscriber, channel, env).getOrElse { e -> return  Result.failure(e) }

            return  if (!isSubscribed){
                Result.success(true)
            }else{
                Result.failure(IllegalStateException("Request failed"))
            }
        }
    }
}