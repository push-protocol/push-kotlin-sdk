package push.kotlin.sdk

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class DecryptPgp {
    fun userDecryptPgpKey(encryptedPrivateKey: String, signer: Signer): String {
        val signature = Signature(privateKey)
        val wallet = Wallet(signature)
        val pp = Json.decodeFromString<EncryptedPrivateKey>(encryptedPrivateKey)

        val secret = wallet.getEip191Signature("Enable Push Profile \\n${pp.preKey}")
        val pgpPrivateKey = AESGCM.decrypt(pp.ciphertext, secret,pp.nonce, pp.salt)

        return pgpPrivateKey
    }
}