package com.studenthousing.app.data.repo

import com.studenthousing.app.data.local.BookingDao
import com.studenthousing.app.data.local.BookingEntity
import com.studenthousing.app.data.local.PropertyDao
import com.studenthousing.app.data.local.PropertyEntity
import com.studenthousing.app.data.model.AuthResponse
import com.studenthousing.app.data.model.BookingDto
import com.studenthousing.app.data.model.CreateBookingRequest
import com.studenthousing.app.data.model.LoginRequest
import com.studenthousing.app.data.model.PropertyDto
import com.studenthousing.app.data.model.RegisterRequest
import com.studenthousing.app.data.model.ResendVerificationRequest
import com.studenthousing.app.data.model.VerifyEmailRequest
import com.studenthousing.app.data.network.ApiService
import com.studenthousing.app.data.store.TokenStore
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class StudentHousingRepository(
    private val api: ApiService,
    private val tokenStore: TokenStore,
    private val propertyDao: PropertyDao,
    private val bookingDao: BookingDao
) {
    suspend fun login(email: String, password: String): ResultState<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.login(LoginRequest(email, password))
            if (response.success && !response.token.isNullOrBlank()) {
                tokenStore.saveToken(response.token)
                response.user?.userType?.let { tokenStore.saveUserType(it) }
                ResultState.Success(Unit)
            } else {
                ResultState.Error(response.message ?: "Login failed")
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val parsed = try { Gson().fromJson(errorBody, AuthResponse::class.java) } catch (_: Exception) { null }
            ResultState.Error(parsed?.message ?: "Login failed (${e.code()})")
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Login error")
        }
    }

    suspend fun register(request: RegisterRequest): ResultState<Pair<String, String?>> = withContext(Dispatchers.IO) {
        try {
            val response = api.register(request)
            if (response.success) {
                ResultState.Success(Pair(request.email, response.verificationCode))
            } else {
                ResultState.Error(response.message ?: "Registration failed")
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val parsed = try { Gson().fromJson(errorBody, AuthResponse::class.java) } catch (_: Exception) { null }
            ResultState.Error(parsed?.message ?: "Registration failed (${e.code()})")
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Registration error")
        }
    }

    suspend fun resendVerification(email: String): ResultState<String?> = withContext(Dispatchers.IO) {
        try {
            val response = api.resendVerification(ResendVerificationRequest(email))
            if (response.success) ResultState.Success(response.verificationCode)
            else ResultState.Error(response.message ?: "Resend failed")
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Resend error")
        }
    }

    suspend fun verifyEmail(email: String, code: String): ResultState<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.verifyEmail(VerifyEmailRequest(email, code))
            if (response.success && !response.token.isNullOrBlank()) {
                tokenStore.saveToken(response.token)
                response.user?.userType?.let { tokenStore.saveUserType(it) }
                ResultState.Success(Unit)
            } else {
                ResultState.Error(response.message ?: "Verification failed")
            }
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Verification error")
        }
    }

    suspend fun getProperties(online: Boolean): ResultState<List<PropertyEntity>> = withContext(Dispatchers.IO) {
        if (online) {
            try {
                val response = api.getProperties()
                if (response.success) {
                    val mapped = response.properties.map { it.toEntity() }
                    propertyDao.insertAll(mapped)
                    return@withContext ResultState.Success(mapped)
                }
            } catch (e: Exception) {
                Log.w("Repository", "Network fetch failed, falling back to cache", e)
            }
        }
        val cached = propertyDao.getAll()
        if (cached.isNotEmpty()) ResultState.Success(cached) else ResultState.Error("No cached data available")
    }

    suspend fun getPropertyById(id: String): PropertyEntity? = withContext(Dispatchers.IO) {
        propertyDao.getById(id)
    }

    suspend fun loadBookings(online: Boolean): ResultState<List<BookingEntity>> = withContext(Dispatchers.IO) {
        if (online) {
            try {
                val response = api.getMyBookings()
                if (response.success) {
                    val mapped = response.bookings.map { it.toEntity() }
                    bookingDao.insertAll(mapped)
                    return@withContext ResultState.Success(mapped)
                }
            } catch (e: Exception) {
                Log.w("Repository", "Bookings fetch failed, falling back to cache", e)
            }
        }
        ResultState.Success(bookingDao.getAll())
    }

    // Roommates
    suspend fun ensureRoommateProfile() {
        try {
            // This creates a roommate profile if it doesn't exist
            api.getRoommateProfile()
        } catch (_: Exception) { }
    }

    suspend fun getPotentialRoommates(): ResultState<List<com.studenthousing.app.data.model.RoommateDto>> = withContext(Dispatchers.IO) {
        try {
            // Ensure current user has a roommate profile first
            ensureRoommateProfile()
            val response = api.getPotentialRoommates()
            if (response.success && response.roommates != null) {
                ResultState.Success(response.roommates)
            } else {
                ResultState.Success(emptyList())
            }
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Error loading roommates")
        }
    }

    suspend fun connectRoommate(roommateId: String): ResultState<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.connectRoommate(roommateId)
            if (response.success) ResultState.Success(Unit)
            else ResultState.Error(response.message ?: "Connection failed")
        } catch (e: HttpException) {
            val parsed = try { Gson().fromJson(e.response()?.errorBody()?.string(), AuthResponse::class.java) } catch (_: Exception) { null }
            ResultState.Error(parsed?.message ?: "Connection failed (${e.code()})")
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Connection error")
        }
    }

    suspend fun getRoommateConnections(): ResultState<List<com.studenthousing.app.data.model.ConnectionDto>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getRoommateConnections()
            if (response.success && response.connections != null) {
                ResultState.Success(response.connections)
            } else {
                ResultState.Success(emptyList())
            }
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Error loading connections")
        }
    }

    suspend fun getRoommateRequests(): ResultState<List<com.studenthousing.app.data.model.RoommateRequestDto>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getRoommateRequests()
            if (response.success && response.requests != null) {
                ResultState.Success(response.requests)
            } else {
                ResultState.Success(emptyList())
            }
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Error loading requests")
        }
    }

    suspend fun respondToRoommateRequest(connectionId: String, accept: Boolean): ResultState<Unit> = withContext(Dispatchers.IO) {
        try {
            val action = if (accept) "accept" else "reject"
            val response = api.respondToRoommateRequest(connectionId, com.studenthousing.app.data.model.RoommateActionRequest(action))
            if (response.success) ResultState.Success(Unit)
            else ResultState.Error(response.message ?: "Action failed")
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Error")
        }
    }

    suspend fun getOwnerBookingRequests(): ResultState<List<com.studenthousing.app.data.model.BookingDto>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getMyBookings()
            if (response.success) {
                ResultState.Success(response.bookings)
            } else {
                ResultState.Error("Failed to load booking requests")
            }
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Error loading requests")
        }
    }

    suspend fun deleteProperty(propertyId: String): ResultState<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteProperty(propertyId)
            if (response.success) ResultState.Success(Unit)
            else ResultState.Error(response.message ?: "Delete failed")
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Delete error")
        }
    }

    suspend fun confirmBooking(bookingId: String): ResultState<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.confirmBooking(bookingId)
            if (response.success) ResultState.Success(Unit)
            else ResultState.Error(response.message ?: "Confirm failed")
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Confirm error")
        }
    }

    suspend fun getMyProperties(): ResultState<List<PropertyEntity>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getMyProperties()
            if (response.success) {
                ResultState.Success(response.properties.map { it.toEntity() })
            } else {
                ResultState.Error("Failed to load your properties")
            }
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Error loading properties")
        }
    }

    suspend fun searchProperties(
        search: String? = null,
        type: String? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        minRooms: Int? = null
    ): ResultState<List<PropertyEntity>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getProperties(search, type, minPrice, maxPrice, minRooms)
            if (response.success) {
                val mapped = response.properties.map { it.toEntity() }
                ResultState.Success(mapped)
            } else {
                ResultState.Error("Search failed")
            }
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Search error")
        }
    }

    // Favorites
    suspend fun getFavorites(): ResultState<List<PropertyEntity>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getFavorites()
            if (response.success) {
                ResultState.Success(response.favorites.map { it.toEntity() })
            } else {
                ResultState.Error("Failed to load favorites")
            }
        } catch (e: HttpException) {
            val parsed = try { Gson().fromJson(e.response()?.errorBody()?.string(), AuthResponse::class.java) } catch (_: Exception) { null }
            ResultState.Error(parsed?.message ?: "Failed to load favorites")
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Favorites error")
        }
    }

    suspend fun toggleFavorite(propertyId: String, currentlyFavorite: Boolean): ResultState<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (currentlyFavorite) {
                api.removeFavorite(propertyId)
            } else {
                api.addFavorite(propertyId)
            }
            ResultState.Success(!currentlyFavorite)
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Favorite toggle error")
        }
    }

    suspend fun checkFavorite(propertyId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            api.checkFavorite(propertyId).isFavorited
        } catch (_: Exception) {
            false
        }
    }

    // Profile
    suspend fun getProfile(): ResultState<com.studenthousing.app.data.model.UserProfileDto> = withContext(Dispatchers.IO) {
        try {
            val response = api.getProfile()
            if (response.success && response.user != null) {
                ResultState.Success(response.user)
            } else {
                ResultState.Error("Failed to load profile")
            }
        } catch (e: HttpException) {
            val parsed = try { Gson().fromJson(e.response()?.errorBody()?.string(), AuthResponse::class.java) } catch (_: Exception) { null }
            ResultState.Error(parsed?.message ?: "Profile error (${e.code()})")
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Profile error")
        }
    }

    suspend fun updateProfile(request: com.studenthousing.app.data.model.UpdateProfileRequest): ResultState<com.studenthousing.app.data.model.UserProfileDto> = withContext(Dispatchers.IO) {
        try {
            val response = api.updateProfile(request)
            if (response.success && response.user != null) {
                ResultState.Success(response.user)
            } else {
                ResultState.Error("Failed to update profile")
            }
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Update error")
        }
    }

    // Booking cancel
    suspend fun cancelBooking(bookingId: String): ResultState<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.cancelBooking(bookingId)
            if (response.success) ResultState.Success(Unit)
            else ResultState.Error(response.message ?: "Cancel failed")
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Cancel error")
        }
    }

    suspend fun createBooking(propertyId: String): ResultState<Unit> = withContext(Dispatchers.IO) {
        try {
            // Check for existing pending/confirmed booking on this property
            val bookingsResult = api.getMyBookings()
            if (bookingsResult.success) {
                val existingBooking = bookingsResult.bookings.find {
                    it.property?._id == propertyId &&
                    (it.status == "pending" || it.status == "confirmed")
                }
                if (existingBooking != null) {
                    return@withContext ResultState.Error("You already have a ${existingBooking.status} booking for this property")
                }
            }
            val response = api.createBooking(CreateBookingRequest(propertyId))
            if (response.success) ResultState.Success(Unit)
            else ResultState.Error(response.message ?: "Booking failed")
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Booking error")
        }
    }

    suspend fun createProperty(request: com.studenthousing.app.data.model.CreatePropertyRequest): ResultState<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.createProperty(request)
            if (response.success) ResultState.Success(Unit)
            else ResultState.Error(response.message ?: "Failed to create property")
        } catch (e: HttpException) {
            val parsed = try { Gson().fromJson(e.response()?.errorBody()?.string(), AuthResponse::class.java) } catch (_: Exception) { null }
            ResultState.Error(parsed?.message ?: "Failed to create property (${e.code()})")
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Property creation error")
        }
    }

    private fun resolveImageUrl(path: String?): String? {
        if (path.isNullOrBlank()) return null
        if (path.startsWith("http")) return path
        val base = com.studenthousing.app.BuildConfig.API_BASE_URL
            .removeSuffix("/api/").removeSuffix("/api")
        return "$base$path"
    }

    private fun PropertyDto.toEntity(): PropertyEntity {
        return PropertyEntity(
            id = _id,
            title = title,
            address = address,
            price = price,
            description = description,
            type = type,
            city = location?.city,
            latitude = location?.coordinates?.lat,
            longitude = location?.coordinates?.lng,
            imageUrl = resolveImageUrl(images?.firstOrNull() ?: image),
            lastSyncedAt = System.currentTimeMillis()
        )
    }

    private fun BookingDto.toEntity(): BookingEntity {
        return BookingEntity(
            id = _id,
            status = status,
            finalPrice = finalPrice,
            propertyTitle = property?.title,
            lastSyncedAt = System.currentTimeMillis()
        )
    }
}
