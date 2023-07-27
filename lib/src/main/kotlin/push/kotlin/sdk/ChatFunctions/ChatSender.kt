package push.kotlin.sdk.ChatFunctions

import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import push.kotlin.sdk.*
import push.kotlin.sdk.ProfileCreator.ProfileCreator


class SendOptions(var messageContent:String, var messageType:String="Text", var receiverAddress:String, var senderAddress:String, var senderPgpPrivateKey: String, var env: ENV){

  init {
    senderAddress = Helpers.walletToPCAIP(senderAddress)
    receiverAddress = Helpers.walletToPCAIP(receiverAddress)
  }
}

data class SendMessagePayload(
    var fromDID: String,
    var toDID: String,
    var fromCAIP10: String,
    var toCAIP10: String,
    var messageContent: String,
    var messageType: String,
    var signature: String,
    var encType: String,
    var encryptedSecret: String,
    var sigType: String,
    var verificationProof: String?
)

data class ChatSender(val sendOptions:SendOptions){

  fun sendIntent():Result<PushChat.Message>{
    val anotherUser = PushUser.getUser(sendOptions.receiverAddress, sendOptions.env) ?: ProfileCreator.createUserEmpty(sendOptions.receiverAddress, sendOptions.env).getOrThrow()
    val senderUser = PushUser.getUser(sendOptions.senderAddress, sendOptions.env) ?: return  Result.failure(IllegalStateException("${sendOptions.senderAddress} account not found"))

    var shouldEncrypt = true

    if (anotherUser.publicKey == ""){
      shouldEncrypt = false
    }

    val publicKeys = listOf<String>()
    val sendMessagePayload = getSendMessagePayload(sendOptions, shouldEncrypt).getOrElse { exception -> return  Result.failure(exception) }

    return sendIntentService(sendMessagePayload, sendOptions.env)
  }

  fun getSendMessagePayload(sendOptions: SendOptions, shouldEncrypt:Boolean):Result<SendMessagePayload>{
    val encType = "PlainText"
    var signature = ""
    var encryptedSecret = ""
    var messageContent = sendOptions.messageContent

    if (shouldEncrypt){

    }else{
      signature = Pgp.sign(sendOptions.senderPgpPrivateKey, messageContent).getOrElse { exception -> return Result.failure(exception) }
    }

    val payload = SendMessagePayload(
      fromCAIP10 = sendOptions.senderAddress,
      fromDID = sendOptions.senderAddress,
      toCAIP10 = sendOptions.receiverAddress,
      toDID = sendOptions.receiverAddress,
      messageContent = messageContent,
      messageType = sendOptions.messageType,
      encryptedSecret = encryptedSecret,
      sigType = "pgp",
      encType = encType,
      signature = signature,
      verificationProof = "pgp:$signature"
    )

    return Result.success(payload)
  }

  fun sendIntentService(payload: SendMessagePayload, env: ENV):Result<PushChat.Message>{
    val url = PushURI.sendChatIntent(env)

    val mediaType = "application/json; charset=utf-8".toMediaType()
    val body = Gson().toJson(payload).toRequestBody(mediaType)

    val client = OkHttpClient()
    val request = Request.Builder().url(url).post(body).build()
    val response = client.newCall(request).execute()

    return if (response.isSuccessful) {
      val jsonResponse = response.body?.string()
      val gson = Gson()
      val apiResponse = gson.fromJson(jsonResponse, PushChat.Message::class.java)
      Result.success(apiResponse)
    } else {
      println("Error: ${response.code} ${response.message}")
      Result.failure(IllegalStateException("Error: ${response.code} ${response.message}"))
    }

  }

}