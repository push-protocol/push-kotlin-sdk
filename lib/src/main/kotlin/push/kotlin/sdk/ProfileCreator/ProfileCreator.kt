package push.kotlin.sdk.ProfileCreator

import AESGCM
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import push.kotlin.sdk.*
import push.kotlin.sdk.HahHelper.GenerateSHA256Hash
import push.kotlin.sdk.JsonHelpers.GetJsonStringFromGenericKV
import push.kotlin.sdk.JsonHelpers.GetJsonStringFromKV
import push.kotlin.sdk.JsonHelpers.ListToJsonString
import java.security.SecureRandom


fun ByteArray.toHexString(): String {
  val hexString = StringBuilder()
  for (byte in this) {
    hexString.append(String.format("%02x", byte))
  }
  return hexString.toString()
}

data class UserProfileBlock(val userAddress: String, val userPgpPrivateKey: String, val addresses:List<String>, val env: ENV,){
  public fun block():Result<Boolean>{
    val _addresses = addresses.map { el -> Helpers.walletToPCAIP(el) }

    val user = PushUser.getUser(userAddress, env) ?: return Result.failure(IllegalStateException("User $userAddress not found"))
    val userProfile = user.profile

    if(userProfile.blockedUsersList == null){
      userProfile.blockedUsersList = _addresses
    }else{
      val _lists:MutableList<String> = userProfile.blockedUsersList!!.toMutableList()
      _addresses.forEach { el ->
        if (!userProfile.blockedUsersList!!.contains(Helpers.walletToPCAIP(el))){
          _lists += el
        }
      }

      userProfile.blockedUsersList = _lists
    }

    return ProfileUpdater(userAddress, userProfile, userPgpPrivateKey, env).updateUserProfile()
  }

  public fun unblock():Result<Boolean>{
    val user = PushUser.getUser(userAddress, env) ?: return Result.failure(IllegalStateException("User $userAddress not found"))
    val userProfile = user.profile

    val _addressToBlock: MutableList<String> = mutableListOf()
    val addressAlreadyBlock: List<String> =
            userProfile.blockedUsersList ?: emptyList()
    val _addressesToUnblock = Helpers.walletsToPCAIP(addresses)

    for (addrs in addressAlreadyBlock) {
      if (!_addressesToUnblock.contains(addrs)) {
        _addressToBlock.add(addrs)
      }
    }

    userProfile.blockedUsersList = _addressToBlock

    return ProfileUpdater(userAddress, userProfile, userPgpPrivateKey, env).updateUserProfile()
  }
}


class ProfileUpdater(val userAddress: String, val userProfile: PushUser.ProfileInfo, val userPgpPrivateKey:String, val env: ENV){
  public fun updateUserProfile():Result<Boolean>{
    val hash = getUpdatedProfileHash(userProfile)
    val sig = Pgp.sign(userPgpPrivateKey, hash).getOrElse { exception -> return Result.failure(exception) }
    val sigType = "pgpv2"
    val verificationProof = "$sigType:$sig"

    val payload = getUpdateUserPayload(userProfile, verificationProof)
    return updateUserService(payload, userAddress, env)
  }

  data class UpdateUserPayload(val name:String, val desc:String, val picture:String, val blockedUsersList:List<String>, val verificationProof:String)

  companion object{
    fun getUpdatedProfileHash(updatedUser:PushUser.ProfileInfo):String{
      var profileJsonString = GetJsonStringFromGenericKV(listOf(
        Pair("name", JsonPrimitive(updatedUser.name ?: " ")),
        Pair("desc", JsonPrimitive(updatedUser.desc ?: " ")),
        Pair("picture", JsonPrimitive(updatedUser.picture ?: " ")),
        "blockedUsersList" to JsonPrimitive("--members--replace"),
      ))

      val members:List<String> = updatedUser.blockedUsersList ?: listOf<String>();

      profileJsonString = profileJsonString.replace("--members--replace", ListToJsonString(members ?: listOf()))
      profileJsonString = profileJsonString.replace("\"[","[")
      profileJsonString = profileJsonString.replace("]\"","]")

      return GenerateSHA256Hash(profileJsonString)
    }

    fun getUpdateUserPayload(updatedUser: PushUser.ProfileInfo, verificationProof: String):UpdateUserPayload{
      return UpdateUserPayload(
        name = updatedUser.name ?: " ",
        desc = updatedUser.desc ?: " ",
        picture = updatedUser.picture ?: " ",
        blockedUsersList = updatedUser.blockedUsersList ?: listOf(),
        verificationProof = verificationProof
      )
    }

    fun updateUserService(payload:UpdateUserPayload, userAddress:String, env: ENV):Result<Boolean>{
      try {
        val url = PushURI.updateUser(env, userAddress)
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = Gson().toJson(payload).toRequestBody(mediaType)

        val client = OkHttpClient()
        val request = Request.Builder().url(url).put(body).build()
        val response = client.newCall(request).execute()

        if(response.isSuccessful){
          return Result.success(true)
        }else{
          println(url)
          println(GsonBuilder().serializeNulls().create().toJson(payload))
          println("Error: ${response.code} ${response.message}")
        }

        return Result.failure(IllegalStateException(response.message))
      }catch (e:Exception){

        return Result.failure(IllegalStateException(e))
      }
    }
  }
}

class ProfileCreator(val signer:Signer, val env:ENV) {
  public  fun createUserProfile():Result<PushUser.UserProfile>{
    try {
      val wallet = Wallet(signer)
      val address = signer.getAddress().getOrThrow()
      val caip10 = Helpers.walletToPCAIP(address)

      val (publicKey, privateKey) = Pgp.generate()
      val preparedPublicKey = preparePGPPublicKey(publicKey, wallet)
      val encryptedPrivateKey = encryptPGPKey(privateKey, wallet).getJsonString()
      val createUserVProof = getCreateUserVerificationProof(wallet, caip10, preparedPublicKey, encryptedPrivateKey)

      val payloadString = getProfileCreatePayload(caip10, preparedPublicKey,encryptedPrivateKey,PushUser.ENCRYPTION_TYPE.PGP_V3.encryptionType, createUserVProof)
      return createUserService(payloadString, env)

    }catch (e:Exception){
      return  Result.failure(e)
    }
  }

  companion object {
    fun createUserEmpty(userAddress: String, env:ENV):Result<PushUser.UserProfile>{
      val caip10 = Helpers.walletToPCAIP(userAddress)
      val payloadString = getEmptyProfileCreatePayload(caip10)

      return createUserService(payloadString, env)
    }
    fun createUserService(payload:String, env: ENV):Result<PushUser.UserProfile>{
      val url = PushURI.createUser(env)
      val mediaType = "application/json; charset=utf-8".toMediaType()
      val body = payload.toRequestBody(mediaType)

      val client = OkHttpClient()
      val request = Request.Builder().url(url).post(body).build()
      val response = client.newCall(request).execute()

      if (response.isSuccessful) {
        val jsonResponse = response.body?.string()
        val gson = Gson()
        val apiResponse = gson.fromJson(jsonResponse, PushUser.UserProfile::class.java)
        return Result.success(apiResponse)
      } else {
        println("Error: ${response.code} ${response.message}")
        return  Result.failure(IllegalStateException("Error: ${response.code} ${response.message}"))
      }
    }
    fun getProfileCreatePayload(caip10:String, preparedPublicKey:String, encryptedPrivateKey: String, encryptionType: String, verificationProof: String):String{
      return GetJsonStringFromKV(listOf(
        "caip10" to caip10,
        "did" to caip10,
        "publicKey" to preparedPublicKey,
        "encryptedPrivateKey" to encryptedPrivateKey,
        "encryptionType" to encryptionType,
        "name" to "",
        "signature" to verificationProof,
        "sigType" to "a"
      ))
    }

    fun getEmptyProfileCreatePayload(caip10: String):String{
      return  GetJsonStringFromKV(listOf(
        "caip10" to caip10,
        "did" to caip10,
        "publicKey" to "",
        "encryptedPrivateKey" to "",
        "encryptionType" to "",
        "name" to "",
        "signature" to "pgp",
        "sigType" to "pgp"
      ))
    }

    public fun preparePGPPublicKey(publicKey:String, wallet:Wallet):String{
      val messageToSign = "Create Push Profile \n" + GenerateSHA256Hash(publicKey)
      val verificationProof = wallet.getEip191Signature(messageToSign).getOrThrow()

      val preparedKey = GetJsonStringFromKV(listOf(
        "key" to publicKey,
        "signature" to verificationProof
      ))

      return  preparedKey
    }

    public fun encryptPGPKey(privateKey:String, wallet: Wallet):PushUser.EncryptedPrivateKey{
      val preKey = SecureRandom().generateSeed(32).toHexString()
      val messageToSign = "Enable Push Profile \n" + preKey
      val encryptionSecret = wallet.getEip191Signature(messageToSign).getOrThrow()

      val (cipher,salt,nonce) = AESGCM.encrypt(privateKey, encryptionSecret)
      return PushUser.EncryptedPrivateKey(
        cipher,
        salt,
        nonce,
        PushUser.ENCRYPTION_TYPE.PGP_V3,
        preKey
      )
    }

    public fun getCreateUserVerificationProof(wallet: Wallet, userAddress:String, publicKey: String, encryptedPrivateKey:String):String{
      val jsonString =  GetJsonStringFromKV(listOf(
        "caip10" to userAddress,
        "did" to userAddress,
        "public" to publicKey,
        "encryptedPrivateKey" to encryptedPrivateKey
      ))

      val hash = GenerateSHA256Hash(jsonString)
      return wallet.getEip191Signature(hash, "v2").getOrThrow()
    }
  }
}