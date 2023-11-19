package hu.bme.aut.echo.http

import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException

class Vectorize {

    data class VectorizationRequest(
        val content: String,
        val tenant: String,
        val chunk_size: Int,
        val sentence_overlap: Int,
    )

    data class VectorResponse(val id: String)

    interface PostVectorizeCallback {
        fun onSuccess(searchId: String)
        fun onFailure(e: IOException)
    }


    fun postVectorizeRequest(user: FirebaseUser, content: String, callback: PostVectorizeCallback) {
        val client = OkHttpClient()

        val requestObj = VectorizationRequest(
            content = content,
            tenant = user.uid,
            chunk_size = 10,
            sentence_overlap = 4
        )

        val gson = Gson()
        val jsonBody = gson.toJson(requestObj)

        val mediaType = "application/json".toMediaTypeOrNull()
        val body = RequestBody.create(mediaType, jsonBody)

        val request = Request.Builder()
            .url("https://api.thoriumlabs.ai/v1/management/documents/")
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
                    println("Unexpected code $response, body: ${response.body?.string()}")
                    return
                }
                val responseData = response.body?.string()
                response.close()
                try {
                    val apiResponse = gson.fromJson(responseData, VectorResponse::class.java)
                    callback.onSuccess(apiResponse.id)
                } catch (e: JsonSyntaxException) {
                    callback.onFailure(IOException(e))
                }
            }
        })
    }
}