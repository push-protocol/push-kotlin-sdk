package push.kotlin.sdk.HahHelper

import com.google.gson.Gson
import org.web3j.crypto.Hash


fun GenerateSHA256Hash(message:Any):String{
  val jsonString = Gson().toJson(message)
  val utf8Bytes = jsonString.toByteArray(Charsets.UTF_8)
  val hashBytes = Hash.sha256(utf8Bytes)
  return hashBytes.joinToString("") { "%02x".format(it) }
}
