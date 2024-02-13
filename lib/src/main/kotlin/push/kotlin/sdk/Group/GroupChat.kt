package push.kotlin.sdk.Group

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import push.kotlin.sdk.*
import push.kotlin.sdk.HahHelper.GenerateSHA256Hash
import push.kotlin.sdk.JsonHelpers.GetJsonStringFromGenericKV
import push.kotlin.sdk.JsonHelpers.ListToJsonString
import push.kotlin.sdk.PushUser.UserProfile
import kotlin.math.*

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

fun updateGroupProfileRequestValidator(
        chatId: String,
        groupName: String,
        groupDescription: String,
        address: String
) {
  if (chatId.isEmpty()) {
    throw Exception("chatId cannot be null or empty")
  }

  if (groupName.isEmpty()) {
    throw Exception("groupName cannot be null or empty")
  }

  if (groupName.length > 50) {
    throw Exception("groupName cannot be more than 50 characters")
  }

  if (groupDescription.length > 150) {
    throw Exception("groupDescription cannot be more than 150 characters")
  }

  if (!Helpers.isValidAddress(address)) {
    throw Exception("Invalid address field!")
  }
}

fun validateGroupMemberUpdateOptions(
        chatId: String,
        upsert: PushGroup.UpsertData,
        remove: List<String>
) {
  if (chatId.isEmpty()) {
    throw Exception("chatId cannot be null or empty")
  }

  // Validating upsert object
  val allowedRoles = listOf("members", "admins")

  upsert.toJson().forEach { (role, value) ->
    if (!allowedRoles.contains(role)) {
      throw Exception("Invalid role: $role. Allowed roles are ${allowedRoles.joinToString(", ")}.")
    }

    if (value is List<*> && value.size > 1000) {
      throw Exception("$role array cannot have more than 1000 addresses.")
    }

    (value as? List<String>)?.forEach { address ->
      if (!Helpers.isValidAddress(address)) {
        throw Exception("Invalid address found in $role list.")
      }
    }
  }

  // Validating remove array
  if (remove.size > 1000) {
    throw Exception("Remove array cannot have more than 1000 addresses.")
  }

  remove.forEach { address ->
    if (!Helpers.isValidAddress(address)) {
      throw Exception("Invalid address found in remove list.")
    }
  }
}




class PushGroup {

  data class PushGroupInfo(
          var groupName: String,
          var groupImage: String,
          var groupDescription: String,
          val isPublic: Boolean,
          var groupCreator: String,
          val chatId: String,
          val scheduleAt: String?,
          val scheduleEnd: String?,
          val groupType: String,
          val status: String?,
          val rules: Map<String, String?>?,
          val meta: String?,
          val sessionKey: String?,
          val encryptedSecret: String?,
  )
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

  data class UpdateGroupPayload(
    var groupName: String,
    var groupDescription: String,
    var groupImage: String,
    var members: List<String>,
    var admins: List<String> = emptyList(),
    var address: String,
    var verificationProof: String,
  )


  data class RoleCounts(
          val total: Int,
          val pending: Int
  )

  data class ChatMemberCounts(
          @SerializedName("totalMembersCount") val totalMembersCount: TotalMembersCount
  )


  data class MemberRoles(
          @SerializedName("ADMIN") val admin: RoleCounts,
          @SerializedName("MEMBER") val member: RoleCounts
  )

  data class TotalMembersCount(
          @SerializedName("overallCount") val overallCount: Int,
          @SerializedName("adminsCount") val adminsCount: Int,
          @SerializedName("membersCount") val membersCount: Int,
          @SerializedName("pendingCount") val pendingCount: Int,
          @SerializedName("approvedCount") val approvedCount: Int,
          @SerializedName("roles") val roles: MemberRoles
  )

  data class GroupMemberStatus(
          val isMember: Boolean,
          val isPending: Boolean,
          val isAdmin: Boolean
  )

  data class GroupMemberPublicKey(
          val did: String,
          val publicKey: String
  ) {
    companion object {
      fun fromJson(json: Map<String, Any>): GroupMemberPublicKey {
        return GroupMemberPublicKey(
                did = json["did"] as String,
                publicKey = json["publicKey"] as String
        )
      }
    }

    fun toJson(): Map<String, Any> {
      return mapOf(
              "did" to did,
              "publicKey" to publicKey
      )
    }
  }

  data class FetchGroupMemberOptions(
          val chatId: String,
          val page: Int = 1,
          val limit: Int = 20,
          val pending: Boolean? = null,
          val role: String? = null
  )
  data class FetchGroupMemberPublicKeysOptions(
          val chatId: String,
          val page: Int = 1,
          val limit: Int = 20,
  )

  data class ChatMemberProfile(
          val address: String,
          val intent: Boolean,
          val role: String,
          val userInfo: UserProfile? = null
  ) {

    companion object {
      fun fromJson(json: Map<String, Any>): ChatMemberProfile {
        return ChatMemberProfile(
                address = json["address"] as String,
                intent = json["intent"] as Boolean,
                role = json["role"] as String,
                userInfo = json["userInfo"]?.let { PushUser.UserProfile.fromJson(it as Map<String, Any>) }
        )
      }
    }

    fun toJson(): Map<String, Any?> {
      return mapOf(
              "address" to address,
              "intent" to intent,
              "role" to role
      )
    }
  }

  data class  UpdateGroupProfileOptions(
          var account: String,
          val chatId: String,
          val groupName: String,
          var groupDescription: String,
          var groupImage: String,
          var rules: Map<String, String?> = emptyMap(),
          var pgpPrivateKey: String
  )

  data class UpdateGroupMemberOptions(
          var account: String,
          val chatId: String,
          var upsert: UpsertData = UpsertData(),
          val remove: List<String> = listOf(),
          var pgpPrivateKey: String
  )

  class UpsertData(
          val members: List<String> = emptyList(),
          val admins: List<String> = emptyList()
  ) {
    constructor(json: Map<String, Any>) : this(
            members = (json["members"] as? List<String>) ?: emptyList(),
            admins = (json["admins"] as? List<String>) ?: emptyList()
    )

    fun toJson(): Map<String, List<String>> {
      return mapOf(
              "members" to members,
              "admins" to admins
      )
    }
  }



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

     fun getGroupInfo(chatId: String, env: ENV):PushGroupInfo?{
      val url = PushURI.getGroupInfo(chatId, env)
      val client = OkHttpClient()

      val request = Request.Builder().url(url).build()
      val response = client.newCall(request).execute()

      if (response.isSuccessful) {
        val jsonResponse = response.body?.string()
        val gson = Gson()
        val apiResponse = gson.fromJson(jsonResponse, PushGroup.PushGroupInfo::class.java)
        return apiResponse
      } else {
        println("Error: ${response.code} ${response.message}")
      }

      response.close()
      return null
    }

    fun getGroupMemberCount(chatId: String, env: ENV): ChatMemberCounts? {
      val url = PushURI.getGroupMemberCount(chatId, env)
      val client = OkHttpClient()

      val request = Request.Builder().url(url).build()
      val response = client.newCall(request).execute()
      if (response.isSuccessful) {
        val jsonResponse = response.body?.string()
        val gson = Gson()
        val apiResponse = gson.fromJson(jsonResponse, PushGroup.ChatMemberCounts::class.java)
        return apiResponse
      } else {
        println("Error: ${response.code} ${response.message}")
      }

      response.close()
      return null
    }

    fun getGroupMembersPublicKeys(options: FetchGroupMemberPublicKeysOptions, env: ENV): List<GroupMemberPublicKey>? {
      val url = PushURI.getGroupMembersPublicKeys(options.chatId,options. page,options. limit, env)
      val client = OkHttpClient()

      val request = Request.Builder().url(url).build()
      val response = client.newCall(request).execute()
      if (response.isSuccessful) {
        val jsonResponse = response.body?.string()
        val gson = Gson()
        val members = gson.fromJson(jsonResponse, Map::class.java)["members"] as? List<Map<String, String>>
                ?: throw Exception("Failed to retrieve members")
        return members.map { GroupMemberPublicKey.fromJson(it) }
      } else {
        println("Error: ${response.code} ${response.message}")
      }

      response.close()
      return null
    }

    fun  getGroupMembers(options: FetchGroupMemberOptions, env: ENV): List<ChatMemberProfile>? {
      if (options.chatId.isEmpty()) {
        throw Exception("chatId cannot be null or empty")
      }

      val url = PushURI.getGroupMembers(options, env)
      val client = OkHttpClient()

      val request = Request.Builder().url(url).build()
      val response = client.newCall(request).execute()
      if (response.isSuccessful) {
        val jsonResponse = response.body?.string()
        println("json $jsonResponse")
        val gson = Gson()
        val members = gson.fromJson(jsonResponse, Map::class.java)["members"] as? List<Map<String, String>>
                ?: throw Exception("Failed to retrieve members")
        return members.map { ChatMemberProfile.fromJson(it) }
      } else {
        println("Error: ${response.code} ${response.message}")
      }

      response.close()
      return null
    }

    fun getAllGroupMembers(chatId: String,env: ENV ): List<ChatMemberProfile> {
      if (chatId.isEmpty()) {
        throw Exception("chatId cannot be null or empty")
      }

      val count = getGroupMemberCount(chatId, env)
      val limit = 5000

      val totalPages = ceil(count!!.totalMembersCount. overallCount.toDouble() / limit).toInt()

      val pagesResult = (1..totalPages).map { index ->
        getGroupMembers(FetchGroupMemberOptions(chatId = chatId, page = index, limit = limit),env)
      }

      val members = mutableListOf<ChatMemberProfile>()
      pagesResult.forEach { members.addAll(it!!) }

      return members
    }

    fun getAllGroupMembersPublicKeys(chatId: String,env: ENV ): List<GroupMemberPublicKey> {
      if (chatId.isEmpty()) {
        throw Exception("chatId cannot be null or empty")
      }

      val count = getGroupMemberCount(chatId, env)
      val limit = 5000

      val totalPages = ceil(count!!.totalMembersCount. overallCount.toDouble() / limit).toInt()

      val pagesResult = (1..totalPages).map { index ->
        getGroupMembersPublicKeys(FetchGroupMemberPublicKeysOptions(chatId = chatId, page = index, limit = limit),env)
      }

      val members = mutableListOf<GroupMemberPublicKey>()
      pagesResult.forEach { members.addAll(it!!) }

      return members
    }

    fun getGroupMemberStatus(chatId: String,did: String, env: ENV): GroupMemberStatus? {
      if (chatId.isEmpty()){
        throw  IllegalArgumentException("chatId cannot be null or empty")
      }

      if (did.isEmpty()) {
        throw IllegalArgumentException("did cannot be null or empty");
      }

      val url = PushURI.getGroupMemberStatus(chatId,did, env)
      val client = OkHttpClient()

      val request = Request.Builder().url(url).build()
      val response = client.newCall(request).execute()
      if (response.isSuccessful) {
        val jsonResponse = response.body?.string()
        val gson = Gson()
        val apiResponse = gson.fromJson(jsonResponse, PushGroup.GroupMemberStatus::class.java)
        return apiResponse
      } else {
        println("Error: ${response.code} ${response.message}")
      }

      response.close()
      return null
    }
    private fun getCreateGroupPayload(options: CreateGroupOptions, verificationProof: String): CreateGroupPayload {
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

    private fun getUpdateGroupHash(updatedGroup: PushGroupProfile):String{
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
    private fun createGroupService(payload:CreateGroupPayload, env: ENV):Result<PushGroupProfile>{
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

    private fun updateGroupService(chatId: String, payload:UpdateGroupPayload, env: ENV):Result<PushGroupProfile>{
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

    fun updateGroupProfile(options: UpdateGroupProfileOptions, env: ENV): Result<PushGroupInfo> {

      // Validations
      if (options.account.isEmpty()) {
        throw Exception("account cannot be empty")
      }

      updateGroupProfileRequestValidator(
                 chatId = options.chatId,
              groupName = options.groupName,
              groupDescription = options.groupDescription ?: "",
              address = options.account,
      )

      val group = getGroupInfo(chatId = options.chatId, env)
      val  updateJsonString = mapOf(
              "groupName" to options.groupName,
              "groupDescription" to (options.groupDescription ?: group?.groupDescription),
              "groupImage" to options.groupImage,
              "rules" to options.rules,
              "isPublic" to group?.isPublic,
              "groupType" to group?.groupType
      )
      val hash = GenerateSHA256Hash(updateJsonString)
      val signature =Pgp.sign(message = hash, pgpPrivateKey = options.pgpPrivateKey).getOrElse { exception -> return Result.failure(exception) }
      val sigType = "pgpv2"
      val profileVerificationProof = "$sigType:$signature:${Helpers.walletToPCAIP( options.account)}"

      val payload = mapOf(
              "groupName" to options.groupName,
              "groupDescription" to (options.groupDescription ?: group?.groupDescription),
              "groupImage" to options.groupImage,
              "rules" to (options.rules ?: emptyMap<String, Any>()),
              "profileVerificationProof" to profileVerificationProof.trimIndent()
      )

      val url = PushURI.updatedChatGroupProfile(options.chatId, env)
      val mediaType = "application/json; charset=utf-8".toMediaType()
      val body =Gson().toJson(payload)
              .toRequestBody(mediaType)


      val client = OkHttpClient()
      val request = Request.Builder().url(url).put(body).build()
      val response = client.newCall(request).execute()


      if (response.isSuccessful) {
        val jsonResponse = response.body?.string()
        val gson = Gson()
        val apiResponse = gson.fromJson(jsonResponse, PushGroupInfo::class.java)
        return Result.success(apiResponse)
      } else {
        println(url)
        println(Gson().toJson(payload))
        println("Error: ${response.code} ${response.message}")
        return  Result.failure(IllegalStateException("Error: ${response.code} ${response.message}"))
      }
    }


    fun updateGroupMember(options: UpdateGroupMemberOptions, env: ENV): Result<PushGroupInfo> {
      validateGroupMemberUpdateOptions(chatId = options.chatId, upsert = options.upsert, remove = options.remove)

      val convertedUpsert = mutableMapOf<String, List<String>>()
      for ((key, value) in options.upsert.toJson()) {
        convertedUpsert[key] = value.map { Helpers.walletToPCAIP(it) };
      }

      val convertedRemove = options.remove.map { Helpers.walletToPCAIP(it) }

      val connectedUser = PushUser.getUser(userAddress = options.account, env)
              ?: throw Exception("${options.account} not found")

      val group = getGroupInfo(chatId = options.chatId, env) ?: throw Exception("Group not found")

      var encryptedSecret: String? = null;
      if (!group.isPublic) {
        if (group.encryptedSecret != null) {
          val isMember = getGroupMemberStatus(chatId = options.chatId, did = connectedUser.did, env = env)!!.isMember

          val removeParticipantSet = convertedRemove.map { it.lowercase() }.toSet()

          var groupMembers = getAllGroupMembersPublicKeys(chatId = options.chatId, env = env)

          var sameMembers = true;

          for (member in groupMembers) {
            if (removeParticipantSet.contains(member.did.lowercase())) {
              sameMembers = false
              break
            }
          }

          if (!sameMembers || !isMember) {
            val secretKey = AESGCM.generateRandomSecret(15)
            val publicKeys = mutableListOf<String>()

            // This will now only take keys of non-removed members
            for (member in groupMembers) {
              if (!removeParticipantSet.contains(member.did.lowercase())) {
                publicKeys.add(member.publicKey)
              }
            }

            // This is autoJoin Case
            if (!isMember) {
              publicKeys.add(connectedUser.publicKey)
            }

            encryptedSecret = Pgp.encrypt(message = secretKey, userPublicKeys = publicKeys).getOrElse { exception -> return Result.failure(exception) }
          }
        }
      }

      val bodyToBeHashed = mapOf(
              "upsert" to convertedUpsert,
              "remove" to convertedRemove,
              "encryptedSecret" to encryptedSecret,
      )


      val hash = GenerateSHA256Hash(bodyToBeHashed);
      val signature = Pgp.sign(options.pgpPrivateKey, hash).getOrElse { exception -> return Result.failure(exception) }
      val sigType = "pgpv2";
      val deltaVerificationProof ="$sigType:$signature:${Helpers.walletToPCAIP(connectedUser.did)}";

      val payload = mapOf(
              "upsert" to convertedUpsert,
              "remove" to convertedRemove,
              "encryptedSecret" to encryptedSecret,
              "deltaVerificationProof" to deltaVerificationProof
      )

      val url = PushURI.updatedChatGroupMember(options.chatId, env)
      val mediaType = "application/json; charset=utf-8".toMediaType()
      val body = GsonBuilder().serializeNulls().create().toJson(payload)
              .toRequestBody(mediaType)


      val client = OkHttpClient()
      val request = Request.Builder().url(url).put(body).build()
      val response = client.newCall(request).execute()


      if (response.isSuccessful) {
        val jsonResponse = response.body?.string()
        val gson = Gson()
        val apiResponse = gson.fromJson(jsonResponse, PushGroupInfo::class.java)
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