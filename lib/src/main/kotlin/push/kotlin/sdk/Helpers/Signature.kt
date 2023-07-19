package push.kotlin.sdk

import org.web3j.crypto.Credentials
import org.web3j.crypto.Sign
import org.web3j.utils.Numeric

interface Signer {
    fun signMessage(message: ByteArray): String
    fun getAddress(): String
}

data class EncryptedPrivateKey(
    val ciphertext: String,
    val version: String,
    val salt: String,
    val nonce: String,
    val preKey: String
)


class Signature {
    companion object {
        fun getEip191Signature(privateKey: String,message: String): String {
            val credentials: Credentials = Credentials.create(privateKey)
            val signatureData = Sign.signPrefixedMessage(message.toByteArray(Charsets.UTF_8), credentials.ecKeyPair)
            val r = Numeric.toHexStringNoPrefix(signatureData.r)
            val s = Numeric.toHexStringNoPrefix(signatureData.s)
            val v = Numeric.toHexStringNoPrefix(signatureData.v)
            val signature = r + s + v
            return "0x$signature"
        }


        fun getAddress(privateKey: String): String {

            val credentials: Credentials = Credentials.create(privateKey)
            return credentials.address
        }
    }
}
