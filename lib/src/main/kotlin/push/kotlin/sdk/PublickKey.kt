package push.kotlin.sdk

import org.bouncycastle.jcajce.provider.digest.Keccak
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Sign
import org.web3j.crypto.Sign.SignatureData

class Util {
    companion object {
        fun keccak256(data: ByteArray): ByteArray {
            val digest256 = Keccak.Digest256()
            return digest256.digest(data)
        }
    }
}

class PublickKeyBuilder {
    companion object {
        private fun merge(vararg arrays: ByteArray): ByteArray {
            var arrCount = 0
            var count = 0
            for (array in arrays) {
                arrCount++
                count += array.size
            }

            // Create new array and copy all array contents
            val mergedArray = ByteArray(count)
            var start = 0
            for (array in arrays) {
                System.arraycopy(array, 0, mergedArray, start, array.size)
                start += array.size
            }
            return mergedArray
        }

        fun getSignatureBytes(sig: SignatureData): ByteArray {
            val v = sig.v[0]
            val fixedV = if (v >= 27) (v - 27).toByte() else v
            return merge(
                sig.r,
                sig.s,
                byteArrayOf(fixedV),
            )
        }

        private const val MESSAGE_PREFIX = "\u0019Ethereum Signed Message:\n"
        val privateKey = "c39d17b1575c8d5e6e615767e19dc285d1f803d21882fb0c60f7f5b7edb759b2"
        fun ethHash(message: String): ByteArray {
            val input = MESSAGE_PREFIX + message.length + message
            return Util.keccak256(input.toByteArray())
        }

        fun byteArrayToHexString(byteArray: ByteArray): String {
            val hexString = StringBuilder()
            for (byte in byteArray) {
                val hexValue = "%02x".format(byte)
                hexString.append(hexValue)
            }
            return "0x$hexString"
        }

        fun verifyData(data: ByteArray): String {
            val ecKeyPair = ECKeyPair.create(privateKey.toBigInteger(16))
            val signatureData = Sign.signMessage(data, ecKeyPair, false)
            val signatureKey = getSignatureBytes(signatureData)
            val randomButes = signatureKey.take(64).toByteArray()
            return byteArrayToHexString(randomButes)
        }

        fun sign(message: String): String {
            val digest = ethHash(message)
            return verifyData(digest)
        }
    }
}
