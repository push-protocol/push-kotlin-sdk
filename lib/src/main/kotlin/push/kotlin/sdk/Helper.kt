package push.kotlin.sdk

import kotlinx.coroutines.runBlocking
import org.web3j.crypto.*
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Numeric
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class Helpers {
    companion object {
        fun walletToCAIP(address: String): String {
            return "eip155:${address}"
        }

        fun decryptMessage(encryptedSecret: String, messageContent: String, pgpPrivateKey: String): String {
            val AESKey = Pgp.decrypt(encryptedSecret, pgpPrivateKey)
            val message = AESCBC.decrypt(AESKey, messageContent)
            return message
        }

//        fun isEthAddressValid(address: String): Boolean {
//            return WalletUtils.isValidAddress(address)
//        }

        fun islengthValid(data: String, upperLen: Int? = null, lowerLen: Int = 1): Boolean {
            val upper = upperLen ?: Int.MAX_VALUE
            return data.length in lowerLen..upper
        }

        fun isValidUrl(urlString: String): Boolean {
            val urlPattern = Regex(
                "^((?:https|http):\\/\\/)" +                // validate protocol
                        "((([a-z\\d]([a-z\\d-]*[a-z\\d])*)\\.)+[a-z]{2,}|" +     // validate domain name
                        "((\\d{1,3}\\.){3}\\d{1,3}))" +                          // validate OR ip (v4) address
                        "(\\:\\d+)?(\\/[-a-z\\d%_.~+]*)*" +                      // validate port and path
                        "(\\?[;&a-z\\d%_.~+=-]*)?" +                             // validate query string
                        "(\\#[-a-z\\d_]*)?\$",
                RegexOption.IGNORE_CASE
            )
            return urlPattern.matches(urlString)

        }

        fun isValidAddress(address: String): Boolean {
            return WalletUtils.isValidAddress(address)
        }

        fun ConnectWeb() {
            val web3j = Web3j.build(HttpService("https://mainnet.infura.io/v3/b64677a9958b450084777c0a529447b1"))

            val privateKey = "b21a68f75f65a6b33cf32f2bb36f0bb93be1c185cb5861d172f1cba2d2cfd129"

            val credentials = Credentials.create(privateKey)

            val address = credentials.address
            println("Addree: $address")

            val publicKey = credentials.ecKeyPair.publicKey
            println("Public Key: $publicKey")

            val signerPrivateKey = credentials.ecKeyPair.privateKey
            println("Private Key: $signerPrivateKey")

            val transaction = web3j.ethGetTransactionCount(address, DefaultBlockParameterName.LATEST).sendAsync().get()
            val nonce = transaction.transactionCount
            val gasPrice = BigInteger("200")
            val gasLimit = BigInteger("20")
            val rawTransaction = RawTransaction.createEtherTransaction(
                nonce,
                gasPrice,
                gasLimit,
                "recipient-address",
                BigInteger("10000000")
            )
            val signedTransaction = TransactionEncoder.signMessage(rawTransaction, credentials)
            val hexValue = Numeric.toHexString(signedTransaction)

            println("signed Transaction $hexValue")
        }

        fun Signature() {

        }

        fun personalSignature() {
                val privateKey = "b21a68f75f65a6b33cf32f2bb36f0bb93be1c185cb5861d172f1cba2d2cfd129"
                val web3j = Web3j.build(HttpService("https://mainnet.infura.io/v3/b64677a9958b450084777c0a529447b1"))

                val message = Hash.sha256("Hello world".toByteArray(StandardCharsets.UTF_8))

//.toByteArray(StandardCharsets.UTF_8)
//                val signature = Sign.signMessage(message, credentials.ecKeyPair)
            val signature = runBlocking { privateKey }

//            val signature = web3j.ethSign(credentials.address, message).send().result
            println("Signature: $signature")
        }

        private fun normalizeSignatureForVerification(signature: ByteArray): ByteArray {
            val r: ByteArray = BigInteger(1, Arrays.copyOfRange(signature, 0, 32)).toByteArray()
            val s: ByteArray = BigInteger(1, Arrays.copyOfRange(signature, 32, 64)).toByteArray()
            val der = ByteArray(6 + r.size + s.size)
            der[0] = 0x30

            der[1] = (der.size - 2).toByte()

            var o = 2
            der[o++] = 0x02
            der[o++] = r.size.toByte()

            System.arraycopy(r, 0,der, 0, r.size)
            o += r.size
            der[o++] = 0x02
            der[o++] = s.size.toByte()

            System.arraycopy(s, 0, der, o, s.size)

            return der
        }

        fun deriveAesParams(password: String, salt: ByteArray, iterationCount: Int, keyLength: Int): SecretKeySpec {
            val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val spec = PBEKeySpec(password.toCharArray(), salt, iterationCount, keyLength)
            val secretKey = secretKeyFactory.generateSecret(spec)
            return SecretKeySpec(secretKey.encoded, "AES")
        }

        fun encryptWithAes(message: String, aesParams: SecretKeySpec, iv: ByteArray): ByteArray {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val ivParams = IvParameterSpec(iv)
            cipher.init(Cipher.ENCRYPT_MODE, aesParams, ivParams)
            return  cipher.doFinal(message.toByteArray())
        }
    }
}


