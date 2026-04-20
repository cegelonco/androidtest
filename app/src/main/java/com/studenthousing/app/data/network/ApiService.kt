package com.studenthousing.app.data.network

import com.studenthousing.app.data.model.*
import retrofit2.http.*

interface ApiService {
    // Auth
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("auth/verify-email")
    suspend fun verifyEmail(@Body request: VerifyEmailRequest): AuthResponse

    @POST("auth/resend-verification")
    suspend fun resendVerification(@Body request: ResendVerificationRequest): AuthResponse

    // Properties
    @GET("properties")
    suspend fun getProperties(
        @Query("search") search: String? = null,
        @Query("type") type: String? = null,
        @Query("minPrice") minPrice: Double? = null,
        @Query("maxPrice") maxPrice: Double? = null,
        @Query("minRooms") minRooms: Int? = null,
        @Query("limit") limit: Int = 100
    ): PropertiesResponse

    @POST("properties")
    suspend fun createProperty(@Body request: CreatePropertyRequest): CreatePropertyResponse

    @GET("properties/owner/my-properties")
    suspend fun getMyProperties(): PropertiesResponse

    @PUT("properties/{id}")
    suspend fun updateProperty(@Path("id") id: String, @Body request: CreatePropertyRequest): CreatePropertyResponse

    @HTTP(method = "DELETE", path = "properties/{id}", hasBody = false)
    suspend fun deleteProperty(@Path("id") id: String): AuthResponse

    // Bookings
    @GET("bookings/my-bookings")
    suspend fun getMyBookings(): BookingsResponse

    @POST("bookings")
    suspend fun createBooking(@Body request: CreateBookingRequest): AuthResponse

    @PUT("bookings/{id}/cancel")
    suspend fun cancelBooking(@Path("id") bookingId: String): AuthResponse

    @PUT("bookings/{id}/confirm")
    suspend fun confirmBooking(@Path("id") bookingId: String): AuthResponse

    // Favorites
    @GET("favorites")
    suspend fun getFavorites(): FavoritesResponse

    @POST("favorites/{id}")
    suspend fun addFavorite(@Path("id") propertyId: String): AuthResponse

    @HTTP(method = "DELETE", path = "favorites/{id}", hasBody = false)
    suspend fun removeFavorite(@Path("id") propertyId: String): AuthResponse

    @GET("favorites/check/{id}")
    suspend fun checkFavorite(@Path("id") propertyId: String): FavoriteCheckResponse

    // Roommates
    @GET("roommates/profile")
    suspend fun getRoommateProfile(): AuthResponse

    @GET("roommates/potential")
    suspend fun getPotentialRoommates(): RoommatesResponse

    @POST("roommates/connect/{id}")
    suspend fun connectRoommate(@Path("id") roommateId: String): AuthResponse

    @GET("roommates/requests")
    suspend fun getRoommateRequests(): RoommateRequestsResponse

    @PUT("roommates/connection/{id}")
    suspend fun respondToRoommateRequest(@Path("id") connectionId: String, @Body body: RoommateActionRequest): AuthResponse

    @GET("roommates/connections")
    suspend fun getRoommateConnections(): ConnectionsResponse

    // Profile
    @GET("users/me")
    suspend fun getProfile(): ProfileResponse

    @PUT("users/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): ProfileResponse
}
