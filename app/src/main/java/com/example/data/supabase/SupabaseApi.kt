package com.example.data.supabase

import com.example.data.model.DomainRequest
import com.example.data.model.NewDomainRequestPayload
import com.example.data.model.UpdateDomainPayload
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface SupabaseApi {
    @GET("domain_requests?order=created_at.desc")
    suspend fun getAllDomainRequests(): Response<List<DomainRequest>>

    @POST("domain_requests")
    suspend fun createDomainRequest(
        @Body payload: NewDomainRequestPayload
    ): Response<List<DomainRequest>>

    @PATCH("domain_requests")
    suspend fun updateDomainRequest(
        @Query("id") idFilter: String, // e.g. "eq.<uuid>"
        @Body payload: UpdateDomainPayload
    ): Response<List<DomainRequest>>

    @DELETE("domain_requests")
    suspend fun deleteDomainRequest(
        @Query("id") idFilter: String // e.g. "eq.<uuid>"
    ): Response<Unit>

    @GET("domain_requests?limit=1")
    suspend fun pingTable(): Response<List<DomainRequest>>
}
