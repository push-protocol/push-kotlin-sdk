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
        val sign = Helpers.personalSignature()
        println(sign)
    }

    @Test fun verifaction() {
        val random = "sometsfhing".toByteArray()
        val sign = PublickKeyBuilder.verifyData(random)
        println(sign)
    }

}