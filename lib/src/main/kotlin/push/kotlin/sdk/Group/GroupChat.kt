package push.kotlin.sdk.Group

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONObject
import push.kotlin.sdk.*
import push.kotlin.sdk.HahHelper.GenerateSHA256Hash
import push.kotlin.sdk.JsonHelpers.GetJsonStringFromGenericKV
import push.kotlin.sdk.JsonHelpers.ListToJsonString
import java.util.*

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

@Throws(IllegalArgumentException::class)
fun createGroupOptionV2Validator(option: PushGroup.CreateGroupOptionsV2) {
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

  for (member in option.members) {
    if (!Helpers.isValidAddress(member)) {
      throw IllegalArgumentException("Invalid member address!")
    }
  }

  for (admin in option.admins) {
    if (!Helpers.isValidAddress(admin)) {
      throw IllegalArgumentException("Invalid admin address!")
    }
  }

  validateScheduleDates(scheduleAt = option.config.scheduleAt, scheduleEnd = option.config.scheduleEnd)
}

@Throws(IllegalArgumentException::class)
fun validateScheduleDates(scheduleAt: Date?, scheduleEnd: Date?) {
  if (scheduleAt != null) {
    val start = Date(scheduleAt.time)
    val now = Date()

    if (start < now) {
      throw IllegalArgumentException("Schedule start time must be in the future.")
    }

    if (scheduleEnd != null) {
      val end = Date(scheduleEnd.time)

      if (end < now) {
        throw IllegalArgumentException("Schedule end time must be in the future.")
      }

      if (start >= end) {
        throw IllegalArgumentException("Schedule start time must be earlier than end time.")
      }
    }
  }
}

fun IsGroupChatId(chatId: String):Boolean {
  return chatId.length == 64
}

fun validateUpdateGroupOptions(group: PushGroup.PushGroupProfile):Result<Any>{
  if (group.groupName.isEmpty()) {
    throw IllegalArgumentException("groupName cannot be null or empty")
  }

  if (group.groupName.length > 50) {
    throw IllegalArgumentException("groupName cannot be more than 50 characters")
  }

  if (group.groupDescription.length > 150) {
    throw IllegalArgumentException("groupDescription cannot be more than 150 characters")
  }

  if (group.members.isEmpty()) {
    throw IllegalArgumentException("members cannot be null")
  }

  for (member in group.members) {
    if (!Helpers.isValidAddress(member.wallet)) {
      throw IllegalArgumentException("Invalid member address!")
    }
  }

  return Result.success(true)
}


class PushGroup {
  data class PushGroupProfile(
          var members: List<Member>,
          var pendingMembers: List<Member>,
          val contractAddressERC20: String?,
          val numberOfERC20: Int,
          val contractAddressNFT: String?,
          val numberOfNFTTokens: Int,
          val verificationProof: String,
          var groupImage: String,
          var groupName: String,
          var groupDescription: String,
          val isPublic: Boolean,
          var groupCreator: String,
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

  data class CreateGroupOptionsV2(
          var name: String,
          var description: String,
          var image: String,
          var members: MutableList<String>,
          var admins: MutableList<String>,
          var isPublic: Boolean,
          var creatorAddress: String,
          var groupType: String = "default",
          var creatorPgpPrivateKey: String,
          var env: ENV,
          var config: GroupConfig,
          var rules: Map<String, String?>
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

      // Remove if group creator from the members list
      creatorAddress.let { address ->
        admins.indexOfFirst { it == address }.let { index ->
          if (index != -1) {
            admins.removeAt(index)
          }
        }
      }

      // Format the addresses
      creatorAddress = Helpers.walletToPCAIP(creatorAddress)
      members = Helpers.walletsToPCAIP(members) as MutableList<String>
      admins = Helpers.walletsToPCAIP(admins) as MutableList<String>
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

  data class CreateGroupPayloadV2(
          @SerializedName("groupName") val groupName: String,
          @SerializedName("groupDescription") val groupDescription: String,
          @SerializedName("groupImage") val groupImage: String,
          @SerializedName("rules") val rules: Map<String, String?>,
          @SerializedName("isPublic") val isPublic: Boolean,
          @SerializedName("groupType") val groupType: String,
          @SerializedName("profileVerificationProof") val profileVerificationProof: String,
          @SerializedName("config") val config: Config,
          @SerializedName("members") val members: List<String>,
          @SerializedName("admins") val admins: List<String>,
          @SerializedName("idempotentVerificationProof") val idempotentVerificationProof: String
  )

  data class Config(
          @SerializedName("meta") val meta: String?,
          @SerializedName("scheduleAt") val scheduleAt: String?,
          @SerializedName("scheduleEnd") val scheduleEnd: String?,
          @SerializedName("status") val status: String?,
          @SerializedName("configVerificationProof") val configVerificationProof: String
  )


  data class GroupConfig(
          var meta: String?,
          var scheduleAt: Date?,
          var scheduleEnd: Date?,
          var status: String? = "PENDING",
  ) {
    constructor() : this(
            meta = null,
            scheduleAt = null,
            scheduleEnd = null
    )
  }


  data class UpdateGroupPayload(
    var groupName: String,
    var groupDescription: String,
    var groupImage: String,
    var members: List<String>,
    var admins: List<String> = emptyList(),
    var address: String,
    var verificationProof: String,
  )

  companion object{
    public fun createGroupV2(options: CreateGroupOptionsV2): Result<PushGroupProfile> {
      try {
        createGroupOptionV2Validator(options)
      } catch (e: Exception) {
        return Result.failure(e)
      }

      val payload = getCreateGroupPayloadV2(options)

      return createGroupServiceV2(payload, options.env)

    }

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

    public fun updateGroup(updatedGroup:PushGroupProfile, userAddress:String, userPgpPrivateKey:String, env: ENV):Result<PushGroupProfile>{
      validateUpdateGroupOptions(updatedGroup).getOrElse { exception -> return Result.failure(exception) }

      val hash = getUpdateGroupHash(updatedGroup)
      val pgpSig = Pgp.sign(userPgpPrivateKey, hash).getOrElse { exception ->  return Result.failure(exception)}
      val sigType = "pgp"
      val verificationProof = "$sigType:$pgpSig"

      val payload = getUpdateGroupPayload(updatedGroup, verificationProof, userAddress)

      return updateGroupService(updatedGroup.chatId, payload, env)
    }

    public fun leaveGroup(chatId: String, userAddress:String, userPgpPrivateKey:String, env:ENV):Result<PushGroupProfile>{
      val group = PushGroup.getGroup(chatId,env) ?: return Result.failure(IllegalStateException("Group not found"))

      group.members = group.members.filter { el -> !el.wallet.lowercase().contains(userAddress.lowercase()) }

      return updateGroup(group, userAddress, userPgpPrivateKey,env)
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

    @Throws(IllegalArgumentException::class)
    private fun getCreateGroupPayloadV2(options: CreateGroupOptionsV2): CreateGroupPayloadV2 {

      /**
       * PROFILE VERIFICATION PROOF
       */
      val profileHash = getCreateGroupProfileHash(options)
      val profileSignature = Pgp.sign(options.creatorPgpPrivateKey, profileHash).getOrElse { exception -> throw IllegalArgumentException(exception) }
      val profileVerificationProof = "pgpv2:$profileSignature:${options.creatorAddress}";

      /**
       * CONFIG VERIFICATION PROOF
       */
      val configHash = getCreateGroupConfigHash(options)
      val configSignature = Pgp.sign(options.creatorPgpPrivateKey, configHash).getOrElse { exception -> throw IllegalArgumentException(exception) }
      val configVerificationProof = "pgpv2:$configSignature:${options.creatorAddress}";

      /**
       * IDEMPOTENT VERIFICATION PROOF
       */
      val idempotentHash = getCreateGroupIdempotentHash(options)
      val idempotentSignature = Pgp.sign(options.creatorPgpPrivateKey, idempotentHash).getOrElse { exception -> throw IllegalArgumentException(exception) }
      val idempotentVerificationProof = "pgpv2:$idempotentSignature:${options.creatorAddress}";


      val configMap = mapOf(
              Pair("meta", options.config.meta),
              Pair("scheduleAt", options.config.scheduleAt?.toString()),
              Pair("scheduleEnd", options.config.scheduleEnd?.toString()),
              Pair("status", options.config.status),
              Pair("configVerificationProof", configVerificationProof)
      )

      return CreateGroupPayloadV2(
              groupName = options.name,
              groupDescription = options.description,
              members = options.members,
              groupImage = options.image,
              isPublic = options.isPublic,
              admins = options.admins,
              profileVerificationProof = profileVerificationProof,
              rules = options.rules,
              idempotentVerificationProof = idempotentVerificationProof,
              config = Config(
                      meta = options.config.meta,
                      scheduleAt = options.config.scheduleAt?.toString(),
                      scheduleEnd = options.config.scheduleEnd?.toString(),
                      status = options.config.status,
                      configVerificationProof = configVerificationProof
              ),
              groupType = options.groupType

      )
    }

    fun getUpdateGroupPayload(updatedGroup:PushGroupProfile, verificationProof: String, userAddress: String):UpdateGroupPayload{
      val pendingMembers = updatedGroup.pendingMembers.map { el -> el.wallet }
      val membersList = updatedGroup.members.map { el -> el.wallet }
      val members = pendingMembers + membersList

      return UpdateGroupPayload(
        groupName = updatedGroup.groupName,
        groupDescription = updatedGroup.groupDescription,
        groupImage = updatedGroup.groupImage,
        members = members,
        admins = listOf(updatedGroup.groupCreator),
        address = Helpers.walletToPCAIP(userAddress),
        verificationProof = verificationProof
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

    private fun getCreateGroupIdempotentHash(options: CreateGroupOptionsV2): String {

      var createGroupJSONString = GetJsonStringFromGenericKV(listOf(
              "members" to JsonPrimitive("--members--replace"),
              "admins" to JsonPrimitive("--admins--replace"),
      ))

      createGroupJSONString = createGroupJSONString.replace("--members--replace", ListToJsonString(options.members))
      createGroupJSONString = createGroupJSONString.replace("--admins--replace", ListToJsonString(options.admins))
      createGroupJSONString = createGroupJSONString.replace("\"[", "[")
      createGroupJSONString = createGroupJSONString.replace("]\"", "]")

      return GenerateSHA256Hash(createGroupJSONString)
    }

    private fun getCreateGroupConfigHash(options: CreateGroupOptionsV2): String {

      val createGroupJSONString = GetJsonStringFromGenericKV(listOf(
              "meta" to JsonPrimitive(options.config.meta),
              "scheduleAt" to JsonPrimitive(options.config.scheduleAt.toString()),
              "scheduleEnd" to JsonPrimitive(options.config.scheduleEnd.toString()),
              "status" to JsonPrimitive(options.config.status),
      ))

      return GenerateSHA256Hash(createGroupJSONString)
    }

    fun getCreateGroupProfileHash(options: CreateGroupOptionsV2): String {

      var createGroupJSONString = GetJsonStringFromGenericKV(listOf(
              "groupName" to JsonPrimitive(options.name),
              "groupDescription" to JsonPrimitive(options.description),
              "groupImage" to JsonPrimitive(options.image),
              "isPublic" to JsonPrimitive(options.isPublic),
              "groupType" to JsonPrimitive(options.groupType),
              "rules" to JsonPrimitive("--rules--replace"),
      ))

      createGroupJSONString = createGroupJSONString.replace("--rules--replace", options.rules.toString())

      return GenerateSHA256Hash(createGroupJSONString)
    }

    private fun getUpdateGroupHash(updatedGroup: PushGroupProfile): String {
      var createGroupJSONString = GetJsonStringFromGenericKV(listOf(
        "groupName" to JsonPrimitive(updatedGroup.groupName),
        "groupDescription" to JsonPrimitive(updatedGroup.groupDescription),
        "groupImage" to JsonPrimitive(updatedGroup.groupImage),
        "members" to JsonPrimitive("--members--replace"),
        "admins" to JsonPrimitive("--admins--replace"),
        "chatId" to JsonPrimitive(updatedGroup.chatId),
      ))

      val pendingMembers = updatedGroup.pendingMembers.map { el -> el.wallet }
      val membersList = updatedGroup.members.map { el -> el.wallet }
      val members = pendingMembers + membersList

      createGroupJSONString = createGroupJSONString.replace("--members--replace", ListToJsonString(members))
      createGroupJSONString = createGroupJSONString.replace("--admins--replace", ListToJsonString(listOf(updatedGroup.groupCreator)))
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
        return Result.failure(IllegalStateException("Error: ${response.code} ${response.message}"))
      }
    }

    private fun createGroupServiceV2(payload: CreateGroupPayloadV2, env: ENV): Result<PushGroupProfile> {
      val url = PushURI.createChatGroupV2(env)
      val mediaType = "application/json; charset=utf-8".toMediaType()
      val gson = GsonBuilder().serializeNulls().create()
      val json = gson.toJson(payload)//.toRequestBody(mediaType)
      val body = json.toRequestBody(mediaType)


      val client = OkHttpClient()
      val request =   Request.Builder().url(url).post(body).build()
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

    fun updateGroupService(chatId: String, payload:UpdateGroupPayload, env: ENV):Result<PushGroupProfile>{
      val url = PushURI.updatedChatGroup(chatId, env)
      val mediaType = "application/json; charset=utf-8".toMediaType()
      val body = Gson().toJson(payload).toRequestBody(mediaType)

      val client = OkHttpClient()
      val request = Request.Builder().url(url).put(body).build()
      val response = client.newCall(request).execute()


      if (response.isSuccessful) {
        val jsonResponse = response.body?.string()
        val gson = Gson()
        val apiResponse = gson.fromJson(jsonResponse, PushGroup.PushGroupProfile::class.java)
        return Result.success(apiResponse)
      } else {
        println(Gson().toJson(payload))
        println("Error: ${response.code} ${response.message}")
        return  Result.failure(IllegalStateException("Error: ${response.code} ${response.message}"))
      }
    }
  }

}