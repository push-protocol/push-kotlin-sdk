package push.kotlin.sdk

import org.web3j.crypto.Credentials
import org.web3j.crypto.Sign
import org.web3j.utils.Numeric

abstract class Signer {
    abstract fun getEip191Signature(message: String): Result<String>
    abstract fun getAddress(): Result<String>
}

abstract class TypedSinger{
    abstract fun getEip712Signature(message: String): Result<String>
    abstract fun getAddress(): Result<String>
}

data class EncryptedPrivateKey(
        val ciphertext: String,
        val version: String,
        val salt: String,
        val nonce: String,
        val preKey: String
)

class PrivateKeySigner(private val privateKey: String) : Signer() {

    override fun getEip191Signature(message: String): Result<String> {
        return try {
            val credentials: Credentials = Credentials.create(privateKey)
            val signatureData = Sign.signPrefixedMessage(message.toByteArray(Charsets.UTF_8), credentials.ecKeyPair)
            val r = Numeric.toHexStringNoPrefix(signatureData.r)
            val s = Numeric.toHexStringNoPrefix(signatureData.s)
            val v = Numeric.toHexStringNoPrefix(signatureData.v)
            val signature = r + s + v
            Result.success("0x$signature")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAddress(): Result<String> {
        return try {
            val credentials: Credentials = Credentials.create(privateKey)
            Result.success(credentials.address)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
