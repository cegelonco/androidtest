package com.studenthousing.app.di

import android.content.Context
import com.studenthousing.app.data.local.AppDatabase
import com.studenthousing.app.data.network.NetworkModule
import com.studenthousing.app.data.repo.StudentHousingRepository
import com.studenthousing.app.data.store.TokenStore

class AppContainer(context: Context) {
    val tokenStore = TokenStore(context)
    private val db = AppDatabase.getInstance(context)
    private val api = NetworkModule.createApiService(tokenStore)

    val repository = StudentHousingRepository(
        api = api,
        tokenStore = tokenStore,
        propertyDao = db.propertyDao(),
        bookingDao = db.bookingDao()
    )
}
