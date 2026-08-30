package com.mindvoice.app.data.remote

import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

data class LeaderboardEntryDto(
    val rank: Int,
    val id: String,
    val name: String,
    val xp: Int,
    val proficiencyLevel: String?,
    val isCurrentUser: Boolean
)

data class LanguageDto(
    val id: String,
    val code: String,
    val name: String,
    val flagEmoji: String?
)

data class LessonDto(
    val id: String,
    val title: String,
    val description: String,
    val targetLanguageCode: String,
    val difficulty: String,
    val xpReward: Int,
    val completed: Boolean
)

data class VocabItemDto(
    val id: String,
    val phrase: String,
    val translation: String,
    val notes: String?
)

data class LessonDetailDto(
    val id: String,
    val title: String,
    val description: String,
    val targetLanguageCode: String,
    val difficulty: String,
    val xpReward: Int,
    val completed: Boolean,
    val vocabItems: List<VocabItemDto>
)

data class CompleteLessonResponse(
    val alreadyCompleted: Boolean,
    val xpAwarded: Int,
    val totalXp: Int
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String,
    val nativeLanguageCode: String,
    val targetLanguageCode: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class UserDto(
    val id: String,
    val email: String,
    val name: String,
    val xp: Int = 0,
    val proficiencyLevel: String? = null,
    val nativeLanguage: String? = null,
    val targetLanguage: String? = null
)

data class AuthResponse(
    val user: UserDto,
    val token: String
)

data class MessageDto(
    val id: String,
    val conversationId: String,
    val role: String,
    val content: String,
    val createdAt: String
)

data class ConversationDto(
    val id: String,
    val userId: String,
    val topic: String?,
    val targetLanguageCode: String,
    val createdAt: String,
    val updatedAt: String,
    val messages: List<MessageDto>? = null
)

data class StartConversationRequest(
    val topic: String? = null,
    val targetLanguageCode: String
)

data class SendMessageRequest(
    val content: String
)

data class CorrectionDto(
    val original: String,
    val suggestion: String,
    val explanation: String
)

data class SendMessageResponse(
    val assistantMessage: MessageDto,
    val corrections: List<CorrectionDto>,
    val xpAwarded: Int,
    val totalXp: Int
)

data class SendVoiceMessageResponse(
    val transcript: String,
    val assistantMessage: MessageDto,
    val corrections: List<CorrectionDto>,
    val xpAwarded: Int,
    val totalXp: Int
)

interface ApiService {
    @GET("api/languages")
    suspend fun getLanguages(): List<LanguageDto>

    @GET("api/users/me")
    suspend fun getMe(
        @Header("Authorization") token: String
    ): UserDto

    @GET("api/users/leaderboard")
    suspend fun getLeaderboard(
        @Header("Authorization") token: String
    ): List<LeaderboardEntryDto>

    @GET("api/lessons")
    suspend fun getLessons(
        @Header("Authorization") token: String,
        @retrofit2.http.Query("targetLanguageCode") targetLanguageCode: String
    ): List<LessonDto>

    @GET("api/lessons/{id}")
    suspend fun getLessonDetail(
        @Header("Authorization") token: String,
        @Path("id") lessonId: String
    ): LessonDetailDto

    @POST("api/lessons/{id}/complete")
    suspend fun completeLesson(
        @Header("Authorization") token: String,
        @Path("id") lessonId: String
    ): CompleteLessonResponse

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("api/conversations")
    suspend fun startConversation(
        @Header("Authorization") token: String,
        @Body request: StartConversationRequest
    ): ConversationDto

    @GET("api/conversations/{id}")
    suspend fun getConversation(
        @Header("Authorization") token: String,
        @Path("id") conversationId: String
    ): ConversationDto

    @POST("api/conversations/{id}/messages")
    suspend fun sendMessage(
        @Header("Authorization") token: String,
        @Path("id") conversationId: String,
        @Body request: SendMessageRequest
    ): SendMessageResponse

    @Multipart
    @POST("api/conversations/{id}/voice-messages")
    suspend fun sendVoiceMessage(
        @Header("Authorization") token: String,
        @Path("id") conversationId: String,
        @Part audio: MultipartBody.Part
    ): SendVoiceMessageResponse
}