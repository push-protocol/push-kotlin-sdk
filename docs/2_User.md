# restapi
This package gives access to Push Protocol (Push Nodes) APIs. Visit [Developer Docs](https://docs.push.org/developers) or [Push.org](https://push.org) to learn more.


## **Signer**
#### Signer is an abstract signer that can be implemented to provide signing functionality. This protocol provides two methods: getEip191Signature and getAddress

```kotlin
abstract class Signer {
    abstract fun getEip191Signature(message: String): Result<String>
    abstract fun getAddress(): Result<String>
}
```

#### For example, PrivateKey signer can be implemented as:
```kotlin
import org.web3j.crypto.Credentials
import org.web3j.crypto.Sign
import org.web3j.utils.Numeric

class PrivateKeySigner(private val privateKey: String) : Signer() {

    override fun getEip191Signature(message: String): Result<String> {
        return try {
            val credentials: Credentials = Credentials.create(privateKey)
            val signatureData = Sign.signPrefixedMessage(message.toByteArray(Charsets.UTF_8), credentials.ecKeyPair)
            val r = Numeric.toHexStringNoPrefix(signatureData.r)
            val s = Numeric.toHexStringNoPrefix(signatureData.s)
            val v = Numeric.toHexStringNoPrefix(signatureData.v)
            val signature = r + s + v
            Result.success("0x$signature")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAddress(): Result<String> {
        return try {
            val credentials: Credentials = Credentials.create(privateKey)
            Result.success(credentials.address)
        } catch (e: Exception) {
            Result.failure(e)
        }
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
    
    val decryptedPgpKey:Result<String> = PushUser.DecryptPgp.decryptPgpKey(user.encryptedPrivateKey, signer)
```

---
### **Update User Profie**
```kotlin
val pushUser:PushUser.UserProfile = PushUser.getUser(USER_ETH_ADDRESS, ENV.staging) 

// update user profile
user.profile.name = "New User Name"
user.profile.desc = "New User Desc"
user.profile.picture = "New IMAGE BASE64"

val res:Result<Boolean> = PushUser.updateUser(
    USER_ETH_ADDRESS,
    user.profile,
    USER_PGP_PRIVATE_KEY,
    ENV.staging
)
```

---
### **Block Users**
```kotlin
val pushUser:PushUser.UserProfile = PushUser.getUser(USER_ETH_ADDRESS, ENV.staging) 

val res:Result<Boolean> = PushUser.blockUser(
    USER_ETH_ADDRESS,
    USER_PGP_PRIVATE_KEY,
    listOf(
        ETH_ADDRESS_1_TO_BLOCK, 
        ETH_ADDRESS_2_TO_BLOCK, 
        ETH_ADDRESS_3_TO_BLOCK
    ),
    ENV.staging
)
```

---
### **UnBlock Users**
```kotlin
val pushUser:PushUser.UserProfile = PushUser.getUser(USER_ETH_ADDRESS, ENV.staging) 

val res:Result<Boolean> = PushUser.unblockUser(
    USER_ETH_ADDRESS,
    USER_PGP_PRIVATE_KEY,
    listOf(
        ETH_ADDRESS_1_TO_UNBLOCK, 
        ETH_ADDRESS_2_TO_UNBLOCK, 
    ),
    ENV.staging
)
```