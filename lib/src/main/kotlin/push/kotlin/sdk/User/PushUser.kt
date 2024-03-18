package push.kotlin.sdk

import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import push.kotlin.sdk.JsonHelpers.GetJsonStringFromKV
import push.kotlin.sdk.ProfileCreator.ProfileCreator
import push.kotlin.sdk.ProfileCreator.ProfileUpdater
import push.kotlin.sdk.ProfileCreator.UserProfileBlock

class PushUser {

    data class ProfileInfo(
        var verificationProof: String?,
        var picture:String?,
        var name:String?,
        var desc:String?,
        var blockedUsersList:List<String>?,
    ){
        companion object {
            fun fromJson(json: Map<String, Any?>): ProfileInfo {
                return ProfileInfo(
                        name = json["name"] as? String,
                        desc = json["desc"] as? String,
                        picture = json["picture"] as? String,
                        blockedUsersList = (json["blockedUsersList"] as? List<*>)
                                ?.filterIsInstance<String>(),
                        verificationProof = json["profileVerificationProof"] as? String
                )
            }
        }
    }



    data class UserPgpPublicKey(val key: String, val signature: String)

    data class UserProfile(
        val did: String,
        val wallets: String,
        val publicKey: String,
        val encryptedPrivateKey: String,
        val verificationProof: String?,
        val msgSent: Number,
        val maxMsgPersisted:Number,
        val profile: PushUser.ProfileInfo,
        val origin: String? = null
    ){
       fun getUserPublicKey():String{
           return try {
               Gson().fromJson(publicKey, UserPgpPublicKey::class.java).key
           }catch(e:Exception){
               publicKey
           }
       }

        companion object {
            fun fromJson(json: Map<String, Any>): UserProfile {
                return UserProfile(
                        msgSent = json["msgSent"] as Number,
                        maxMsgPersisted = json["maxMsgPersisted"] as Number,
                        did = json["did"] as String,
                        wallets = json["wallets"] as String,
                        profile = ProfileInfo.fromJson(json["profile"] as Map<String, Any>),
                        encryptedPrivateKey = json["encryptedPrivateKey"] as String,
                        publicKey = json["publicKey"] as String,
                        verificationProof = json["verificationProof"] as String?,
                        origin = json["origin"] as? String
                )
            }
        }
    }

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
            return ProfileCreator(signer,env).createUserProfile()
        }

        public fun updateUser(userAddress:String, userProfile: PushUser.ProfileInfo, userPgpPrivateKey:String, env: ENV):Result<Boolean>{
            return ProfileUpdater(userAddress, userProfile, userPgpPrivateKey, env).updateUserProfile()
        }

        public fun blockUser(userAddress: String, userPgpPrivateKey: String, addressToBlock:List<String>,env: ENV):Result<Boolean>{
            return UserProfileBlock(userAddress, userPgpPrivateKey, addressToBlock, env).block()
        }

        public fun unblockUser(userAddress: String, userPgpPrivateKey: String, addressToUnBlock:List<String>,env: ENV):Result<Boolean>{
            return UserProfileBlock(userAddress, userPgpPrivateKey, addressToUnBlock, env).unblock()
        }

        public  fun getUser(userAddress:String, env: ENV): PushUser.UserProfile?{
            val url =  PushURI.getUser(env, Helpers.walletToPCAIP(userAddress))

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