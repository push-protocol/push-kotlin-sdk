package push.kotlin.sdk

import AESGCM
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
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


class Signature(private val privateKey: String) : Signer {
    private val credentials: Credentials = Credentials.create(privateKey)

    override fun signMessage(message: ByteArray): String {
        val signatureData = Sign.signPrefixedMessage(message, credentials.ecKeyPair)
        val r = Numeric.toHexStringNoPrefix(signatureData.r)
        val s = Numeric.toHexStringNoPrefix(signatureData.s)
        val v = Numeric.toHexStringNoPrefix(signatureData.v)
        val signature = r + s + v
        return "0x$signature"
    }


    override fun getAddress(): String {
        return credentials.address
    }

    fun DecryptProfile(): String {
        val sgnature = Signature(privateKey)

        val wallet = Wallet(sgnature)

        val address = wallet.getEip191Signature("Enable Push Profile \nc6f086dbc8295c8499873bf73e374f0bc230d567705c047938b3414163132280", "v1")

        return address
    }

    fun userDecryptPgpKey(encryptedPrivateKey: String, signer: Signer): String {
        val signature = Signature(privateKey)
        val wallet = Wallet(signature)
        val pp = Json.decodeFromString<EncryptedPrivateKey>(encryptedPrivateKey)

        val secret = wallet.getEip191Signature("Enable Push Profile \\n${pp.preKey}")
        val pgpPrivateKey = AESGCM.decrypt(pp.ciphertext, secret,pp.nonce, pp.salt)

        return pgpPrivateKey
    }

}

class Wallet(private val signer: Signer) {
    private var account: String? = null

    init {
        CoroutineScope(Dispatchers.Default).launch {
            account = signer.getAddress()
        }
    }

    fun getEip191Signature(message: String, version: String = "v1"): String {
        val hash = signer.signMessage(message.toByteArray())
        val sigType = if (version == "v2") "eip191v2" else "eip191"
        return "$sigType:$hash"
    }
}
