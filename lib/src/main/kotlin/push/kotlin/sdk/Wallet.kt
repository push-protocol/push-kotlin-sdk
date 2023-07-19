package push.kotlin.sdk

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class Wallet(private val signer: Signature, privateKey: String) {
    private var account: String? = null

    init {
        CoroutineScope(Dispatchers.Default).launch {
            account = signer.getAddress(privateKey)
        }
    }

    fun getEip191Signature(privateKey: String,message: String, version: String = "v1"): String {
        val hash = signer.getEipSignature(privateKey,message)
        val sigType = if (version == "v2") "eip191v2" else "eip191"
        return "$sigType:$hash"
    }
}
