package push.kotlin.sdk.JsonHelpers

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun GetJsonStringFromKV(tuples:List<Pair<String,String>>):String{
  val dictionary = mutableMapOf<String, String>()
  for ((key, value) in tuples) {
    dictionary[key] = value
  }

  return Json.encodeToString(dictionary)
}
