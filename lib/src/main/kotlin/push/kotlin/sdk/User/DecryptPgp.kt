package push.kotlin.sdk

import AESGCM
import com.google.gson.Gson

class DecryptPgp {

    data class EncryptedPrivateKey(
            val ciphertext: String,
            val version: String,
            val salt: String,
            val nonce: String,
            val preKey: String
    ) {
        companion object {
            fun fromJsonString(jsonString: String): EncryptedPrivateKey? {
                return try {
                    val gson = Gson()
                    gson.fromJson(jsonString, EncryptedPrivateKey::class.java)
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    companion object{
        fun decryptPgpKey(encryptedPrivateKey: String, signer:Signer): Result<String> {
            return try {
                val encPk = EncryptedPrivateKey.fromJsonString(encryptedPrivateKey)
                        ?: throw IllegalStateException("Invalid Encrypted Pgp Key");
                val wallet = Wallet(signer)
                val secret = wallet.getEip191Signature("Enable Push Profile \n" + encPk.preKey).getOrThrow()
                return Result.success(AESGCM.decrypt(encPk.ciphertext, secret, encPk.nonce, encPk.salt).getOrThrow())
            }catch (e:Exception){
                Result.failure(e)
            }
        }
    }
}