package com.example.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

data class AuthenticateByNameRequest(
    val Username: String,
    val Pw: String = ""
)

data class JellyfinAuthResult(
    val User: JellyfinUserResponse?,
    val AccessToken: String?
)

data class JellyfinUserResponse(
    val Id: String,
    val Name: String
)

data class JellyfinUserDataDto(
    val IsFavorite: Boolean? = false,
    val PlaybackPositionTicks: Long? = 0L,
    val Played: Boolean? = false
)

data class JellyfinItemsResponse(
    val Items: List<JellyfinItemDto> = emptyList(),
    val TotalRecordCount: Int = 0
)

data class JellyfinItemDto(
    val Id: String = "",
    val Name: String? = null,
    val Overview: String? = null,
    val Type: String? = null,
    val ProductionYear: Int? = null,
    val CommunityRating: Float? = null,
    val RunTimeTicks: Long? = null,
    val SeriesName: String? = null,
    val SeasonNumber: Int? = null,
    val IndexNumber: Int? = null,
    val Genres: List<String>? = emptyList(),
    val UserData: JellyfinUserDataDto? = null,
    val ImageTags: Map<String, String>? = null,
    val BackdropImageTags: List<String>? = null
)

interface JellyfinApiService {

    @POST
    suspend fun authenticateByName(
        @Url url: String,
        @Header("X-Emby-Authorization") authHeader: String,
        @Body request: AuthenticateByNameRequest
    ): JellyfinAuthResult

    @GET
    suspend fun getItems(
        @Url url: String,
        @Header("X-Emby-Authorization") authHeader: String,
        @Query("api_key") apiKey: String? = null,
        @Query("UserId") userId: String? = null,
        @Query("ParentId") parentId: String? = null,
        @Query("IncludeItemTypes") includeItemTypes: String? = null,
        @Query("SearchTerm") searchTerm: String? = null,
        @Query("Recursive") recursive: Boolean = true,
        @Query("Fields") fields: String = "Overview,Genres,CommunityRating,ProductionYear,RunTimeTicks,SeriesName,UserData",
        @Query("Limit") limit: Int = 100
    ): JellyfinItemsResponse

    @GET
    suspend fun getEpisodes(
        @Url url: String,
        @Header("X-Emby-Authorization") authHeader: String,
        @Query("api_key") apiKey: String? = null,
        @Query("UserId") userId: String? = null,
        @Query("Fields") fields: String = "Overview,RunTimeTicks,UserData"
    ): JellyfinItemsResponse

    @GET
    suspend fun getSystemInfo(
        @Url url: String
    ): Map<String, Any>
}

