
import com.google.crypto.tink.subtle.Hkdf
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec


fun byteArrayToHexString(byteArray: ByteArray): String {
    val hexString = StringBuilder()

    for (b in byteArray) {
        val st = String.format("%02X", b)
        hexString.append(st)
    }

    return hexString.toString().lowercase()
}


val TAG_LENGTH = 16

class AESGCM {
    companion object {
        private const val GCM_TAG_LENGTH = 16

        fun hexToBytes(hex: String): ByteArray {
            // We are expecting UnitBytes
            val bytes = mutableListOf<Byte>()
            // Temporary fix for the Ethereum Push Notification Service issue
            bytes.addAll(listOf(14, 0, 145, 0, 0).map { it.toByte() })

            // hex is of length -> 130, we need 65 got 70
            for (i in 0..hex.count()-2 step 2) {
                val sub = hex.substring(i, i + 2)
                val num = sub.toIntOrNull(16)?.toByte() ?: 0.toByte()
                bytes.add(num)
            }

            return bytes.toByteArray()
        }


        fun getSigToBytes(sig: String): ByteArray {
            val com = sig.split(":")[1]
            val remaining = com.drop(3)
            return hexToBytes(remaining)
        }


        fun encrypt(message: String, secret: String, nonceHex: String?=null, saltHex: String?=null): Triple<String,String,String>{
                val messageBytes = message.trim().toByteArray()
                val nonce =  if(nonceHex != null) hexStringToByteArray(nonceHex) else SecureRandom().generateSeed(32)
                val salt = if(saltHex !=null) hexStringToByteArray(saltHex) else SecureRandom().generateSeed(12)
                val sk = getSigToBytes(secret)

                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                val spec = GCMParameterSpec(TAG_LENGTH*8, nonce)
                val secretKey = generateSecretKey(sk, salt)

                cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)
                val result = cipher.doFinal(messageBytes)
                return Triple(
                    byteArrayToHexString(result),
                    byteArrayToHexString(salt),
                    byteArrayToHexString(nonce)
                )

        }

        fun decrypt(cipherHex: String, secret: String, nonceHex: String, saltHex: String): Result<String> {
            return try {
                val cipherData = hexStringToByteArray(cipherHex)
                val nonce = hexStringToByteArray(nonceHex)
                val salt = hexStringToByteArray(saltHex)
                val sk = getSigToBytes(secret)


                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                val gcmParameterSpec = GCMParameterSpec(TAG_LENGTH*8, nonce)
                val keySpec = generateSecretKey(sk, salt)

                cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec)

                Result.success(String(cipher.doFinal(cipherData), Charsets.UTF_8).trim())
            } catch (e: Exception) {
                Result.failure(e)
            }

        }

        private fun generateSecretKey(secret: ByteArray, salt: ByteArray): SecretKey {
            val derivedKey = Hkdf.computeHkdf("HMACSHA256", secret, salt, null, 32)
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