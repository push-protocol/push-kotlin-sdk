import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.generators.HKDFBytesGenerator
import org.bouncycastle.crypto.params.HKDFParameters
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class AESGCM {
    companion object {
        private const val GCM_TAG_LENGTH = 16

        fun hexToBytes(hex: String): ByteArray {
            val bytes = mutableListOf<Byte>()

            // Temporary fix for the Ethereum Push Notification Service issue
            bytes.addAll(listOf(14, 0, 145, 0, 0).map { it.toByte() })

            val sanitizedHex = hex.replace(Regex("[^0-9a-fA-F]"), "")
            val paddedHex = if (sanitizedHex.length % 2 != 0) "0$sanitizedHex" else sanitizedHex

            for (i in paddedHex.indices.step(2)) {
                val sub = paddedHex.substring(i, i + 2)
                val num = sub.toIntOrNull(16)?.toByte() ?: 0.toByte()
                bytes.add(num)
            }
            println(bytes)

            return bytes.toByteArray()
        }


        fun getSigToBytes(sig: String): ByteArray {
            val com = sig.split(":")[1]
            val remaining = com.drop(3)
            return hexToBytes(remaining)
        }

        fun decrypt(cipherHex: String, secret: String, nonceHex: String, saltHex: String): String {
            val cipherData = hexStringToByteArray(cipherHex)
            val nonce = hexStringToByteArray(nonceHex)
            val salt = hexStringToByteArray(saltHex)
            val sk = getSigToBytes(secret)

            val ciphertextBytes = cipherData.copyOfRange(0, cipherData.size - 16)
            val tag = cipherData.copyOfRange(cipherData.size - 16, cipherData.size)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val gcmParameterSpec = GCMParameterSpec(128, nonce)

            val secretKey = generateSecretKey(String(sk), salt)

            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec)
            val decryptedBytes = cipher.doFinal(ciphertextBytes)
            val decryptedText = String(decryptedBytes, StandardCharsets.UTF_8)

            return decryptedText
        }

        private fun generateSecretKey(secret: String, salt: ByteArray): SecretKey {
            val hkdf = HKDFBytesGenerator(SHA256Digest())
            val hkdfParameters = HKDFParameters(
                secret.toByteArray(StandardCharsets.UTF_8),
                salt,
                null
            )
            hkdf.init(hkdfParameters)

            val derivedKey = ByteArray(32)
            hkdf.generateBytes(derivedKey, 0, derivedKey.size)

            return SecretKeySpec(derivedKey, "AES")
        }

        private fun hexStringToByteArray(hexString: String): ByteArray {
            val sanitizedHexString = hexString.replace(Regex("[^0-9a-fA-F]"), "")
            val length = sanitizedHexString.length
            val paddedHexString = if (length % 2 != 0) "0$sanitizedHexString" else sanitizedHexString
            val byteArray = ByteArray(paddedHexString.length / 2)

            var i = 0
            while (i < paddedHexString.length) {
                byteArray[i / 2] = ((Character.digit(paddedHexString[i], 16) shl 4) +
                        Character.digit(paddedHexString[i + 1], 16)).toByte()
                i += 2
            }

            return byteArray
        }
    }
}