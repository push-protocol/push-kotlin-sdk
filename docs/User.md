# restapi
This package gives access to Push Protocol (Push Nodes) APIs. Visit [Developer Docs](https://docs.push.org/developers) or [Push.org](https://push.org) to learn more.


## **Signer**
#### Signer is an abstract signer that can be implemented to provide signing functionality. This protocol provides two methods: getEip191Signature and getAddress

```kotlin
abstract class Signer {
    abstract fun getEip191Signature(message: String): String
    abstract fun getAddress(): String
}
```

#### For example, PrivateKey signer can be implemented as:
```kotlin
import org.web3j.crypto.Credentials
import org.web3j.crypto.Sign
import org.web3j.utils.Numeric

class PrivateKeySigner(private val privateKey: String): Signer(){
    override fun getEip191Signature(message: String): String {
        val credentials: Credentials = Credentials.create(privateKey)
        val signatureData = Sign.signPrefixedMessage(message.toByteArray(Charsets.UTF_8), credentials.ecKeyPair)
        val r = Numeric.toHexStringNoPrefix(signatureData.r)
        val s = Numeric.toHexStringNoPrefix(signatureData.s)
        val v = Numeric.toHexStringNoPrefix(signatureData.v)
        val signature = r + s + v
        return "0x$signature"
    }

    override fun getAddress(): String {
        val credentials: Credentials = Credentials.create(privateKey)
        return credentials.address
    }
}
```

## Chat User

### **Get user data for chat**

```kotlin
  val userAddress = "0xD26A7BF7fa0f8F1f3f73B056c9A67565A6aFE63c"
  val user:PushUser.UserProfile? = PushUser.getUser(userAddress, ENV.staging)
```

---

### **Decrypting encrypted pgp private key from user data**
```kotlin
    val signer = PrivateKeySigner(ETH_PRIVATE_KEY_STRING)
    val user:PushUser.UserProfile? = PushUser.getUser(userAddress, ENV.staging)
    
    val decryptedPgpKey = PushUser.DecryptPgp.decryptPgpKey(user.encryptedPrivateKey, signer)
```