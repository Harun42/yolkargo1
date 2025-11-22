package com.gogo.mobile.data

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
enum class UserRole { DRIVER, SENDER }

@Serializable
enum class ListingStatus(val label: String) {
    PENDING("Beklemede"),
    APPROVED("Yayında"),
    IN_TRANSIT("Yolda"),
    DELIVERED("Teslim Edildi")
}

@Serializable
enum class PackageSize(val label: String) {
    SMALL("Küçük"), MEDIUM("Orta"), LARGE("Büyük")
}

@Serializable
data class User(
    val id: String = UUID.randomUUID().toString(),
    val role: UserRole,
    val fullName: String,
    val email: String,
    val phone: String,
    val password: String,
    val city: String,
    val agreementAccepted: Boolean,
    val verified: Boolean = false,
    val rating: Double = 4.8
)

@Serializable
data class Listing(
    val id: String = UUID.randomUUID().toString(),
    val ownerId: String,
    val ownerRole: UserRole,
    val fromCity: String,
    val toCity: String,
    val departureDate: String,
    val flexibleDate: Boolean,
    val priceExpectation: String,
    val packageSize: PackageSize,
    val importance: String,
    val description: String,
    val status: ListingStatus = ListingStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class Appointment(
    val id: String = UUID.randomUUID().toString(),
    val listingId: String,
    val driverId: String,
    val senderId: String,
    val note: String,
    val scheduledFor: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val threadId: String,
    val authorId: String,
    val body: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class MessageThread(
    val id: String = UUID.randomUUID().toString(),
    val listingId: String,
    val senderId: String,
    val driverId: String,
    val lastUpdated: Long = System.currentTimeMillis(),
    val unreadBy: List<String> = listOf(senderId, driverId),
    val messages: List<ChatMessage> = emptyList()
)

@Serializable
data class GoGoState(
    val users: List<User> = emptyList(),
    val listings: List<Listing> = emptyList(),
    val appointments: List<Appointment> = emptyList(),
    val threads: List<MessageThread> = emptyList()
)

fun sampleState(): GoGoState {
    val sender = User(
        role = UserRole.SENDER,
        fullName = "Ezgi Yılmaz",
        email = "ezgi@gogo.com",
        phone = "+905551112233",
        password = "123456",
        city = "İzmir",
        agreementAccepted = true,
        verified = true
    )
    val driver = User(
        role = UserRole.DRIVER,
        fullName = "Umut Kaya",
        email = "umut@gogo.com",
        phone = "+905557778899",
        password = "123456",
        city = "İstanbul",
        agreementAccepted = true,
        verified = true
    )
    val listings = listOf(
        Listing(
            ownerId = sender.id,
            ownerRole = sender.role,
            fromCity = "İzmir",
            toCity = "Ankara",
            departureDate = "2024-11-25",
            flexibleDate = true,
            priceExpectation = "900-1100 TL",
            packageSize = PackageSize.MEDIUM,
            importance = "Kırılabilir cihaz",
            description = "Drone paketim güvenle ulaştırılmalı",
            status = ListingStatus.APPROVED
        ),
        Listing(
            ownerId = sender.id,
            ownerRole = sender.role,
            fromCity = "İstanbul",
            toCity = "Adana",
            departureDate = "2024-11-27",
            flexibleDate = false,
            priceExpectation = "1200 TL",
            packageSize = PackageSize.LARGE,
            importance = "Acil teslimat",
            description = "Büyük koli için sürücü arıyorum",
            status = ListingStatus.IN_TRANSIT
        )
    )
    val thread = MessageThread(
        listingId = listings.first().id,
        senderId = sender.id,
        driverId = driver.id,
        messages = listOf(
            ChatMessage(
                threadId = "seed",
                authorId = sender.id,
                body = "Merhaba Umut Bey, paketi akşam teslim edebilirim.",
            ),
            ChatMessage(
                threadId = "seed",
                authorId = driver.id,
                body = "Harika, navigasyon pinini paylaşabilir misiniz?",
            )
        ),
        unreadBy = listOf(sender.id)
    )
    return GoGoState(
        users = listOf(sender, driver),
        listings = listings,
        appointments = listOf(
            Appointment(
                listingId = listings.first().id,
                driverId = driver.id,
                senderId = sender.id,
                note = "Çarşamba 11:00 depoda",
                scheduledFor = "2024-11-24 11:00"
            )
        ),
        threads = listOf(
            thread.copy(
                id = thread.id,
                messages = thread.messages.map { it.copy(threadId = thread.id) }
            )
        )
    )
}
