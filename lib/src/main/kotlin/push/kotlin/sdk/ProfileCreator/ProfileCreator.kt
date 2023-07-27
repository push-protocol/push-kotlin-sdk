package push.kotlin.sdk.ProfileCreator

import AESGCM
import push.kotlin.sdk.HahHelper.GenerateSHA256Hash
import push.kotlin.sdk.JsonHelpers.GetJsonStringFromKV
import push.kotlin.sdk.PushUser
import push.kotlin.sdk.Wallet
import java.security.SecureRandom


fun ByteArray.toHexString(): String {
  val hexString = StringBuilder()
  for (byte in this) {
    hexString.append(String.format("%02x", byte))
  }
  return hexString.toString()
}


class ProfileCreator {
  companion object {
    public fun preparePGPPublicKey(publicKey:String, wallet:Wallet):String{
      val messageToSign = "Create Push Profile \n" + GenerateSHA256Hash(publicKey)
      val verificationProof = wallet.getEip191Signature(messageToSign).getOrThrow()

      val preparedKey = GetJsonStringFromKV(listOf(
        "key" to publicKey,
        "signature" to verificationProof
      ))

      return  preparedKey
    }

    public fun encryptPGPKey(privateKey:String, wallet: Wallet):PushUser.EncryptedPrivateKey{
      val preKey = SecureRandom().generateSeed(32).toHexString()
      val messageToSign = "Enable Push Profile \n" + preKey
      val encryptionSecret = wallet.getEip191Signature(messageToSign).getOrThrow()

      val (cipher,salt,nonce) = AESGCM.encrypt(privateKey, encryptionSecret)
      return PushUser.EncryptedPrivateKey(
        cipher,
        salt,
        nonce,
        PushUser.ENCRYPTION_TYPE.PGP_V3,
        preKey
      )
    }

    public fun getCreateUserVerificationProof(wallet: Wallet, userAddress:String, publicKey: String, encryptedPrivateKey:String):String{
      val jsonString =  GetJsonStringFromKV(listOf(
        "caip10" to userAddress,
        "did" to userAddress,
        "public" to publicKey,
        "encryptedPrivateKey" to encryptedPrivateKey
      ))

      val hash = GenerateSHA256Hash(jsonString)
      return wallet.getEip191Signature(hash, "v2").getOrThrow()
    }
  }
}