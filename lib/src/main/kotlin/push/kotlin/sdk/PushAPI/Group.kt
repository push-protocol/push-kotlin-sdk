package push.kotlin.sdk.PushAPI

import push.kotlin.sdk.ChatFunctions.ApproveOptions
import push.kotlin.sdk.ChatFunctions.ChatApprover
import push.kotlin.sdk.ENV
import push.kotlin.sdk.Group.PushGroup
import push.kotlin.sdk.Helpers
import push.kotlin.sdk.Signer
import java.util.*

class Group(
        private val account: String,
        val env: ENV,
        private val decryptedPgpPvtKey: String,
        private val signer: Signer,
) {
   var participants: GroupParticipants

   init {
      participants = GroupParticipants(account, env, decryptedPgpPvtKey, signer)
   }
    enum class GroupRoles {
        MEMBER, ADMIN
    }

    data class GroupCreationOptions(
            val description: String,
            val image: String,
            val members: MutableList<String> = mutableListOf(),
            val admins: MutableList<String> = mutableListOf(),
            val private: Boolean = false,
            val rules: Map<String, String>? = emptyMap()
    )


    data class GroupUpdateOptions(
            val name: String? = null,
            val description: String? = null,
            val image: String? = null,
            val scheduleAt: Date? = null,
            val scheduleEnd: Date? = null,
            val meta: String? = null,
            val rules: Map<String, String?>? = null,
    )


    fun create(name: String, options: GroupCreationOptions): Result<PushGroup.PushGroupProfile> {
        val option = PushGroup.CreateGroupOptionsV2(
                name,
                description = options.description,
                image = options.image,
                members = options.members,
                admins = options.admins,
                isPublic = !options.private,
                creatorAddress = account,
                env = env,
                rules = options.rules,
                config = PushGroup.GroupConfig(),
                creatorPgpPrivateKey = decryptedPgpPvtKey,
        )
        return PushGroup.createGroupV2(option)
    }

    fun permissions(chatId: String): PushGroup.GroupAccess? {
        return PushGroup.getGroupAccess(chatId, account, env)
    }

    fun info(chatId: String): PushGroup.PushGroupInfo? {
        return PushGroup.getGroupInfo(chatId, env);
    }

    fun update(chatId: String, options: GroupUpdateOptions): Result<PushGroup.PushGroupInfo> {
        val group = PushGroup.getGroupInfo(chatId, env)
                ?: return Result.failure(IllegalStateException("Cannot find group with chat id $chatId"));
        val updateOptions = PushGroup.UpdateGroupProfileOptions(
                account,
                chatId,
                groupName = options.name ?: group.groupName,
                groupDescription = options.description ?: group.groupDescription,
                groupImage = options.image ?: group.groupImage,
                rules = options.rules ?: group.rules ?: emptyMap(),
                decryptedPgpPvtKey,
        )

        return PushGroup.updateGroupProfile(updateOptions, env);
    }

    fun add(chatId: String, roles: GroupRoles, accounts: List<String>): Result<PushGroup.PushGroupInfo> {
        if (accounts.isEmpty()) {
            throw IllegalStateException("Cannot find group with chat id $chatId")
        }
        accounts.forEach {
            if (!Helpers.isValidAddress(it)) {
                throw IllegalStateException("Invalid account address: $chatId")
            }
        }

        if (roles == GroupRoles.ADMIN) {
            val options = PushGroup.UpdateGroupMemberOptions(account, chatId,
                    upsert = PushGroup.UpsertData(
                            admins = accounts
                    ),
                    pgpPrivateKey = decryptedPgpPvtKey
            )
            return PushGroup.updateGroupMember(options, env);
        } else {
            val options = PushGroup.UpdateGroupMemberOptions(account, chatId,
                    upsert = PushGroup.UpsertData(
                            members = accounts
                    ),
                    pgpPrivateKey = decryptedPgpPvtKey
            )
            return PushGroup.updateGroupMember(options, env);
        }
    }

    fun remove(chatId: String, accounts: List<String>): Result<PushGroup.PushGroupInfo> {
        if (accounts.isEmpty()) {
            throw IllegalStateException("Cannot find group with chat id $chatId")
        }
        accounts.forEach {
            if (!Helpers.isValidAddress(it)) {
                throw IllegalStateException("Invalid account address: $chatId")
            }
        }

        val options = PushGroup.UpdateGroupMemberOptions(account, chatId,
                remove = accounts,
                pgpPrivateKey = decryptedPgpPvtKey)
        return PushGroup.updateGroupMember(options, env);
    }

    fun modify(chatId: String, roles: GroupRoles, accounts: List<String>): Result<PushGroup.PushGroupInfo> {
        if (accounts.isEmpty()) {
            throw IllegalStateException("Cannot find group with chat id $chatId")
        }
        accounts.forEach {
            if (!Helpers.isValidAddress(it)) {
                throw IllegalStateException("Invalid account address: $chatId")
            }
        }

        if (roles == GroupRoles.ADMIN) {
            val options = PushGroup.UpdateGroupMemberOptions(account, chatId,
                    upsert = PushGroup.UpsertData(
                            admins = accounts
                    ),
                    pgpPrivateKey = decryptedPgpPvtKey
            )
            return PushGroup.updateGroupMember(options, env);
        } else {
            val options = PushGroup.UpdateGroupMemberOptions(account, chatId,
                    upsert = PushGroup.UpsertData(
                            members = accounts
                    ),
                    pgpPrivateKey = decryptedPgpPvtKey
            )
            return PushGroup.updateGroupMember(options, env);
        }
    }

    fun join(target: String): PushGroup.PushGroupInfo? {
        val status = PushGroup.getGroupMemberStatus(target, account, env)
        if (status?.isPending != false) {
            ChatApprover(ApproveOptions(target, account, decryptedPgpPvtKey, env)).approve().getOrThrow()
        } else if (status.isMember) {
            val options = PushGroup.UpdateGroupMemberOptions(account, target,
                    upsert = PushGroup.UpsertData(members = listOf(account)),
                    pgpPrivateKey = decryptedPgpPvtKey
            )
            PushGroup.updateGroupMember(options, env).getOrThrow()
        }
        return info(target)
    }

    fun leave(target: String): PushGroup.PushGroupInfo? {
        val status = PushGroup.getGroupMemberStatus(target, account, env)
        if (status?.isMember == true || status?.isAdmin == true) {
            val options = PushGroup.UpdateGroupMemberOptions(
                    account,
                    target,
                    remove = listOf(account),
                    pgpPrivateKey = decryptedPgpPvtKey
            )
            PushGroup.updateGroupMember(options, env);
        }
        return info(target)
    }

    fun reject(target: String): PushGroup.PushGroupInfo? {
        ChatApprover(ApproveOptions(target, account, decryptedPgpPvtKey, env)).reject().getOrThrow()

        return info(target)
    }


}

