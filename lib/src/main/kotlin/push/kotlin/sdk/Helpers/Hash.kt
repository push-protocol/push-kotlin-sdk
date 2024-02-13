package push.kotlin.sdk.HahHelper

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.web3j.crypto.Hash


fun GenerateSHA256Hash(message:Any):String{
  val jsonString = GsonBuilder().serializeNulls().create().toJson(message)
  val utf8Bytes = jsonString.toByteArray(Charsets.UTF_8)
  val hashBytes = Hash.sha256(utf8Bytes)
  return hashBytes.joinToString("") { "%02x".format(it) }
}
