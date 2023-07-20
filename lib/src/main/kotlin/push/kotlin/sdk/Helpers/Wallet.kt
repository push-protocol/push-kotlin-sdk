package push.kotlin.sdk


class Wallet(private val signer: Signer) {

    fun getEip191Signature(message: String, version: String = "v1"): String {
        val sig = signer.getEip191Signature(message)
        val sigType = if (version == "v2") "eip191v2" else "eip191"
        return "$sigType:$sig"
    }
}
