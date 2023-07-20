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
        fun decryptPgpKey(encryptedPrivateKey: String, signer:Signer): String {
            val encPk = EncryptedPrivateKey.fromJsonString(encryptedPrivateKey) ?: throw IllegalStateException("Invalid Encrypted Pgp Key");
            val wallet = Wallet(privateKey)
//            it should work like this
//            val secret = wallet.getEip191Signature(privateKey,encPk.preKey)
            val secret = wallet.getEip191Signature(privateKey,"Enable Push Profile \n"+encPk.preKey)

            val pgpPrivateKey = AESGCM.decrypt(encPk.ciphertext, secret, encPk.nonce, encPk.salt)
            return pgpPrivateKey
        }
    }
}