package push.kotlin.sdk.Group

import com.google.gson.Gson
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import push.kotlin.sdk.*
import push.kotlin.sdk.HahHelper.GenerateSHA256Hash
import push.kotlin.sdk.JsonHelpers.GetJsonStringFromGenericKV
import push.kotlin.sdk.JsonHelpers.ListToJsonString

@Throws(IllegalArgumentException::class)
fun createGroupOptionValidator(option: PushGroup.CreateGroupOptions) {
  if (option.name.isEmpty()) {
    throw IllegalArgumentException("groupName cannot be null or empty")
  }

  if (option.name.length > 50) {
    throw IllegalArgumentException("groupName cannot be more than 50 characters")
  }

  if (option.description.length > 150) {
    throw IllegalArgumentException("groupDescription cannot be more than 150 characters")
  }

  if (option.members.isEmpty()) {
    throw IllegalArgumentException("members cannot be null")
  }

  for (member in option.members) {
    if (!Helpers.isValidAddress(member)) {
      throw IllegalArgumentException("Invalid member address!")
    }
  }
}

fun IsGroupChatId(chatId: String):Boolean {
  return chatId.length == 64
}


class PushGroup {
  data class PushGroupProfile(
    val members: List<Member>,
    val pendingMembers: List<Member>,
    val contractAddressERC20: String?,
    val numberOfERC20: Int,
    val contractAddressNFT: String?,
    val numberOfNFTTokens: Int,
    val verificationProof: String,
    val groupImage: String,
    val groupName: String,
    val groupDescription: String,
    val isPublic: Boolean,
    val groupCreator: String,
    val chatId: String,
    val scheduleAt: String?,
    val scheduleEnd: String?,
    val groupType: String,
    val status: String?,
    val eventType: String?
  )

  data class Member(
    val wallet: String,
    val publicKey: String?,
    val isAdmin: Boolean,
    val image: String?
  )

  data class CreateGroupOptions(
      var name: String,
      var description: String,
      var image: String,
      var members: MutableList<String>,
      var isPublic: Boolean,
      var creatorAddress: String,
      var creatorPgpPrivateKey: String,
      var env: ENV
  ) {
    init {
      // Remove if group creator from the members list
      creatorAddress.let { address ->
        members.indexOfFirst { it == address }.let { index ->
          if (index != -1) {
            members.removeAt(index)
          }
        }
      }

      // Format the addresses
      creatorAddress = Helpers.walletToPCAIP(creatorAddress)
      members = Helpers.walletsToPCAIP(members) as MutableList<String>
    }
  }

  data class CreateGroupPayload(
      var groupName: String,
      var groupDescription: String,
      var members: List<String>,
      var groupImage: String,
      var isPublic: Boolean,
      var admins: List<String> = emptyList(),
      var contractAddressNFT: String? = null,
      var numberOfNFTs: Int? = null,
      var contractAddressERC20: String? = null,
      var numberOfERC20: Int? = null,
      var groupCreator: String,
      var verificationProof: String,
      var meta: String? = null
  )

  companion object{
    public fun createGroup(options:CreateGroupOptions):Result<PushGroupProfile>{
      try {
        createGroupOptionValidator(options)
      }catch (e:Exception){
        return Result.failure(e)
      }

      val hash = getCreateGroupHash(options)
      val pgpSig = Pgp.sign(options.creatorPgpPrivateKey, hash).getOrElse { exception ->  return Result.failure(exception)}
      val sigType = "pgp"
      val verificationProof = "$sigType:$pgpSig"

      val payload = getCreateGroupPayload(options, verificationProof)

      return createGroupService(payload, options.env)

    }

    public fun getGroup(chatId: String, env: ENV):PushGroupProfile?{
      val url = PushURI.getGroup(chatId, env)
      val client = OkHttpClient()

      val request = Request.Builder().url(url).build()
      val response = client.newCall(request).execute()

      if (response.isSuccessful) {
        val jsonResponse = response.body?.string()
        val gson = Gson()
        val apiResponse = gson.fromJson(jsonResponse, PushGroup.PushGroupProfile::class.java)
        return apiResponse
      } else {
        println("Error: ${response.code} ${response.message}")
      }

      response.close()
      return null
    }

    fun getCreateGroupPayload(options: CreateGroupOptions, verificationProof: String):CreateGroupPayload{
      return CreateGroupPayload(
        groupName = options.name,
        groupDescription = options.description,
        members = options.members,
        groupImage = options.image,
        isPublic = options.isPublic,
        admins = listOf(),
        contractAddressNFT = null,
        numberOfNFTs = 0,
        contractAddressERC20 = null,
        numberOfERC20 = 0,
        groupCreator = options.creatorAddress,
        verificationProof = verificationProof,
      )
    }

    fun getCreateGroupHash(options: CreateGroupOptions):String{

      var createGroupJSONString = GetJsonStringFromGenericKV(listOf(
        "groupName" to JsonPrimitive(options.name),
        "groupDescription" to JsonPrimitive(options.description),
        "members" to JsonPrimitive("--members--replace"),
        "groupImage" to JsonPrimitive(options.image),
        "admins" to JsonPrimitive("[]"),
        "isPublic" to JsonPrimitive(options.isPublic),
        "contractAddressNFT" to JsonPrimitive(null),
        "numberOfNFTs" to JsonPrimitive(0),
        "contractAddressERC20" to JsonPrimitive(null),
        "numberOfERC20" to JsonPrimitive(0),
        "groupCreator" to JsonPrimitive(options.creatorAddress)
      ))

      createGroupJSONString = createGroupJSONString.replace("--members--replace", ListToJsonString(options.members))
      createGroupJSONString = createGroupJSONString.replace("\"[","[")
      createGroupJSONString = createGroupJSONString.replace("]\"","]")

      return  GenerateSHA256Hash(createGroupJSONString)
    }

    fun createGroupService(payload:CreateGroupPayload, env: ENV):Result<PushGroupProfile>{
      val url = PushURI.createChatGroup(env)
      val mediaType = "application/json; charset=utf-8".toMediaType()
      val body = Gson().toJson(payload).toRequestBody(mediaType)

      val client = OkHttpClient()
      val request = Request.Builder().url(url).post(body).build()
      val response = client.newCall(request).execute()

      if (response.isSuccessful) {
        val jsonResponse = response.body?.string()
        val gson = Gson()
        val apiResponse = gson.fromJson(jsonResponse, PushGroup.PushGroupProfile::class.java)
        return Result.success(apiResponse)
      } else {
        println(url)
        println(Gson().toJson(payload))
        println("Error: ${response.code} ${response.message}")
        return  Result.failure(IllegalStateException("Error: ${response.code} ${response.message}"))
      }
    }
  }

}