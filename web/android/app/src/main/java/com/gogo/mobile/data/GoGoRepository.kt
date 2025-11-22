package com.gogo.mobile.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class GoGoRepository(context: Context) {
    private val prefs = context.getSharedPreferences("gogo_repo", Context.MODE_PRIVATE)
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true; encodeDefaults = true }
    private val stateFlow = MutableStateFlow(loadState())

    val state: StateFlow<GoGoState> = stateFlow.asStateFlow()

    private fun loadState(): GoGoState {
        val stored = prefs.getString("state", null)
        return if (stored.isNullOrBlank()) {
            val sample = sampleState()
            persist(sample)
            sample
        } else {
            runCatching { json.decodeFromString<GoGoState>(stored) }.getOrElse {
                val sample = sampleState()
                persist(sample)
                sample
            }
        }
    }

    private fun persist(state: GoGoState) {
        prefs.edit().putString("state", json.encodeToString(state)).apply()
        stateFlow.value = state
    }

    fun register(user: User): Result<User> {
        val current = stateFlow.value
        if (current.users.any { it.email == user.email }) {
            return Result.failure(IllegalStateException("Bu e-posta ile kayıt mevcut"))
        }
        val updated = current.copy(users = current.users + user)
        persist(updated)
        return Result.success(user)
    }

    fun login(email: String, password: String, role: UserRole): Result<User> {
        val current = stateFlow.value
        val user = current.users.firstOrNull { it.email == email && it.role == role }
            ?: return Result.failure(IllegalArgumentException("Kullanıcı bulunamadı"))
        return if (user.password == password) {
            Result.success(user)
        } else {
            Result.failure(IllegalArgumentException("Şifre hatalı"))
        }
    }

    fun saveListing(listing: Listing) {
        val current = stateFlow.value
        val listings = current.listings.toMutableList()
        val index = listings.indexOfFirst { it.id == listing.id }
        if (index >= 0) {
            listings[index] = listing
        } else {
            listings.add(listing)
        }
        persist(current.copy(listings = listings))
    }

    fun publishListing(
        ownerId: String,
        ownerRole: UserRole,
        from: String,
        to: String,
        date: String,
        flexible: Boolean,
        price: String,
        size: PackageSize,
        importance: String,
        description: String
    ): Listing {
        val listing = Listing(
            ownerId = ownerId,
            ownerRole = ownerRole,
            fromCity = from,
            toCity = to,
            departureDate = date,
            flexibleDate = flexible,
            priceExpectation = price,
            packageSize = size,
            importance = importance,
            description = description,
            status = if (ownerRole == UserRole.SENDER) ListingStatus.PENDING else ListingStatus.APPROVED
        )
        saveListing(listing)
        return listing
    }

    fun updateListingStatus(id: String, status: ListingStatus) {
        val current = stateFlow.value
        val listing = current.listings.firstOrNull { it.id == id } ?: return
        saveListing(listing.copy(status = status))
    }

    fun createAppointment(listingId: String, driverId: String, senderId: String, scheduledFor: String, note: String) {
        val appointment = Appointment(
            listingId = listingId,
            driverId = driverId,
            senderId = senderId,
            scheduledFor = scheduledFor,
            note = note
        )
        val current = stateFlow.value
        persist(current.copy(appointments = current.appointments + appointment))
    }

    fun startThread(listingId: String, senderId: String, driverId: String): MessageThread {
        val existing = stateFlow.value.threads.firstOrNull {
            it.listingId == listingId && it.senderId == senderId && it.driverId == driverId
        }
        if (existing != null) return existing
        val thread = MessageThread(
            listingId = listingId,
            senderId = senderId,
            driverId = driverId
        )
        val current = stateFlow.value
        persist(current.copy(threads = current.threads + thread))
        return thread
    }

    fun postMessage(threadId: String, authorId: String, body: String) {
        val current = stateFlow.value
        val threads = current.threads.map { thread ->
            if (thread.id == threadId) {
                val message = ChatMessage(threadId = threadId, authorId = authorId, body = body)
                thread.copy(
                    messages = thread.messages + message,
                    lastUpdated = System.currentTimeMillis(),
                    unreadBy = listOf(thread.senderId, thread.driverId).filter { it != authorId }
                )
            } else thread
        }
        persist(current.copy(threads = threads))
    }

    fun markThreadAsRead(threadId: String, userId: String) {
        val current = stateFlow.value
        val threads = current.threads.map { thread ->
            if (thread.id == threadId) {
                thread.copy(unreadBy = thread.unreadBy.filterNot { it == userId })
            } else thread
        }
        persist(current.copy(threads = threads))
    }
}
