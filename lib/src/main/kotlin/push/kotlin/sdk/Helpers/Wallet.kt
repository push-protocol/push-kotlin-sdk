package push.kotlin.sdk


class Wallet(private val signer: Signer) {

    fun getEip191Signature(message: String, version: String = "v1"): Result<String> {
        return try {
            val sig = signer.getEip191Signature(message).getOrThrow()
            val sigType = if (version == "v2") "eip191v2" else "eip191"
            Result.success("$sigType:$sig")
        } catch (e: Exception) {
            Result.failure(e)
        }

    }
}
