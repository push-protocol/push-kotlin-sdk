package push.kotlin.sdk.PushAPI

import push.kotlin.sdk.ENV
import push.kotlin.sdk.PushUser
import push.kotlin.sdk.Signer

class Profile(
        private val account: String,
        val env: ENV,
        private val decryptedPgpPvtKey: String,
        private val signer: Signer,
) {
    fun info(overrideAccount: String? = null): PushUser.UserProfile? {
        return PushUser.getUser(overrideAccount ?: account, env);
    }

    fun update(
            name: String? = null, desc: String? = null, picture: String? = null
    ): Result<Boolean> {
        val info = info();
        val profile = info!!.profile;

        profile.name = name ?: profile.name
        profile.desc = desc ?: profile.desc
        profile.picture = picture ?: profile.picture

        return PushUser.updateUser(account, profile, decryptedPgpPvtKey, env);
    }

}