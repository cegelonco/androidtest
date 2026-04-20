package com.studenthousing.app.data.model

data class AuthResponse(
    val success: Boolean,
    val message: String?,
    val token: String?,
    val user: UserDto?,
    val verificationCode: String?
)

data class UserDto(
    val _id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val userType: String
)

data class PropertiesResponse(
    val success: Boolean,
    val properties: List<PropertyDto>
)

data class PropertyDto(
    val _id: String,
    val title: String,
    val address: String,
    val price: Double,
    val description: String?,
    val type: String?,
    val location: LocationDto?,
    val images: List<String>?,
    val image: String?
)

data class LocationDto(
    val city: String?,
    val state: String?,
    val country: String?,
    val coordinates: LocationCoordsDto?
)

data class LocationCoordsDto(
    val lat: Double?,
    val lng: Double?
)

data class BookingDto(
    val _id: String,
    val status: String,
    val finalPrice: Double?,
    val property: PropertyDto?,
    val student: UserDto?
)

data class BookingsResponse(
    val success: Boolean,
    val bookings: List<BookingDto>
)

data class LoginRequest(val email: String, val password: String)
data class VerifyEmailRequest(val email: String, val code: String)
data class CreateBookingRequest(val propertyId: String)

data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val phoneCode: String = "+961",
    val password: String,
    val university: String?,
    val department: String?,
    val budget: Double?,
    val userType: String = "student"
)

data class ResendVerificationRequest(val email: String)

// Favorites
data class FavoritesResponse(
    val success: Boolean,
    val favorites: List<PropertyDto>
)

data class FavoriteCheckResponse(
    val success: Boolean,
    val isFavorited: Boolean
)

// Profile
data class ProfileResponse(
    val success: Boolean,
    val user: UserProfileDto?
)

data class UserProfileDto(
    val _id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String?,
    val phoneCode: String?,
    val university: String?,
    val department: String?,
    val userType: String,
    val budget: BudgetDto?,
    val age: Int?,
    val rating: Double?,
    val totalProperties: Int?,
    val responseTime: String?
)

data class BudgetDto(
    val min: Double?,
    val max: Double?
)

data class UpdateProfileRequest(
    val phone: String?,
    val university: String?,
    val department: String?
)

// Property creation
data class CreatePropertyRequest(
    val title: String,
    val description: String?,
    val address: String,
    val location: CreateLocationDto?,
    val images: List<String>?,
    val price: Double,
    val type: String,
    val rooms: Int,
    val floor: Int?,
    val amenities: List<String>?
)

data class CreateLocationDto(
    val city: String?,
    val state: String?,
    val country: String?,
    val coordinates: CoordinatesDto?
)

data class CoordinatesDto(
    val lat: Double,
    val lng: Double
)

data class CreatePropertyResponse(
    val success: Boolean,
    val message: String?,
    val property: PropertyDto?
)

// Roommates
data class RoommateDto(
    val _id: String,
    val student: UserDto?,
    val university: String?,
    val department: String?,
    val age: Int?,
    val budget: BudgetDto?,
    val isLookingForRoommate: Boolean?
)

data class RoommatesResponse(
    val success: Boolean,
    val roommates: List<RoommateDto>?
)

data class ConnectionsResponse(
    val success: Boolean,
    val connections: List<ConnectionDto>?
)

data class ConnectionDto(
    val user: RoommateDto?,
    val status: String?,
    val createdAt: String?
)

data class RoommateRequestDto(
    val connectionId: String?,
    val fromStudent: UserDto?,
    val status: String?,
    val createdAt: String?
)

data class RoommateRequestsResponse(
    val success: Boolean,
    val requests: List<RoommateRequestDto>?
)

data class RoommateActionRequest(
    val action: String  // "accept" or "reject"
)
