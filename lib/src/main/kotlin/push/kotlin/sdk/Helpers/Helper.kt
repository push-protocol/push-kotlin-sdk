package push.kotlin.sdk

import org.web3j.crypto.WalletUtils

class Helpers {
    companion object {
        fun walletToCAIP(address: String): String {
            return "eip155:${address}"
        }

        fun decryptMessage(encryptedSecret: String, messageContent: String, pgpPrivateKey: String): String {
            val AESKey = Pgp.decrypt(encryptedSecret, pgpPrivateKey).getOrThrow()
            val message = AESCBC.decrypt(AESKey.toString(), messageContent)
            return message
        }

//        fun isEthAddressValid(address: String): Boolean {
//            return WalletUtils.isValidAddress(address)
//        }

        fun islengthValid(data: String, upperLen: Int? = null, lowerLen: Int = 1): Boolean {
            val upper = upperLen ?: Int.MAX_VALUE
            return data.length in lowerLen..upper
        }

        fun isValidUrl(urlString: String): Boolean {
            val urlPattern = Regex(
                "^((?:https|http):\\/\\/)" +                // validate protocol
                        "((([a-z\\d]([a-z\\d-]*[a-z\\d])*)\\.)+[a-z]{2,}|" +     // validate domain name
                        "((\\d{1,3}\\.){3}\\d{1,3}))" +                          // validate OR ip (v4) address
                        "(\\:\\d+)?(\\/[-a-z\\d%_.~+]*)*" +                      // validate port and path
                        "(\\?[;&a-z\\d%_.~+=-]*)?" +                             // validate query string
                        "(\\#[-a-z\\d_]*)?\$",
                RegexOption.IGNORE_CASE
            )
            return urlPattern.matches(urlString)

        }

        fun isValidAddress(address: String): Boolean {
            return WalletUtils.isValidAddress(address)
        }

    }
}


