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

        fun getSignatureBytes(sig: SignatureData): Result<ByteArray> {
            return try {
                val v = sig.v[0]
                val fixedV = if (v >= 27) (v - 27).toByte() else v
                Result.success(merge(
                    sig.r,
                    sig.s,
                    byteArrayOf(fixedV))
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        private const val MESSAGE_PREFIX = "\u0019Ethereum Signed Message:\n"
        val privateKey = "c39d17b1575c8d5e6e615767e19dc285d1f803d21882fb0c60f7f5b7edb759b2"
        fun ethHash(message: String): Result<ByteArray> {
            return try {
                val input = MESSAGE_PREFIX + message.length + message
                Result.success(Util.keccak256(input.toByteArray()))
            } catch (e: Exception) {
                Result.failure(e)
            }

        }

        fun byteArrayToHexString(byteArray: ByteArray): String {
            val hexString = StringBuilder()
            for (byte in byteArray) {
                val hexValue = "%02x".format(byte)
                hexString.append(hexValue)
            }
            return "0x$hexString"
        }

        fun verifyData(data: ByteArray): Result<String> {
            return try {
                val ecKeyPair = ECKeyPair.create(privateKey.toBigInteger(16))
                val signatureData = Sign.signMessage(data, ecKeyPair, false)
                val signatureKey = getSignatureBytes(signatureData).getOrThrow()
                val randomButes = signatureKey.take(64).toByteArray()
                Result.success(byteArrayToHexString(randomButes))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        fun sign(message: String): String {
            val digest = ethHash(message).getOrThrow()
            return verifyData(digest).getOrThrow()
        }
    }
}
