package push.kotlin.sdk

enum class ENV {
    prod, staging, dev
}

object PushURI {
    fun getCID(env: ENV, cid: String): String {
        return "${getBaseUri(env)}/ipfs/$cid"
    }

    fun getUser(env:ENV, userAddress:String):String{
        return "${getBaseUri(env,"v2")}/users/?caip10=${Helpers.walletToPCAIP(userAddress)}"
    }

    fun updateUser(env:ENV,userAddress: String):String{
        return "${getBaseUri(env,"v2")}/users/${Helpers.walletToPCAIP(userAddress)}/profile"
    }

    fun createUser(env: ENV):String{
        return "${getBaseUri(env,"v2")}/users"
    }

    fun getChats(env:ENV, did:String, page:Number=1, limit:Number=10):String{
        return "${getBaseUri(env)}/chat/users/$did/chats?page=$page&limit=$limit"
    }

    fun getChatRequests(env:ENV, did:String, page:Number=1, limit:Number=10):String{
        return "${getBaseUri(env)}/chat/users/$did/requests?page=$page&limit=$limit"
    }

    fun getConversationHaash(env:ENV, account:String, converationId:String):String{
        return "${getBaseUri(env)}/chat/users/$account/conversations/$converationId/hash"
    }

    fun sendChatIntent(env:ENV):String{
        return "${getBaseUri(env)}/chat/request"
    }

    fun sendChatMessage(env:ENV):String{
        return "${getBaseUri(env)}/chat/message"
    }

    fun acceptChatRequest(env: ENV):String{
        return "${getBaseUri(env)}/chat/request/accept"
    }

    fun getConversationHashReslove(env: ENV,threadHash:String, limit: Number):String{
        return  "${getBaseUri(env)}/chat/conversationhash/$threadHash?fetchLimit=${limit}"
    }

    fun createChatGroup(env:ENV):String{
        return "${getBaseUri(env)}/chat/groups"
    }

    fun getGroup(chatId:String, env:ENV):String{
        return "${getBaseUri(env)}/chat/groups/$chatId"
    }

    fun updatedChatGroup(chatId:String, env:ENV):String{
        return "${getBaseUri(env)}/chat/groups/$chatId"
    }

    fun OptInChannel(env: ENV, channel: String): String {
        return "${getBaseUri(env)}/channels/$channel/subscribe"
    }

    fun OptOutChannel(env: ENV, channel: String): String {
        return "${getBaseUri(env)}/channels/$channel/unsubscribe"
    }

    fun getChannels(page: Number, limit: Number,env: ENV): String {
        return "${getBaseUri(env)}/channels?page=${page}&limit=${limit}"
    }

    fun getChannel(env: ENV, channel: String): String {
        return "${getBaseUri(env)}/channels/${channel}"
    }

    fun searchChannels(env: ENV, page: Number, limit: Number, order: String, query: String): String {
        return "${getBaseUri(env)}/channels/search?page=${page}&limit=${limit}&order=${order}&query=${query}"
    }

    fun getSubscribers(channel: String, page: Number, limit: Number, env: ENV): String {
        return "${getBaseUri(env)}/channels/${channel}/subscribers?&page=${page}&limit=${limit}"
    }

    fun isUserSubscribed(env: ENV): String {
        if (env == ENV.prod){
            return "https://backend.epns.io/apis/channels/_is_user_subscribed"
        }
        return "https://backend-staging.epns.io/apis/channels/_is_user_subscribed"
    }

    fun getBaseUri(env: ENV, version:String="v1"):String {

        var baseURL = when (env) {
            ENV.prod -> "https://backend.epns.io"
            ENV.staging -> "https://backend-staging.epns.io"
            ENV.dev -> "https://backend-dev.epns.io"
        }

        baseURL += "/apis/$version"
        return baseURL
    }

}
