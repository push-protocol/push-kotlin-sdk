package push.kotlin.sdk

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class AESCBC{
  companion object{
    public fun decrypt(passPhrase: String, ciphertextCombined: String):String{
      val (originalKey, iv, ciphertext) = AESCBC.getAESParams(ciphertextCombined, passPhrase)
      return AESCBC.decryptAESCBC(ciphertext, originalKey, iv)
    }

    public  fun getRandomString(length: Int): String {
      val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
      return (1..length)
              .map { allowedChars.random() }
              .joinToString("")
    }

    public fun encrypt(passPhrase: String, dataToEncrypt:String):String{
      val cipherTextPrefix = "Salted__".toByteArray()
      val salt = SecureRandom().generateSeed(8)

      val messageData = dataToEncrypt.toByteArray()
      val passcode = passPhrase.toByteArray()

      val (aesSecret, IV) = getAESParamsEnc(passcode, salt)

      val encrypted = _encrypt(messageData,aesSecret,IV)
      val cipherPacked = cipherTextPrefix + salt + encrypted

      return String(Base64.getEncoder().encode(cipherPacked))

    }

    public fun _encrypt(data: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
      val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")

      val secretKey = SecretKeySpec(key, "AES")
      val ivParams = IvParameterSpec(iv)

      cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParams)

      return cipher.doFinal(data)
    }

    private  fun getAESParamsEnc(passcode: ByteArray, salt: ByteArray): Pair<ByteArray, ByteArray > {

      fun genMd5(data: ByteArray): ByteArray {
        return MessageDigest.getInstance("MD5").digest(data)
      }

      var derived = ByteArray(0)
      while (derived.size < 48) {
        var toHash = if (derived.size > 16) {
          derived.copyOfRange(derived.size - 16, derived.size)
        } else {
          derived
        }

        toHash += passcode + salt

        val hash = genMd5(toHash)

        derived += hash
      }

      val originalkey = derived.copyOfRange(0, 32)
      val iv = derived.copyOfRange(32, derived.size)

      return Pair(originalkey, iv )
    }

    private  fun getAESParams(ciphertextCombined: String, passPhrase: String): Triple<ByteArray, ByteArray, ByteArray> {
      val cipherDataAll = Base64.getDecoder().decode(ciphertextCombined)
      val passcode = passPhrase.toByteArray(Charsets.UTF_8)
      val salt = cipherDataAll.copyOfRange(8, 16)
      val ciphertext = cipherDataAll.copyOfRange(16, cipherDataAll.size)

      fun genMd5(data: ByteArray): ByteArray {
        return MessageDigest.getInstance("MD5").digest(data)
      }

      var derived = ByteArray(0)
      while (derived.size < 48) {
        var toHash = if (derived.size > 16) {
          derived.copyOfRange(derived.size - 16, derived.size)
        } else {
          derived
        }

        toHash += passcode + salt

        val hash = genMd5(toHash)

        derived += hash
      }

      val originalkey = derived.copyOfRange(0, 32)
      val iv = derived.copyOfRange(32, derived.size)

      return Triple(originalkey, iv, ciphertext)
    }

    private  fun decryptAESCBC(encryptedMessage: ByteArray, key: ByteArray, iv: ByteArray): String {
      val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")


      val secretKey = SecretKeySpec(key, "AES")
      val ivParams = IvParameterSpec(iv)

      cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParams)


      val decrypted = cipher.doFinal(encryptedMessage)

      return decrypted.toString(Charsets.UTF_8)
    }
  }
}