package push.kotlin.sdk

import org.junit.jupiter.api.Test
import push.kotlin.sdk.ProfileCreator.ProfileCreator

class ProfileCreatorTest {
  @Test fun preparePublicKey(){
    val pk = "c39d17b1575c8d5e6e615767e19dc285d1f803d21882fb0c60f7f5b7edb759b2"
    val publicKey = PGP_PUBLIC;

    val signer = PrivateKeySigner(pk)
    val preparedPublicKey = ProfileCreator.preparePGPPublicKey(publicKey, Wallet(signer));

    assert(preparedPublicKey.contains("key"))
    assert(preparedPublicKey.contains("signature"))
  }

  @Test fun encryptPrivateKey(){
    val pk = "c39d17b1575c8d5e6e615767e19dc285d1f803d21882fb0c60f7f5b7edb759b2"
    val signer = PrivateKeySigner(pk)
    val wallet = Wallet(signer)

    val encryptedKey = ProfileCreator.encryptPGPKey(PGP_PK, wallet)

    val encryptedKeyJsonString = encryptedKey.getJsonString()

    val decryptedKey = DecryptPgp.decryptPgpKey(encryptedKeyJsonString,signer).getOrThrow()
    assert(decryptedKey.contains("-----BEGIN PGP PRIVATE KEY BLOCK-----"))
  }

}