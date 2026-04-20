package com.studenthousing.app.ui.properties

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studenthousing.app.BuildConfig
import com.studenthousing.app.data.local.PropertyEntity
import com.studenthousing.app.data.repo.ResultState
import com.studenthousing.app.data.repo.StudentHousingRepository
import com.studenthousing.app.util.LocationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PropertiesViewModel(
    private val repository: StudentHousingRepository
) : ViewModel() {
    private val _state = MutableLiveData<ResultState<List<PropertyEntity>>>()
    val state: LiveData<ResultState<List<PropertyEntity>>> = _state

    private val _offline = MutableLiveData(false)
    val offline: LiveData<Boolean> = _offline

    private var lastItems: List<PropertyEntity> = emptyList()
    private var searchJob: Job? = null

    // User location (set when "Near Me" is enabled and GPS is available)
    private var userLat: Double? = null
    private var userLng: Double? = null

    fun setUserLocation(lat: Double?, lng: Double?) {
        userLat = lat
        userLng = lng
    }

    fun load(online: Boolean) {
        _offline.value = !online
        _state.value = ResultState.Loading
        viewModelScope.launch {
            val result = repository.getProperties(online)
            if (result is ResultState.Success) lastItems = result.data
            _state.value = result
        }
    }

    fun search(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400) // debounce
            if (query.isBlank()) {
                _state.value = ResultState.Success(lastItems)
                return@launch
            }
            _state.value = ResultState.Loading
            val result = repository.searchProperties(search = query)
            _state.value = result
        }
    }

    fun applyFilters(
        type: String? = null,
        minRooms: Int? = null,
        maxPrice: Double? = null,
        campusLat: Double? = null,
        campusLng: Double? = null,
        maxDistanceKm: Double? = null
    ) {
        _state.value = ResultState.Loading
        viewModelScope.launch {
            val result = repository.searchProperties(
                type = type,
                minRooms = minRooms,
                maxPrice = maxPrice
            )
            if (result is ResultState.Success &&
                campusLat != null && campusLat != 0.0 &&
                campusLng != null && campusLng != 0.0 &&
                maxDistanceKm != null && maxDistanceKm > 0.0
            ) {
                val filtered = result.data.filter { property ->
                    val lat = property.latitude
                    val lng = property.longitude
                    if (lat == null || lng == null) false
                    else LocationHelper.distanceKm(lat, lng, campusLat, campusLng) <= maxDistanceKm
                }
                lastItems = filtered
                _state.value = ResultState.Success(filtered)
            } else {
                if (result is ResultState.Success) lastItems = result.data
                _state.value = result
            }
        }
    }

    fun sortByDistanceToCampus(enabled: Boolean): List<PropertyEntity> {
        if (!enabled) return lastItems
        // Prefer user's real GPS location if available, otherwise fall back to hardcoded campus
        val refLat = userLat ?: BuildConfig.CAMPUS_LAT
        val refLng = userLng ?: BuildConfig.CAMPUS_LNG
        return lastItems.sortedBy { item ->
            if (item.latitude == null || item.longitude == null) Double.MAX_VALUE
            else LocationHelper.distanceKm(
                item.latitude,
                item.longitude,
                refLat,
                refLng
            )
        }
    }

    fun sortByPriceLowToHigh(): List<PropertyEntity> = lastItems.sortedBy { it.price }

    fun sortByPriceHighToLow(): List<PropertyEntity> = lastItems.sortedByDescending { it.price }

    fun getDefault(): List<PropertyEntity> = lastItems
}
