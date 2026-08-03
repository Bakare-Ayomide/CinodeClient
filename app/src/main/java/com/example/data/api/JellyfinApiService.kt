package com.example.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
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

data class CreateUserByNameRequest(
    val Name: String,
    val Password: String? = null
)

data class JellyfinAuthResult(
    val User: JellyfinUserResponse?,
    val AccessToken: String?
)

data class JellyfinUserResponse(
    val Id: String = "",
    val Name: String = "",
    val ServerId: String? = null,
    val HasPassword: Boolean? = false,
    val HasConfiguredPassword: Boolean? = false,
    val Policy: JellyfinUserPolicyDto? = null
)

data class JellyfinUserPolicyDto(
    val IsAdministrator: Boolean? = false,
    val IsHidden: Boolean? = false,
    val IsDisabled: Boolean? = false,
    val EnableContentDownloading: Boolean? = true,
    val EnableMediaPlayback: Boolean? = true
)

data class JellyfinSessionDto(
    val Id: String = "",
    val UserId: String? = null,
    val UserName: String? = null,
    val Client: String? = null,
    val DeviceName: String? = null,
    val ApplicationVersion: String? = null,
    val RemoteEndPoint: String? = null,
    val NowPlayingItem: JellyfinItemDto? = null
)

data class JellyfinSystemInfoDto(
    val SystemName: String? = null,
    val Version: String? = null,
    val OperatingSystem: String? = null,
    val ServerName: String? = null,
    val LocalAddress: String? = null,
    val WanAddress: String? = null,
    val HasPendingRestart: Boolean? = false,
    val CanSelfRestart: Boolean? = false
)

data class JellyfinMediaFolderDto(
    val Id: String = "",
    val Name: String? = null,
    val CollectionType: String? = null
)

data class JellyfinMediaFoldersResponse(
    val Items: List<JellyfinMediaFolderDto> = emptyList()
)

data class JellyfinActivityLogDto(
    val Id: Long = 0L,
    val Name: String? = null,
    val Overview: String? = null,
    val Date: String? = null,
    val UserId: String? = null,
    val Severity: String? = null
)

data class JellyfinActivityLogsResponse(
    val Items: List<JellyfinActivityLogDto> = emptyList(),
    val TotalRecordCount: Int = 0
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

    @POST
    suspend fun createUserByName(
        @Url url: String,
        @Header("X-Emby-Authorization") authHeader: String,
        @Query("api_key") apiKey: String? = null,
        @Body request: CreateUserByNameRequest
    ): JellyfinUserResponse

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

    @GET
    suspend fun getUsers(
        @Url url: String,
        @Header("X-Emby-Authorization") authHeader: String,
        @Query("api_key") apiKey: String? = null
    ): List<JellyfinUserResponse>

    @DELETE
    suspend fun deleteUser(
        @Url url: String,
        @Header("X-Emby-Authorization") authHeader: String,
        @Query("api_key") apiKey: String? = null
    ): Response<Unit>

    @GET
    suspend fun getSessions(
        @Url url: String,
        @Header("X-Emby-Authorization") authHeader: String,
        @Query("api_key") apiKey: String? = null
    ): List<JellyfinSessionDto>

    @GET
    suspend fun getMediaFolders(
        @Url url: String,
        @Header("X-Emby-Authorization") authHeader: String,
        @Query("api_key") apiKey: String? = null
    ): JellyfinMediaFoldersResponse

    @POST
    suspend fun refreshLibrary(
        @Url url: String,
        @Header("X-Emby-Authorization") authHeader: String,
        @Query("api_key") apiKey: String? = null
    ): Response<Unit>

    @GET
    suspend fun getSystemInfoDetails(
        @Url url: String,
        @Header("X-Emby-Authorization") authHeader: String,
        @Query("api_key") apiKey: String? = null
    ): JellyfinSystemInfoDto

    @GET
    suspend fun getActivityLogs(
        @Url url: String,
        @Header("X-Emby-Authorization") authHeader: String,
        @Query("api_key") apiKey: String? = null,
        @Query("Limit") limit: Int = 20
    ): JellyfinActivityLogsResponse
}

