package push.kotlin.sdk.ChatFunctions

import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import push.kotlin.sdk.*
import push.kotlin.sdk.HahHelper.GenerateSHA256Hash
import push.kotlin.sdk.JsonHelpers.GetJsonStringFromKV
import push.kotlin.sdk.ProfileCreator.ProfileCreator


data class SendOptions(var messageContent:String, var messageType:String="Text", var receiverAddress:String, var senderAddress:String, var senderPgpPrivateKey: String, var env: ENV){

  init {
    senderAddress = Helpers.walletToPCAIP(senderAddress)
    receiverAddress = Helpers.walletToPCAIP(receiverAddress)
  }
}

data class ApproveOptions(var requesterAddress: String, var approverAddress: String, var pgpPrivateKey:String, var env: ENV){
  var fromDID:String
  var toDID:String
  init {
    fromDID = Helpers.walletToPCAIP(requesterAddress)
    toDID = Helpers.walletToPCAIP(approverAddress)
  }
}

data class ApproveRequestPayload(
  val fromDID:String,
  val toDID: String,
  val signature: String,
  val status:String="Approved",
  val sigType: String,
  var verificationProof: String
)

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

data class ChatApprover(val approveOptions: ApproveOptions){
  fun getApprovePayload():Result<ApproveRequestPayload>{
    val jsonString = GetJsonStringFromKV(listOf(
            "fromDID" to approveOptions.fromDID,
            "toDID" to approveOptions.toDID,
            "status" to "Approved"
    ))

    val hash = GenerateSHA256Hash(jsonString)

    val sig = Pgp.sign(approveOptions.pgpPrivateKey, hash).getOrElse { exception -> return Result.failure(exception) }

    return Result.success(ApproveRequestPayload(
            fromDID = approveOptions.fromDID,
            toDID = approveOptions.toDID,
            signature = sig,
            status = "Approved",
            sigType = "pgp",
            verificationProof = "pgp:$sig"
    ))

  }

  fun approve():Result<String>{
    val payload = getApprovePayload().getOrElse { exception -> return Result.failure(exception) }
    val url = PushURI.acceptChatRequest(approveOptions.env)

    val mediaType = "application/json; charset=utf-8".toMediaType()
    val body = Gson().toJson(payload).toRequestBody(mediaType)

    println(url)
    println(Gson().toJson(payload))

    val client = OkHttpClient()
    val request = Request.Builder().url(url).put(body).build()
    val response = client.newCall(request).execute()

    return if (response.isSuccessful) {
      val apiResponse = response.body ?: return  Result.failure(IllegalStateException(""))
      Result.success(apiResponse.string())
    } else {
      println("Error: ${response.code} ${response.message}")
      Result.failure(IllegalStateException("Error: ${response.code} ${response.message}"))
    }
  }
}

data class ChatSender(val sendOptions:SendOptions){

  private fun getP2PChatPublicKeys(sendOptions: SendOptions):Result<List<String>>{
    val senderUser = PushUser.getUser(sendOptions.senderAddress, sendOptions.env) ?: return  Result.failure(IllegalStateException("${sendOptions.senderAddress} account not found"))

    val receiverUser = PushUser.getUser(sendOptions.receiverAddress, sendOptions.env) ?: ProfileCreator.createUserEmpty(sendOptions.receiverAddress, sendOptions.env).getOrElse{
      exception -> return Result.failure(exception)
    }

    if(senderUser.publicKey == "" || receiverUser.publicKey == ""){
      return Result.success(listOf<String>())
    }

    val publicKeys = listOf<String>(senderUser.getUserPublicKey(), receiverUser.getUserPublicKey())

    return Result.success(publicKeys)
  }

  private fun encryptAndSign(messageContent: String, senderPgpPrivateKey: String, publicKeys: List<String>):Result<Triple<String,String,String>>{
    val aesKey =  AESCBC.getRandomString(15)
    val ciphertext = AESCBC.encrypt(aesKey, messageContent)
    val encryptedAES = Pgp.encrypt(aesKey,publicKeys).getOrElse { exception -> return Result.failure(exception) }
    val sign = Pgp.sign(senderPgpPrivateKey, ciphertext).getOrElse { exception -> return Result.failure(exception) }
    return Result.success(Triple(ciphertext, encryptedAES, sign))
  }

  fun sendIntent():Result<PushChat.Message>{
    val publicKeys = getP2PChatPublicKeys(sendOptions).getOrElse { exception -> return Result.failure(exception)  }

    val sendMessagePayload = getSendMessagePayload(sendOptions, publicKeys).getOrElse { exception -> return  Result.failure(exception) }

    return sendIntentService(sendMessagePayload, sendOptions.env)
  }

  fun getSendMessagePayload(sendOptions: SendOptions, publicKeys:List<String>):Result<SendMessagePayload>{
    var encType = "PlainText"
    var signature = ""
    var encryptedSecret = ""
    var messageContent = sendOptions.messageContent

    val shouldEncrypt = publicKeys.isNotEmpty()

    if (shouldEncrypt){
      encType = "pgp"

      val (_messageConent, _encryptedSecret, _signature) = encryptAndSign(
              messageContent,
              sendOptions.senderPgpPrivateKey,
              publicKeys
      ).getOrElse { exception -> return Result.failure(exception)  }

      messageContent = _messageConent
      encryptedSecret = _encryptedSecret
      signature = _signature

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