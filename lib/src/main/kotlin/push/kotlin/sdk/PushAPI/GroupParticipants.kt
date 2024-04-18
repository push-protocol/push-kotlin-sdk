package push.kotlin.sdk.PushAPI

import push.kotlin.sdk.ENV
import push.kotlin.sdk.Group.PushGroup
import push.kotlin.sdk.Signer

class GroupParticipants(
        private val account: String,
        val env: ENV,
        private val decryptedPgpPvtKey: String,
        private val signer: Signer,
) {

    data class GroupCountInfo(val participants: Int, val pending: Int)
    data class GetGroupParticipantsOptions(
            val page: Int = 1,
            val limit: Int = 20,
            val filter: FilterOptions? = null
    )

    data class FilterOptions(
            val pending: Boolean? = null,
            val role: Group.GroupRoles? = null  // role: 'admin' | 'member';
    )

    data class ParticipantStatus(
            val pending: Boolean,
            val role: String,
            val participant: Boolean
    )


    fun count(chartId: String): GroupCountInfo {
        val count = PushGroup.getGroupMemberCount(chartId, env);
        return GroupCountInfo(
                pending = count?.totalMembersCount?.pendingCount ?: -1,
                participants = (count?.totalMembersCount?.overallCount ?: -1) - (count?.totalMembersCount?.pendingCount
                        ?: 0)
        )
    }

    fun list(chartId: String, options: GetGroupParticipantsOptions): List<PushGroup.ChatMemberProfile>? {
        return PushGroup.getGroupMembers(
                PushGroup.FetchGroupMemberOptions(
                        chartId,
                        page = options.page,
                        pending = options.filter?.pending,
                        limit = options.limit,
                        role = if (options.filter?.role == Group.GroupRoles.MEMBER) "admin" else "member",
                ),
                env
        )
    }

    fun status(chartId: String, overrideAccount: String?): ParticipantStatus {

        val result = PushGroup.getGroupMemberStatus(chartId, overrideAccount ?: account, env)
        return ParticipantStatus(
                pending = result?.isPending == true,
                role = if (result?.isAdmin == true) "ADMIN" else "MEMBER",
                participant = result?.isMember == true
        )
    }
}