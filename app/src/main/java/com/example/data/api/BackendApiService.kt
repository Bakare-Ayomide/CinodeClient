package com.example.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

data class BackendApiResponse<T>(
    val isSuccess: Boolean = false,
    val message: String = "",
    val data: T? = null
)

data class BackendAuthData(
    val user_id: Int? = null,
    val username: String? = null,
    val email: String? = null,
    val full_name: String? = null,
    val is_admin: Boolean? = false,
    val is_premium: Boolean? = false,
    val jellyfin_user_id: String? = null,
    val token: String? = null,
    val server_url: String? = null
)

data class BackendSignupRequest(
    val name: String,
    val email: String,
    val username: String,
    val password: String,
    val preferred_quality: String? = "4K Ultra HD"
)

data class BackendLoginRequest(
    val username: String,
    val password: String
)

data class BackendChangePasswordRequest(
    val username: String,
    val current_password: String? = null,
    val new_password: String
)

data class BackendPaymentInitRequest(
    val amount: Double = 2500.0,
    val customer_name: String = "",
    val customer_email: String = "",
    val item_title: String = "",
    val username: String = ""
)

data class BackendPaymentInitData(
    val paymentReference: String? = null,
    val transactionReference: String? = null,
    val checkoutUrl: String? = null,
    val virtualAccountNumber: String? = null,
    val bankName: String? = null,
    val ussdCode: String? = null,
    val amount: Double? = 2500.0
)

data class BackendPaymentVerifyRequest(
    val payment_reference: String,
    val username: String = ""
)

data class BackendPaymentVerifyData(
    val paymentStatus: String? = "PAID",
    val amountPaid: Double? = 2500.0,
    val paymentReference: String? = null,
    val isPremiumActivated: Boolean? = false
)

interface BackendApiService {

    @POST
    suspend fun signup(
        @Url url: String,
        @Body request: BackendSignupRequest
    ): BackendApiResponse<BackendAuthData>

    @POST
    suspend fun login(
        @Url url: String,
        @Body request: BackendLoginRequest
    ): BackendApiResponse<BackendAuthData>

    @POST
    suspend fun changePassword(
        @Url url: String,
        @Body request: BackendChangePasswordRequest
    ): BackendApiResponse<Unit>

    @GET
    suspend fun getUserProfile(
        @Url url: String,
        @Query("username") username: String
    ): BackendApiResponse<BackendAuthData>

    @POST
    suspend fun initializePayment(
        @Url url: String,
        @Body request: BackendPaymentInitRequest
    ): BackendApiResponse<BackendPaymentInitData>

    @POST
    suspend fun verifyPayment(
        @Url url: String,
        @Body request: BackendPaymentVerifyRequest
    ): BackendApiResponse<BackendPaymentVerifyData>

    @GET
    suspend fun getItems(
        @Url url: String,
        @Query("IncludeItemTypes") includeItemTypes: String? = null,
        @Query("SearchTerm") searchTerm: String? = null,
        @Query("Limit") limit: Int = 100
    ): BackendApiResponse<JellyfinItemsResponse>

    @GET
    suspend fun getEpisodes(
        @Url url: String,
        @Query("seriesId") seriesId: String
    ): BackendApiResponse<JellyfinItemsResponse>

    @GET
    suspend fun getUsers(
        @Url url: String
    ): BackendApiResponse<List<JellyfinUserResponse>>

    @POST
    suspend fun createUser(
        @Url url: String,
        @Body request: CreateUserByNameRequest
    ): BackendApiResponse<JellyfinUserResponse>

    @DELETE
    suspend fun deleteUser(
        @Url url: String,
        @Query("userId") userId: String
    ): BackendApiResponse<Unit>

    @GET
    suspend fun getSessions(
        @Url url: String
    ): BackendApiResponse<List<JellyfinSessionDto>>

    @GET
    suspend fun getMediaFolders(
        @Url url: String
    ): BackendApiResponse<JellyfinMediaFoldersResponse>

    @POST
    suspend fun refreshLibrary(
        @Url url: String
    ): BackendApiResponse<Unit>

    @GET
    suspend fun getActivityLogs(
        @Url url: String
    ): BackendApiResponse<JellyfinActivityLogsResponse>
}
