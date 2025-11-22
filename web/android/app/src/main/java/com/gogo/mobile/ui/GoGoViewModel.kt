package com.gogo.mobile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gogo.mobile.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GoGoViewModel(private val repository: GoGoRepository) : ViewModel() {
    private val session = MutableStateFlow(SessionState())

    val uiState: StateFlow<GoGoUiState> = combine(repository.state, session) { repo, sess ->
        GoGoUiState(
            repoState = repo,
            currentUser = sess.currentUser,
            selectedRole = sess.selectedRole,
            listingFilter = sess.listingFilter
        )
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Eagerly, GoGoUiState())

    fun selectRole(role: UserRole) {
        session.value = session.value.copy(selectedRole = role)
    }

    fun updateFilter(action: (ListingFilter) -> ListingFilter) {
        session.value = session.value.copy(listingFilter = action(session.value.listingFilter))
    }

    fun login(email: String, password: String, role: UserRole, onResult: (Result<User>) -> Unit) {
        viewModelScope.launch {
            val result = repository.login(email.trim(), password, role)
            if (result.isSuccess) {
                session.value = session.value.copy(currentUser = result.getOrNull())
            }
            onResult(result)
        }
    }

    fun register(user: User, onResult: (Result<User>) -> Unit) {
        viewModelScope.launch {
            val result = repository.register(user)
            if (result.isSuccess) {
                session.value = session.value.copy(currentUser = result.getOrNull(), selectedRole = user.role)
            }
            onResult(result)
        }
    }

    fun logout() {
        session.value = session.value.copy(currentUser = null)
    }

    fun publishListing(
        from: String,
        to: String,
        date: String,
        flexible: Boolean,
        price: String,
        size: PackageSize,
        importance: String,
        description: String
    ): Listing? {
        val user = session.value.currentUser ?: return null
        val listing = repository.publishListing(
            ownerId = user.id,
            ownerRole = user.role,
            from = from,
            to = to,
            date = date,
            flexible = flexible,
            price = price,
            size = size,
            importance = importance,
            description = description
        )
        return listing
    }

    fun createAppointment(listingId: String, scheduled: String, note: String) {
        val driver = session.value.currentUser ?: return
        val listing = uiState.value.repoState.listings.firstOrNull { it.id == listingId } ?: return
        if (driver.role != UserRole.DRIVER) return
        repository.createAppointment(listingId, driver.id, listing.ownerId, scheduled, note)
    }

    fun ensureThread(listing: Listing): MessageThread? {
        val current = session.value.currentUser ?: return null
        val senderId = when (current.role) {
            UserRole.SENDER -> current.id
            UserRole.DRIVER -> listing.ownerId
        }
        val driverId = when (current.role) {
            UserRole.DRIVER -> current.id
            UserRole.SENDER -> listing.ownerId
        }
        val resolvedSender = if (listing.ownerRole == UserRole.SENDER) listing.ownerId else senderId
        val resolvedDriver = if (listing.ownerRole == UserRole.DRIVER) listing.ownerId else driverId
        return repository.startThread(listing.id, resolvedSender, resolvedDriver)
    }

    fun sendMessage(threadId: String, message: String) {
        val author = session.value.currentUser ?: return
        if (message.isBlank()) return
        repository.postMessage(threadId, author.id, message.trim())
    }

    fun markThreadRead(threadId: String) {
        val user = session.value.currentUser ?: return
        repository.markThreadAsRead(threadId, user.id)
    }
}

data class SessionState(
    val currentUser: User? = null,
    val selectedRole: UserRole? = null,
    val listingFilter: ListingFilter = ListingFilter()
)

data class ListingFilter(
    val from: String = "",
    val to: String = "",
    val status: ListingStatus? = ListingStatus.APPROVED
)

data class GoGoUiState(
    val repoState: GoGoState = GoGoState(),
    val currentUser: User? = null,
    val selectedRole: UserRole? = null,
    val listingFilter: ListingFilter = ListingFilter()
)

class GoGoViewModelFactory(private val repository: GoGoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GoGoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GoGoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
