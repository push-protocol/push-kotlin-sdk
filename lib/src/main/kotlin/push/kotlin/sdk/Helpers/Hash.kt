package push.kotlin.sdk.HahHelper

import java.security.MessageDigest

fun GenerateSHA256Hash(message:String):String{
  val messageDigest = MessageDigest.getInstance("SHA-256")
  val byteArray = messageDigest.digest(message.toByteArray())
  return byteArray.joinToString("") { "%02x".format(it) }
}
