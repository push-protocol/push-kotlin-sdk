package push.kotlin.sdk

import org.bouncycastle.openpgp.PGPSecretKeyRing
import org.bouncycastle.util.io.Streams
import org.pgpainless.PGPainless
import org.pgpainless.algorithm.DocumentSignatureType
import org.pgpainless.decryption_verification.ConsumerOptions
import org.pgpainless.encryption_signing.EncryptionOptions
import org.pgpainless.encryption_signing.ProducerOptions
import org.pgpainless.encryption_signing.SigningOptions
import org.pgpainless.key.SubkeyIdentifier
import org.pgpainless.key.generation.type.rsa.RsaLength
import org.pgpainless.key.protection.SecretKeyRingProtector
import org.pgpainless.util.ArmorUtils
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream


class CustomException(message: String) : Exception(message) {
    override fun toString(): String {
        return message ?: "Unknown error occurred"
    }
}


class Pgp {

  companion object {
    fun getEcryptionOptions(userPublicKeys: List<String>):Result<EncryptionOptions>{
      val ecryptionOptions = EncryptionOptions()

      userPublicKeys.forEach { el ->
        val publicKey = PGPainless.readKeyRing().publicKeyRing(el) ?: return  Result.failure(IllegalStateException("Public key not found"))
        ecryptionOptions.addRecipient(publicKey)
      }

      return  Result.success(ecryptionOptions)
    }

    public fun encrypt(message: String, userPublicKeys: List<String>): Result<String> {
      return try {
        if (userPublicKeys.isEmpty()){
          throw IllegalStateException("Public keys should not be empty")
        }

        val decryptionOptions = getEcryptionOptions(userPublicKeys).getOrElse { exception -> return Result.failure(exception) }

        val outputStream = ByteArrayOutputStream()
        val encryptionStream = PGPainless.encryptAndOrSign()
                .onOutputStream(outputStream)
                .withOptions(
                    ProducerOptions.encrypt(
                       decryptionOptions
                    ).setAsciiArmor(true)
                )

        val inputStream = ByteArrayInputStream(message.toByteArray())
        Streams.pipeAll(inputStream, encryptionStream);
        encryptionStream.close();

        Result.success(outputStream.toByteArray().toString(Charsets.UTF_8))
      } catch (e: Exception) {
        Result.failure(e)
      }
    }

    public fun decrypt(encryptedMessage: String, pgpPrivateKey: String): Result<String> {
        return try {
            val secretKey:PGPSecretKeyRing = PGPainless.readKeyRing()
                .secretKeyRing(pgpPrivateKey) ?: throw IllegalStateException("Secret key not found");

            val decryptedInputStream = ByteArrayInputStream(encryptedMessage.toByteArray())
            val decryptionStream = PGPainless.decryptAndOrVerify()
                .onInputStream(decryptedInputStream)
                .withOptions(
                    ConsumerOptions().addDecryptionKey(secretKey)
                )

            val outputStream = ByteArrayOutputStream()
            Streams.pipeAll(decryptionStream, outputStream);
            decryptionStream.close();

            Result.success(outputStream.toString(Charsets.UTF_8))
        } catch (e: Exception) {
            Result.failure(e)
        }

    }

    public fun sign(pgpPrivateKey: String, message: String): Result<String> {
      return try {
        val secretKey: PGPSecretKeyRing = PGPainless.readKeyRing()
                .secretKeyRing(pgpPrivateKey) ?: throw IllegalStateException("Secret key not found");

        val protector = SecretKeyRingProtector.unprotectedKeys();

        val messageIn: InputStream = ByteArrayInputStream(message.toByteArray())

        val ignoreMe = ByteArrayOutputStream()
        val signingStream = PGPainless.encryptAndOrSign()
                .onOutputStream(ignoreMe)
                .withOptions(ProducerOptions.sign(SigningOptions.get()
                        .addDetachedSignature(protector, secretKey, DocumentSignatureType.CANONICAL_TEXT_DOCUMENT))
                        .setAsciiArmor(false)
                )

        Streams.pipeAll(messageIn, signingStream)
        signingStream.close()

        val result = signingStream.result

        val signingKey = PGPainless.inspectKeyRing(secretKey).signingSubkeys[0]
        val signature = result.detachedSignatures[SubkeyIdentifier(secretKey, signingKey.keyID)].iterator().next()

        var detachedSignature = ArmorUtils.toAsciiArmoredString(signature.encoded)
        detachedSignature = detachedSignature.replace("Version: PGPainless\n","")

        return Result.success(detachedSignature)
      }catch (e:Exception){
        Result.failure(e)
      }
    }

    public fun generate():Pair<String, String>{
        val secretKey: PGPSecretKeyRing = PGPainless.generateKeyRing().simpleRsaKeyRing(" ", RsaLength._2048)

        val privateKey = ArmorUtils.toAsciiArmoredString(secretKey.encoded)
        val publicKey = ArmorUtils.toAsciiArmoredString(secretKey.getPublicKey().encoded)

        return Pair(publicKey, privateKey)
    }
  }
}