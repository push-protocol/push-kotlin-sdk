package push.kotlin.sdk.channels

import push.kotlin.sdk.ENV
import push.kotlin.sdk.Helpers
import push.kotlin.sdk.PrivateKeySigner
import push.kotlin.sdk.PushURI

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


        fun OptInChannel(env: ENV, channel: String, subscriber: String, privateKey: String): String {

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
            val channelAddressCAIP = Helpers.walletToCAIP(channel)
            val userAddressCAIP = Helpers.walletToCAIP(userAddress.toString())
            val requestBody = OptRequest.optRequest(verificationProof.toString(), channel, subscriber, true)
            println(requestBody)
            val url = PushURI.OptInChannel(env, channel)

            return ""
        }
    }
}