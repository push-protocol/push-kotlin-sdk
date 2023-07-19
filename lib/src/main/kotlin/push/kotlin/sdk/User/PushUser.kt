package push.kotlin.sdk

import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request

class PushUser {

    data class ProfileInfo(
            val verificationProof: String?,
            val picture:String?,
            val name:String?,
            val desc:String?,
            val blockedUsersList:Array<String>?,
    )

    data class UserProfile(
            val did: String,
            val walllets: String,
            val pubicKey: String,
            val encryptedPrivateKey: String,
            val verificationProof: String,
            val msgSent: Number,
            val maxMsgPersisted:Number,
            val profile: PushUser.ProfileInfo
    )
    companion object{
        public  fun getUser(userAddress:String, env: ENV): PushUser.UserProfile?{
            val url =  PushURI.getUser(env, userAddress)

            // Create an OkHttpClient instance
            val client = OkHttpClient()

            // Create a request object
            val request = Request.Builder()
                    .url(url)
                    .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val jsonResponse = response.body?.string()
                val gson = Gson()
                val apiResponse = gson.fromJson(jsonResponse, PushUser.UserProfile::class.java)
                return apiResponse
            } else {
                println("Error: ${response.code} ${response.message}")
            }

            // Close the response body
            response.close()
            return null
        }
    }
}