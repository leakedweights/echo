package hu.bme.aut.echo.http

import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import hu.bme.aut.echo.models.Message
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException

class Chat {

    data class MessageRequest(
        val language_model: String,
        val instructions: String,
        val context: String,
        val message: String,
        val semantic_search: SemanticSearch,
        val history: List<HistoryItem>
    )

    data class SemanticSearch(val limit: Int, val tenant: String)
    data class HistoryItem(val isUser: Boolean, val text: String)

    data class MessageResponse(val response: String)

    interface PostMessageCallback {
        fun onSuccess(responseMessage: String)
        fun onFailure(e: IOException)
    }


    fun postMessage(user: FirebaseUser, message: Message, messages: List<Message>, callback: PostMessageCallback) {
        val client = OkHttpClient()

        val requestObj = MessageRequest(
            language_model = "gpt-3.5-turbo",
            instructions = getInstructions(),
            context = "",
            message = message.content,
            semantic_search = SemanticSearch(limit = 3, tenant = user.uid),
            history = getHistoryMapping(messages)
        )

        val gson = Gson()
        val jsonBody = gson.toJson(requestObj)

        val mediaType = "application/json".toMediaTypeOrNull()
        val body = RequestBody.create(mediaType, jsonBody)

        val request = Request.Builder()
            .url("https://api.thoriumlabs.ai/v1/inference/messages/")
            .post(body)
            .addHeader("accept", "application/json")
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onFailure(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    callback.onFailure(IOException("Cannot retrieve response. Code: ${response.code}"))
                    return
                }
                val responseData = response.body?.string()
                response.close()
                try {
                    val apiResponse = gson.fromJson(responseData, MessageResponse::class.java)
                    callback.onSuccess(apiResponse.response)
                } catch (e: JsonSyntaxException) {
                    callback.onFailure(IOException(e))
                }
            }
        })
    }

    private fun getHistoryMapping(messageHistory: List<Message>) : List<HistoryItem> {
        return messageHistory.map { HistoryItem(it.sender == Message.Sender.User, it.content) }
    }

    private fun getInstructions(): String {
        return """
            You are Echo, a helpful personal assistant helping people in their daily lives.
            Your job is to provide helpful information and advice based on the provided context.
            The users record voice messages that are transcribed to text. The context includes the most relevant messages.
            Answer with helpful but brief messages. Keep it to 2-3 sentences at most.
            """.trimIndent()
    }

}