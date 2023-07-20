package push.kotlin.sdk

import org.web3j.crypto.Credentials
import org.web3j.crypto.Sign
import org.web3j.utils.Numeric

abstract class Signer {
    abstract fun getEip191Signature(message: String): String
    abstract fun getAddress(): String
}

data class EncryptedPrivateKey(
    val ciphertext: String,
    val version: String,
    val salt: String,
    val nonce: String,
    val preKey: String
)


class PrivateKeySigner(private val privateKey: String): Signer(){
    override fun getEip191Signature(message: String): String {
        val credentials: Credentials = Credentials.create(privateKey)
        val signatureData = Sign.signPrefixedMessage(message.toByteArray(Charsets.UTF_8), credentials.ecKeyPair)
        val r = Numeric.toHexStringNoPrefix(signatureData.r)
        val s = Numeric.toHexStringNoPrefix(signatureData.s)
        val v = Numeric.toHexStringNoPrefix(signatureData.v)
        val signature = r + s + v
        return "0x$signature"
    }

    override fun getAddress(): String {
        val credentials: Credentials = Credentials.create(privateKey)
        return credentials.address
    }
}
