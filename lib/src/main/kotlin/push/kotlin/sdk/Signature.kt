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


class Signature() {
    fun getEipSignature(privateKey: String,message: String): String {
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

//    fun userDecryptPgpKey(encryptedPrivateKey: String, signer: Signature, privateKey: String): String {
////        val signature = Signature()
//        val wallet = Wallet(signer, privateKey)
//        println("$wallet wallet")
//        val pp = Json.decodeFromString<EncryptedPrivateKey>(encryptedPrivateKey)

//        val secret = wallet.getEip191Signature(privateKey,"Enable Push Profile")
//        val pgpPrivateKey = AESGCM.decrypt(pp.ciphertext, secret,pp.nonce, pp.salt)

//        return "pgpPrivateKey"
//    }

}
