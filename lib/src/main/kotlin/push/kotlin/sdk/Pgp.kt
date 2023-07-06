package push.kotlin.sdk

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openpgp.PGPException
import org.bouncycastle.openpgp.PGPPublicKeyRing
import org.bouncycastle.openpgp.PGPSecretKeyRing
import org.bouncycastle.openpgp.PGPSignature
import org.bouncycastle.util.io.Streams
import org.pgpainless.PGPainless
import org.pgpainless.algorithm.HashAlgorithm
import org.pgpainless.algorithm.SignatureType
import org.pgpainless.decryption_verification.ConsumerOptions
import org.pgpainless.encryption_signing.BcHashContextSigner
import org.pgpainless.encryption_signing.EncryptionOptions
import org.pgpainless.encryption_signing.ProducerOptions
import org.pgpainless.key.protection.SecretKeyRingProtector
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import org.pgpainless.util.ArmorUtils;



class Pgp {

  companion object {
//
//        val userPublicKey:PGPPublicKeyRing = PGPainless.readKeyRing()
//                .publicKeyRing(UserPublicKey) ?: throw IllegalStateException("Secret key not found");
//
//

    public fun encrypt(message: String, userPublicKey: PGPPublicKeyRing): String {
      val outputStream = ByteArrayOutputStream()
      val encryptionStream = PGPainless.encryptAndOrSign()
              .onOutputStream(outputStream)
              .withOptions(
                      ProducerOptions.encrypt(
                              EncryptionOptions().addRecipient(userPublicKey)
                      ).setAsciiArmor(true)
              )

      val inputStream = ByteArrayInputStream(message.toByteArray())
      Streams.pipeAll(inputStream, encryptionStream);
      encryptionStream.close();

      return outputStream.toByteArray().toString(Charsets.UTF_8)
    }

    public fun decrypt(encryptedMessage: String, pgpPrivateKey: String): String {
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

      return outputStream.toString(Charsets.UTF_8)
    }

    public fun sign(pgpPrivateKey: String, message: String): String {
      val outputStream = ByteArrayOutputStream()
      val secretKeys:PGPSecretKeyRing = PGPainless.readKeyRing()
              .secretKeyRing(pgpPrivateKey) ?: throw IllegalStateException("Secret key not found");

      val messageBytes: ByteArray = message.toByteArray()
      val messageIn = ByteArrayInputStream(messageBytes)

      val signature: PGPSignature = signMessage(messageBytes, HashAlgorithm.SHA256, secretKeys) ?: throw IllegalStateException("Secret key not found");

      return ArmorUtils.toAsciiArmoredString(signature)
    }

    @Throws(NoSuchAlgorithmException::class, PGPException::class)
    private fun signMessage(message: ByteArray, hashAlgorithm: HashAlgorithm, secretKeys: PGPSecretKeyRing): PGPSignature? {
      // Prepare the hash context
      // This would be done by the caller application
      val messageDigest = MessageDigest.getInstance(hashAlgorithm.algorithmName, BouncyCastleProvider())
      messageDigest.update(message)
      return BcHashContextSigner.signHashContext(messageDigest, SignatureType.BINARY_DOCUMENT, secretKeys, SecretKeyRingProtector.unprotectedKeys())
    }

  }
}