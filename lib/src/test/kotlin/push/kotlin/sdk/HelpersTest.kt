package push.kotlin.sdk
import kotlin.test.Test
import kotlin.test.assertEquals

class HelpersTest {
    @Test fun messageDecryptionTest() {
        val messageContent = "U2FsdGVkX18+UrnRfChnSk36MaqUQC7gD7r8aD2PKtI="
        val encryptedSecret = ENC_MESSAGE

        val decryptedMessage = Helpers.decryptMessage(encryptedSecret, messageContent, PGP_PK)
        assertEquals(decryptedMessage, "welcome to push")
    }

    @Test fun isEthValid() {
        val address1 = "0x355c8042605dEE474c1D5AF5705BC02Ae22351AB"
        val address2 = "0x355c804285dEE474a1D5AF57056BC02Ae22353AC"

       println(Helpers.islengthValid(address1))
        println(Helpers.isValidUrl(address2));
    }

    @Test fun isSigning() {
        val address = Helpers.ConnectWeb()
        println(address)
    }
    @Test fun personal_signature () {
        val message = "This is good place to find a city"
        val sig = PublickKeyBuilder.sign(message)
        println("Sig was $sig")
    }

    @Test fun verifaction() {
        val random = "sometsfhing".toByteArray()
        val sign = PublickKeyBuilder.verifyData(random)
        println("signature was $sign")
    }

    @Test fun signerAbstract() {
        val privateKey = "c39d17b1575c8d5e6e615767e19dc285d1f803d21882fb0c60f7f5b7edb759b2"

        val message = "Create Push Profile \n252f10c83610ebca1a059c0bae8255eba2f95be4d1d7bcfa89d7248a82d9f111".toByteArray()
        val signer = Signature(privateKey)
        val signature = signer.signMessage(message)
        val address = signer.getAddress()
        val expectedSignature = "0x9d71faa2582414160f3bc5b62bd8204b45e8ce60e42e034065366c27ed4f456d406ed157fec97db7cedb070f3488a0e78b6b59467063fab11aee6a2bf8233b131b"
        assertEquals(expectedSignature, signature)
    }

}