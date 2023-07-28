package push.kotlin.sdk.JsonHelpers

import com.google.gson.Gson
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive

fun GetJsonStringFromKV(tuples:List<Pair<String,String>>):String{
  val dictionary = mutableMapOf<String, String>()
  for ((key, value) in tuples) {
    dictionary[key] = value
  }

  return Json.encodeToString(dictionary)
}

fun GetJsonStringFromGenericKV(tuples:List<Pair<String,JsonPrimitive>>):String{
  val dictionary = mutableMapOf<String, JsonPrimitive>()
  for ((key, value) in tuples) {
    dictionary[key] = value
  }

  return Json.encodeToString(dictionary)
}


fun ListToJsonString(lists:List<String>):String{
  return Gson().toJson(lists)
}