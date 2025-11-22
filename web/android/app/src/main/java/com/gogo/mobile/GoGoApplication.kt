package com.gogo.mobile

import android.app.Application
import com.gogo.mobile.data.GoGoRepository

class GoGoApplication : Application() {
    lateinit var repository: GoGoRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = GoGoRepository(this)
    }
}
