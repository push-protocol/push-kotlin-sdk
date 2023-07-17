package push.kotlin.sdk

import org.web3j.crypto.Credentials
import org.web3j.crypto.Sign
import org.web3j.utils.Numeric

interface Signer {
    fun signMessage(message: ByteArray): String
    fun getAddress(): String
}

    class Signature(privateKey: String) : Signer {
        private val credentials: Credentials = Credentials.create(privateKey)

        override fun signMessage(message: ByteArray): String {
            val signatureData = Sign.signPrefixedMessage(message, credentials.ecKeyPair)
            val r = Numeric.toHexStringNoPrefix(signatureData.r)
            val s = Numeric.toHexStringNoPrefix(signatureData.s)
            val v = Numeric.toHexStringNoPrefix(signatureData.v)
            val signature = r + s + v
            return "0x" + signature
        }

        override fun getAddress(): String {
            return credentials.address
        }
    }