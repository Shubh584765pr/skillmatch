package `in`.binkgo.app.data

import com.google.gson.Gson
import `in`.binkgo.app.BuildConfig
import `in`.binkgo.app.R
import `in`.binkgo.app.SkillMatchApp
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.IOException

// ---- Models (field names match the JSON from the Next.js API) ----

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val location: String?,
    val experienceLevel: String,
    val language: String,
    val jobAlerts: Boolean,
    val skills: List<UserSkill>,
)

/** level: 1 Beginner, 2 Proficient, 3 Advanced, 4 Expert */
data class UserSkill(val id: Int, val name: String, val level: Int)

data class Skill(val id: Int, val name: String)

data class Job(
    val id: Int,
    val title: String,
    val company: String,
    val location: String,
    val employmentType: String,
    val workMode: String,
    val experienceMin: Int,
    val experienceMax: Int,
    val salaryMinLpa: Double?,
    val salaryMaxLpa: Double?,
    val postedAt: String,
    val appliedAt: String? = null, // when the user applied (null if not applied)
    val matched: List<String>,
    val gaps: List<String>,
    val matchScore: Int,
    val fit: String, // "excellent" | "good" | "stretch"
    val saved: Boolean,
    val applied: Boolean = false,
    val description: String?, // only sent by the detail endpoint
)

data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val name: String, val email: String, val password: String)
data class AuthResponse(val token: String, val user: User)
data class UserResponse(val user: User)
data class JobsResponse(val jobs: List<Job>)
data class JobResponse(val job: Job)
data class SkillsResponse(val skills: List<Skill>)
data class SaveResponse(val saved: Boolean)
data class ApplyResponse(val applied: Boolean)
data class SkillLevel(val id: Int, val level: Int)

/** Null fields are left out of the JSON, so the server only changes what is sent. */
data class ProfileUpdate(
    val name: String? = null,
    val location: String? = null,
    val experienceLevel: String? = null,
    val language: String? = null,
    val jobAlerts: Boolean? = null,
    val skills: List<SkillLevel>? = null,
)

interface SkillMatchApi {
    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @GET("api/me")
    suspend fun me(): UserResponse

    @PUT("api/me")
    suspend fun updateMe(@Body body: ProfileUpdate): UserResponse

    @GET("api/jobs")
    suspend fun jobs(@Query("tab") tab: String, @Query("q") query: String): JobsResponse

    @GET("api/jobs/{id}")
    suspend fun job(@Path("id") id: Int): JobResponse

    @POST("api/jobs/{id}/save")
    suspend fun saveJob(@Path("id") id: Int): SaveResponse

    @DELETE("api/jobs/{id}/save")
    suspend fun unsaveJob(@Path("id") id: Int): SaveResponse

    @POST("api/jobs/{id}/apply")
    suspend fun applyJob(@Path("id") id: Int): ApplyResponse

    @DELETE("api/jobs/{id}/apply")
    suspend fun unapplyJob(@Path("id") id: Int): ApplyResponse

    @GET("api/skills")
    suspend fun skills(): SkillsResponse
}

object ApiClient {
    /** Set after login; sent as a Bearer token on every request. */
    @Volatile
    var token: String? = null

    private val http = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request()
            val authed = token?.let { request.newBuilder().header("Authorization", "Bearer $it").build() }
            chain.proceed(authed ?: request)
        }
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .build()

    val api: SkillMatchApi = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(http)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(SkillMatchApi::class.java)
}

/** An API failure with a message that is safe to show to the user. */
class ApiError(message: String, val status: Int? = null) : Exception(message)

private data class ErrorBody(val error: String?)

/** Runs an API call and turns network/HTTP failures into an [ApiError]. */
suspend fun <T> apiCall(block: suspend SkillMatchApi.() -> T): Result<T> = try {
    Result.success(ApiClient.api.block())
} catch (e: HttpException) {
    val message = runCatching {
        Gson().fromJson(e.response()?.errorBody()?.string(), ErrorBody::class.java)?.error
    }.getOrNull()
    Result.failure(ApiError(message ?: "Something went wrong (${e.code()})", e.code()))
} catch (e: IOException) {
    Result.failure(ApiError(SkillMatchApp.instance.getString(R.string.error_network)))
}
