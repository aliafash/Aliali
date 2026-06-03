package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object YemenGeminiService {
    private const val TAG = "YemenGeminiService"
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    suspend fun chatWithAssistant(userMessage: String, appName: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API key is not configured or is the default placeholder!")
            return@withContext "مرحباً بك! للاستفادة الكاملة من المساعد الذكي لخدمات اليمن، يرجى تهيئة مفتاح API الخاص بـ Gemini في نافذة الأسرار. \n\nأنا هنا لخدمتك ومعاونتك في اختيار أفضل كهربائي، سباك، طبيب، أو مدرب في اليمن!"
        }

        val url = "$BASE_URL?key=$apiKey"
        val mediaType = "application/json".toMediaType()

        // System instructions to give character and local Yemen style to the chatbot!
        val systemInstruction = "أنت المساعد الذكي لتطبيق '$appName' (خدمات اليمن). تتحدث بلهجة يمنية دافئة، ترحيبية، ومهذبة للغاية (على سبيل المثال: أهلاً يا غالي، عيني لك، تحت أمرك يا طيب). وظيفتك هي مساعدة المستخدمين في العثور على أفضل مقدمي الخدمات كأصحاب المهن الحرة أو الأطباء في اليمن، وتقديم نصائح عامة حول صيانة المنزل، الرعاية، والتعليم بطريقة ذكية وعملية."

        try {
            val systemPart = JSONObject().apply {
                put("text", systemInstruction)
            }
            val systemContent = JSONObject().apply {
                put("parts", JSONArray().put(systemPart))
            }

            val part = JSONObject().apply {
                put("text", userMessage)
            }
            val content = JSONObject().apply {
                put("parts", JSONArray().put(part))
            }

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().put(content))
                put("systemInstruction", systemContent)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                })
            }

            val requestBody = requestJson.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val code = response.code
                    val errorBody = response.body?.string()
                    Log.e(TAG, "Gemini API failed with code $code: $errorBody")
                    return@withContext "عذراً يا طيب، واجهت مشكلة في الاتصال بالذكاء الاصطناعي حالياً. كرر محاولتك لاحقاً."
                }

                val bodyStr = response.body?.string() ?: return@withContext "لم أحصل على رد يا غالي."
                val responseJson = JSONObject(bodyStr)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val contentObj = firstCandidate.getJSONObject("content")
                    val parts = contentObj.getJSONArray("parts")
                    if (parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).getString("text")
                    }
                }
                "عذراً، لم أستطع معالجة الإجابة بشكل صحيح."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in chatWithAssistant", e)
            "أهلاً بك يا طيب! يبدو أن الإنترنت ضعيف أو هناك عطل مؤقت. أنا المساعد الذكي لخدمات اليمن وسأبقى بجانبك دائماً لتلبية احتياجاتك وتوجيهك لأصحاب المهن المعتمدين والموثوقين!"
        }
    }
}
