package push.kotlin.sdk.ChatFunctions

import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import push.kotlin.sdk.*
import push.kotlin.sdk.Group.IsGroupChatId
import push.kotlin.sdk.Group.PushGroup
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
    if(IsGroupChatId(requesterAddress)){
     toDID = Helpers.walletToPCAIP(requesterAddress)
     fromDID = Helpers.walletToPCAIP(approverAddress)
    }else {
      fromDID = Helpers.walletToPCAIP(requesterAddress)
      toDID = Helpers.walletToPCAIP(approverAddress)
    }
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

data class RejectRequestPayload(
        val fromDID:String,
        val toDID: String,
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
    val jsonString = mapOf(
        "fromDID" to approveOptions.fromDID,
        "toDID" to approveOptions.toDID,
        "status" to "Approved"
    )

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

  fun getRejectPayload():Result<RejectRequestPayload>{
    val jsonString = mapOf(
            "fromDID" to approveOptions.fromDID,
            "toDID" to approveOptions.toDID
    )

    val hash = GenerateSHA256Hash(jsonString)

    val sig = Pgp.sign(approveOptions.pgpPrivateKey, hash).getOrElse { exception -> return Result.failure(exception) }

    return Result.success(RejectRequestPayload(
            fromDID = approveOptions.fromDID,
            toDID = approveOptions.toDID,
            verificationProof = "pgp:$sig"
    ))

  }

  fun reject() : Result<String>{
    val payload = getRejectPayload().getOrElse { exception -> return Result.failure(exception) }
    val url = PushURI.rejectChatRequest(approveOptions.env)

    val mediaType = "application/json; charset=utf-8".toMediaType()
    val body = Gson().toJson(payload).toRequestBody(mediaType)

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

  fun approve():Result<String>{
    val payload = getApprovePayload().getOrElse { exception -> return Result.failure(exception) }
    val url = PushURI.acceptChatRequest(approveOptions.env)

    val mediaType = "application/json; charset=utf-8".toMediaType()
    val body = Gson().toJson(payload).toRequestBody(mediaType)

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

    if(IsGroupChatId(sendOptions.receiverAddress)){
      val group = PushGroup.getGroup(sendOptions.receiverAddress, sendOptions.env) ?: return Result.failure(IllegalStateException("${sendOptions.senderAddress} group not found"))
      return if (group.isPublic){
        Result.success(listOf())
      }else{
        val publicKeys = group.members.map { el -> el.publicKey }.filterNotNull()
        Result.success(publicKeys)
      }
    }

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

  fun send():Result<PushChat.Message>{
    if(IsGroupChatId(sendOptions.receiverAddress)){
      sendChat()
    }

    val threadHash = PushChat.getConversationHash(sendOptions.receiverAddress, sendOptions.senderAddress, ENV.staging)

    return if(threadHash == null){
      sendIntent()
    }else{
      sendChat()
    }
  }

  fun sendChat():Result<PushChat.Message>{
    val publicKeys = getP2PChatPublicKeys(sendOptions).getOrElse { exception -> return Result.failure(exception)  }
    val sendMessagePayload = getSendMessagePayload(sendOptions, publicKeys).getOrElse { exception -> return  Result.failure(exception) }
    return sendService(sendMessagePayload, sendOptions.env, isRequest = false)
  }

  fun sendIntent():Result<PushChat.Message>{
    val publicKeys = getP2PChatPublicKeys(sendOptions).getOrElse { exception -> return Result.failure(exception)  }

    val sendMessagePayload = getSendMessagePayload(sendOptions, publicKeys).getOrElse { exception -> return  Result.failure(exception) }

    return sendService(sendMessagePayload, sendOptions.env, isRequest = true)
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

  fun sendService(payload: SendMessagePayload,env: ENV, isRequest:Boolean=false):Result<PushChat.Message>{
    val url = if(isRequest) {PushURI.sendChatIntent(env)} else {PushURI.sendChatMessage(env)}

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