package push.kotlin.sdk.HahHelper

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.web3j.crypto.Hash


import java.security.MessageDigest

fun GenerateSHA256Hash(message:String):String{
  val jsonString = Gson().toJson(message)
  val utf8Bytes = jsonString.toByteArray(Charsets.UTF_8)
  val hashBytes = Hash.sha256(utf8Bytes)
  return hashBytes.joinToString("") { "%02x".format(it) }
}

fun GenerateSHA256Hash(message:Any):String{
  val jsonString = GsonBuilder().serializeNulls().create().toJson(message)
  val utf8Bytes = jsonString.toByteArray(Charsets.UTF_8)
  val hashBytes = Hash.sha256(utf8Bytes)
  return hashBytes.joinToString("") { "%02x".format(it) }
}

fun GenerateSHA256Hash_(message:String ):String{
//  val jsonString = Json.encodeToString(message)
//  println(jsonString)
//  val bytes = jsonString.toByteArray(Charsets.UTF_8)
  return  MessageDigest
          .getInstance("SHA-256")
          .digest(message.toByteArray())
          .fold("", { str, it -> str + "%02x".format(it) })

//  val digest = MessageDigest.getInstance("SHA-256")
//  val hashBytes = digest.digest(bytes)
//
//  // Convert the byte array to a hexadecimal string
//  val hexString = StringBuilder()
//  for (byte in hashBytes) {
//    val hex = Integer.toHexString(0xff and byte.toInt())
//    if (hex.length == 1) {
//      hexString.append('0')
//    }
//    hexString.append(hex)
//  }
//  return hexString.toString()
}

