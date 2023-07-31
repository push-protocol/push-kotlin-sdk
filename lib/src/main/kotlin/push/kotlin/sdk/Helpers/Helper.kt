package push.kotlin.sdk

import org.web3j.crypto.WalletUtils
import push.kotlin.sdk.Group.IsGroupChatId

class Helpers {
    companion object {

        fun walletToCAIP(env: ENV, address: String): String {
            if (env == ENV.prod) {
                return "eip155:1:${address}"
            }
            return "eip155:5:${address}"
        }

        fun walletToPCAIP(address: String): String {
            if(IsGroupChatId(address)){
                return  address
            }

            if(address.contains("eip155:")){
                return address
            }
            return "eip155:${address}"
        }

        fun walletsToPCAIP(addresses: List<String>): List<String> {
            return addresses.map { el -> Helpers.walletToPCAIP(el) }
        }

        fun decryptMessage(encryptedSecret: String, messageContent: String, pgpPrivateKey: String): String {
            val AESKey = Pgp.decrypt(encryptedSecret, pgpPrivateKey).getOrThrow()
            val message = AESCBC.decrypt(AESKey, messageContent)
            return message
        }

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
            if(address.contains("eip155:")){
                return  WalletUtils.isValidAddress(address.split(":")[1])
            }
            return WalletUtils.isValidAddress(address)
        }

    }
}


