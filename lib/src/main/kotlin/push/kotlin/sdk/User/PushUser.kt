package push.kotlin.sdk

import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import push.kotlin.sdk.JsonHelpers.GetJsonStringFromKV
import push.kotlin.sdk.ProfileCreator.ProfileCreator

class PushUser {

    data class ProfileInfo(
            val verificationProof: String?,
            val picture:String?,
            val name:String?,
            val desc:String?,
            val blockedUsersList:Array<String>?,
    )

    data class UserProfile(
            val did: String,
            val wallets: String,
            val pubicKey: String,
            val encryptedPrivateKey: String,
            val verificationProof: String,
            val msgSent: Number,
            val maxMsgPersisted:Number,
            val profile: PushUser.ProfileInfo
    )

    data class EncryptedPrivateKey(
        val ciphertext: String,
        val salt: String,
        val nonce: String,
        val version: ENCRYPTION_TYPE,
        val preKey: String,
    ){
        fun getJsonString():String{
            return  GetJsonStringFromKV(listOf(
                "ciphertext" to ciphertext,
                "salt" to salt,
                "nonce" to nonce,
                "version" to version.encryptionType,
                "preKey" to preKey
            ))
        }
    }

    enum class ENCRYPTION_TYPE(val encryptionType: String) {
        PGP_V1("x25519-xsalsa20-poly1305"),
        PGP_V2("aes256GcmHkdfSha256"),
        PGP_V3("eip191-aes256-gcm-hkdf-sha256"),
        NFTPGP_V1("pgpv1:nft")
    }

    companion object{
        public fun createUser(signer: Signer, env:ENV):Result<PushUser.UserProfile>{

            return try {
                val wallet = Wallet(signer)
                val address = signer.getAddress().getOrThrow()
                val caip10 = Helpers.walletToPCAIP(address)

                val (publicKey, privateKey) = Pgp.generate()

                val preparedPublicKey = ProfileCreator.preparePGPPublicKey(publicKey, wallet)
                val encryptedPrivateKey = ProfileCreator.encryptPGPKey(privateKey, wallet).getJsonString()
                val createUserVProof = ProfileCreator.getCreateUserVerificationProof(wallet, caip10, preparedPublicKey, encryptedPrivateKey)

                val requestBody = GetJsonStringFromKV(listOf(
                        "caip10" to caip10,
                        "did" to caip10,
                        "publicKey" to preparedPublicKey,
                        "encryptedPrivateKey" to encryptedPrivateKey,
                        "encryptionType" to ENCRYPTION_TYPE.PGP_V3.encryptionType,
                        "name" to "",
                        "signature" to createUserVProof,
                        "sigType" to "a"
                ))

                val url = PushURI.createUser(env)

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val body = requestBody.toRequestBody(mediaType)


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
            }catch (e:Exception){
                return Result.failure(e)
            }

        }

        public  fun getUser(userAddress:String, env: ENV): PushUser.UserProfile?{
            val url =  PushURI.getUser(env, userAddress)

            // Create an OkHttpClient instance
            val client = OkHttpClient()

            // Create a request object
            val request = Request.Builder()
                    .url(url)
                    .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val jsonResponse = response.body?.string()
                val gson = Gson()
                val apiResponse = gson.fromJson(jsonResponse, PushUser.UserProfile::class.java)
                return apiResponse
            } else {
                println("Error: ${response.code} ${response.message}")
            }

            // Close the response body
            response.close()
            return null
        }
    }
}