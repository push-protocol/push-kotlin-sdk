package push.kotlin.sdk.PushAPI

import push.kotlin.sdk.DecryptPgp
import push.kotlin.sdk.ENV
import push.kotlin.sdk.PushNotification.ChannelAPI
import push.kotlin.sdk.PushUser
import push.kotlin.sdk.Signer

class PushAPI(
        private var env: ENV,
        private var account: String,
        private var readMode: Boolean,
        private var decryptedPgpPvtKey: String,
        private var pgpPublicKey: String?,
        private var signer: Signer,
) {
    var chat: Chat
    var profile: Profile
    var channel: ChannelAPI

    init {
        chat = Chat(account, env, decryptedPgpPvtKey, signer)
        profile = Profile(account, env, decryptedPgpPvtKey, signer)
        channel = ChannelAPI(account, env, decryptedPgpPvtKey, signer)
    }


    data class PushAPIInitializeOptions(
            val version: PushUser.ENCRYPTION_TYPE = PushUser.ENCRYPTION_TYPE.PGP_V3,
            val versionMeta: Map<String, Map<String, String>>? = null,
            val autoUpgrade: Boolean = true,
            val origin: String? = null,
            val showHttpLog: Boolean = false,
            val env: ENV = ENV.prod
    )


    companion object {
        @Throws(IllegalArgumentException::class)
        fun initialize(signer: Signer, options: PushAPIInitializeOptions = PushAPIInitializeOptions()): PushAPI {

            // Get account
            // Derives account from signer if not provided
            val derivedAccount = signer.getAddress().getOrThrow();

            val decryptedPGPPrivateKey: String?
            val pgpPublicKey: String?

            /**
             * Decrypt PGP private key
             * If user exists, decrypts the PGP private key
             * If user does not exist, creates a new user and returns the decrypted PGP private key
             */
            val user = PushUser.getUser(derivedAccount, options.env)

            if (user != null) {
                decryptedPGPPrivateKey = DecryptPgp.decryptPgpKey(user.encryptedPrivateKey, signer).getOrThrow()
                pgpPublicKey = user.publicKey;
            } else {
                val newUser = PushUser.createUser(signer, options.env).getOrThrow()
                decryptedPGPPrivateKey = DecryptPgp.decryptPgpKey(newUser.encryptedPrivateKey, signer).getOrThrow()
                pgpPublicKey = newUser.publicKey;
            }

            return PushAPI(options.env, derivedAccount, readMode = true, decryptedPGPPrivateKey, pgpPublicKey, signer)


        }
    }

}