package com.studenthousing.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.studenthousing.app.data.repo.StudentHousingRepository
import com.studenthousing.app.ui.auth.LoginViewModel
import com.studenthousing.app.ui.auth.SignupViewModel
import com.studenthousing.app.ui.auth.VerifyEmailViewModel
import com.studenthousing.app.ui.booking.BookingsViewModel
import com.studenthousing.app.ui.favorites.FavoritesViewModel
import com.studenthousing.app.ui.owner.AddPropertyViewModel
import com.studenthousing.app.ui.owner.BookingRequestsViewModel
import com.studenthousing.app.ui.owner.MyPropertiesViewModel
import com.studenthousing.app.ui.profile.ProfileViewModel
import com.studenthousing.app.ui.profile.EditProfileViewModel
import com.studenthousing.app.ui.roommates.ConnectionsViewModel
import com.studenthousing.app.ui.roommates.RoommatesViewModel
import com.studenthousing.app.ui.properties.PropertiesViewModel
import com.studenthousing.app.ui.properties.PropertyDetailViewModel

class CommonViewModelFactory(
    private val repository: StudentHousingRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> LoginViewModel(repository) as T
            modelClass.isAssignableFrom(SignupViewModel::class.java) -> SignupViewModel(repository) as T
            modelClass.isAssignableFrom(VerifyEmailViewModel::class.java) -> VerifyEmailViewModel(repository) as T
            modelClass.isAssignableFrom(PropertiesViewModel::class.java) -> PropertiesViewModel(repository) as T
            modelClass.isAssignableFrom(PropertyDetailViewModel::class.java) -> PropertyDetailViewModel(repository) as T
            modelClass.isAssignableFrom(BookingsViewModel::class.java) -> BookingsViewModel(repository) as T
            modelClass.isAssignableFrom(FavoritesViewModel::class.java) -> FavoritesViewModel(repository) as T
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> ProfileViewModel(repository) as T
            modelClass.isAssignableFrom(EditProfileViewModel::class.java) -> EditProfileViewModel(repository) as T
            modelClass.isAssignableFrom(AddPropertyViewModel::class.java) -> AddPropertyViewModel(repository) as T
            modelClass.isAssignableFrom(MyPropertiesViewModel::class.java) -> MyPropertiesViewModel(repository) as T
            modelClass.isAssignableFrom(BookingRequestsViewModel::class.java) -> BookingRequestsViewModel(repository) as T
            modelClass.isAssignableFrom(RoommatesViewModel::class.java) -> RoommatesViewModel(repository) as T
            modelClass.isAssignableFrom(ConnectionsViewModel::class.java) -> ConnectionsViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
