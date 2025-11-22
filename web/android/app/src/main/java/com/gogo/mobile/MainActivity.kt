package com.gogo.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
import com.gogo.mobile.data.*
import com.gogo.mobile.ui.GoGoViewModel
import com.gogo.mobile.ui.GoGoViewModelFactory
import com.gogo.mobile.ui.GoGoUiState
import com.gogo.mobile.ui.theme.GoGoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val application = application as GoGoApplication
            val vm: GoGoViewModel = viewModel(factory = GoGoViewModelFactory(application.repository))
            GoGoTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    GoGoApp(viewModel = vm)
                }
            }
        }
    }
}

@Composable
fun GoGoApp(viewModel: GoGoViewModel) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()

    NavHost(navController = navController, startDestination = "welcome") {
        composable("welcome") {
            RoleScreen(
                uiState = uiState,
                onSelect = {
                    viewModel.selectRole(it)
                    navController.navigate("login")
                },
                onAdmin = { navController.navigate("login") }
            )
        }
        composable("login") {
            LoginScreen(
                uiState = uiState,
                onBack = { navController.popBackStack() },
                onRoleChange = viewModel::selectRole,
                onSubmit = { email, pass, role ->
                    viewModel.login(email, pass, role) { result ->
                        result.onSuccess { navController.navigate("home") { popUpTo("welcome") { inclusive = true } } }
                    }
                },
                onNavigateRegister = { navController.navigate("register") }
            )
        }
        composable("register") {
            RegisterScreen(
                uiState = uiState,
                onSubmit = { user ->
                    viewModel.register(user) { result ->
                        result.onSuccess {
                            navController.navigate("home") { popUpTo("welcome") { inclusive = true } }
                        }
                    }
                },
                onRoleChange = viewModel::selectRole,
                onBack = { navController.popBackStack() }
            )
        }
        composable("home") {
            HomeScreen(
                uiState = uiState,
                onFilterChange = viewModel::updateFilter,
                onListingDetail = { navController.navigate("listing/${it.id}") },
                onCreateListing = {
                    if (uiState.currentUser == null) navController.navigate("login") else navController.navigate("createListing")
                },
                onNavigateMessages = { navController.navigate("messages") },
                onNavigateAppointments = { navController.navigate("appointments") },
                onLogout = {
                    viewModel.logout()
                    navController.navigate("welcome") { popUpTo("welcome") { inclusive = true } }
                }
            )
        }
        composable("createListing") {
            CreateListingScreen(
                uiState = uiState,
                onSubmit = { from, to, date, flexible, price, size, importance, desc ->
                    val created = viewModel.publishListing(from, to, date, flexible, price, size, importance, desc)
                    if (created != null) {
                        navController.popBackStack()
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "listing/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) {
            val id = it.arguments?.getString("id") ?: return@composable
            ListingDetailScreen(
                uiState = uiState,
                listingId = id,
                onStartChat = { listing ->
                    val thread = viewModel.ensureThread(listing)
                    if (thread != null) {
                        navController.navigate("thread/${thread.id}")
                    }
                },
                onCreateAppointment = { listing, scheduled, note ->
                    viewModel.createAppointment(listing.id, scheduled, note)
                    navController.navigate("appointments")
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable("messages") {
            MessagesScreen(
                uiState = uiState,
                onOpenThread = { navController.navigate("thread/${it.id}") },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "thread/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { entry ->
            val threadId = entry.arguments?.getString("id") ?: return@composable
            ThreadScreen(
                uiState = uiState,
                threadId = threadId,
                onSend = viewModel::sendMessage,
                onRead = viewModel::markThreadRead,
                onBack = { navController.popBackStack() }
            )
        }
        composable("appointments") {
            AppointmentScreen(
                uiState = uiState,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun RoleScreen(uiState: GoGoUiState, onSelect: (UserRole) -> Unit, onAdmin: () -> Unit) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text("GoGo", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Şehirler arası kargo paylaşım ağı", style = MaterialTheme.typography.bodyLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                RoleCard(title = "Gönderici", desc = "Paket oluştur ve sürücü bul", onClick = { onSelect(UserRole.SENDER) })
                RoleCard(title = "Sürücü", desc = "Boş alanını gelir kaynağına çevir", onClick = { onSelect(UserRole.DRIVER) })
            }
            OutlinedButton(onClick = onAdmin) {
                Text("Var olan hesabımla giriş yap")
            }
        }
    }
}

@Composable
fun RoleCard(title: String, desc: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .weight(1f)
            .heightIn(min = 160.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(desc, style = MaterialTheme.typography.bodyMedium)
            Text("Seç", color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun LoginScreen(
    uiState: GoGoUiState,
    onBack: () -> Unit,
    onRoleChange: (UserRole) -> Unit,
    onSubmit: (String, String, UserRole) -> Unit,
    onNavigateRegister: () -> Unit
) {
    var email by remember { mutableStateOf(uiState.currentUser?.email ?: "") }
    var password by remember { mutableStateOf("123456") }
    val role = uiState.selectedRole ?: UserRole.SENDER

    Scaffold(topBar = {
        SmallTopAppBar(title = { Text("Giriş Yap") }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.Outlined.AccountCircle, contentDescription = null) }
        })
    }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RoleSegmented(role, onRoleChange)
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("E-posta") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Şifre") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = { onSubmit(email, password, role) }, modifier = Modifier.fillMaxWidth()) {
                Text("Giriş Yap")
            }
            TextButton(onClick = onNavigateRegister) { Text("Hesabın yok mu? Kayıt ol") }
        }
    }
}

@Composable
fun RegisterScreen(
    uiState: GoGoUiState,
    onSubmit: (User) -> Unit,
    onRoleChange: (UserRole) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var agreement by remember { mutableStateOf(false) }
    val role = uiState.selectedRole ?: UserRole.SENDER

    Scaffold(topBar = {
        SmallTopAppBar(title = { Text("Kayıt Ol") }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.Outlined.AccountCircle, contentDescription = null) }
        })
    }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RoleSegmented(role, onRoleChange)
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Ad Soyad") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("E-posta") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Telefon") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("Şehir") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Şifre") }, modifier = Modifier.fillMaxWidth())
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = agreement, onCheckedChange = { agreement = it })
                Text("Sözleşmeyi okudum onayladım")
            }
            Button(
                onClick = {
                    if (agreement) {
                        onSubmit(
                            User(
                                role = role,
                                fullName = name,
                                email = email,
                                phone = phone,
                                password = password,
                                city = city,
                                agreementAccepted = agreement
                            )
                        )
                    }
                },
                enabled = agreement,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Kayıt Ol")
            }
        }
    }
}

@Composable
fun RoleSegmented(selected: UserRole, onSelect: (UserRole) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        FilterChip(selected = selected == UserRole.SENDER, onClick = { onSelect(UserRole.SENDER) }, label = { Text("Gönderici") })
        FilterChip(selected = selected == UserRole.DRIVER, onClick = { onSelect(UserRole.DRIVER) }, label = { Text("Sürücü") })
    }
}

@Composable
fun HomeScreen(
    uiState: GoGoUiState,
    onFilterChange: ((ListingFilter) -> ListingFilter) -> Unit,
    onListingDetail: (Listing) -> Unit,
    onCreateListing: () -> Unit,
    onNavigateMessages: () -> Unit,
    onNavigateAppointments: () -> Unit,
    onLogout: () -> Unit
) {
    val listings = remember(uiState.repoState.listings, uiState.listingFilter) {
        uiState.repoState.listings.filter { listing ->
            val matchesFrom = uiState.listingFilter.from.isBlank() || listing.fromCity.contains(uiState.listingFilter.from, true)
            val matchesTo = uiState.listingFilter.to.isBlank() || listing.toCity.contains(uiState.listingFilter.to, true)
            val matchesStatus = uiState.listingFilter.status?.let { listing.status == it } ?: true
            matchesFrom && matchesTo && matchesStatus
        }
    }
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateListing) {
                Icon(Icons.Filled.Add, contentDescription = null)
            }
        },
        topBar = {
            HomeTopBar(
                currentUser = uiState.currentUser,
                unreadThreads = uiState.repoState.threads.count { thread ->
                    val id = uiState.currentUser?.id
                    id != null && thread.unreadBy.contains(id)
                },
                onMessages = onNavigateMessages,
                onAppointments = onNavigateAppointments,
                onLogout = onLogout
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Canlı ilanlar", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            FilterRow(uiState.listingFilter, onFilterChange)
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(listings) { listing ->
                    ListingCard(listing = listing, owner = uiState.repoState.users.firstOrNull { it.id == listing.ownerId }, onClick = { onListingDetail(listing) })
                }
            }
        }
    }
}

@Composable
fun HomeTopBar(
    currentUser: User?,
    unreadThreads: Int,
    onMessages: () -> Unit,
    onAppointments: () -> Unit,
    onLogout: () -> Unit
) {
    TopAppBar(
        title = { Text("GoGo") },
        actions = {
            var expanded by remember { mutableStateOf(false) }
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Outlined.AccountCircle, contentDescription = "Profil")
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                if (currentUser == null) {
                    DropdownMenuItem(text = { Text("Lütfen giriş yap") }, onClick = { expanded = false })
                } else {
                    DropdownMenuItem(text = { Text(currentUser.fullName) }, onClick = { expanded = false })
                    DropdownMenuItem(text = { Text("Mesajlarım (${unreadThreads})") }, onClick = {
                        expanded = false
                        onMessages()
                    })
                    DropdownMenuItem(text = { Text("Randevularım") }, onClick = {
                        expanded = false
                        onAppointments()
                    })
                    DropdownMenuItem(text = { Text("Çıkış yap") }, onClick = {
                        expanded = false
                        onLogout()
                    })
                }
            }
        }
    )
}

@Composable
fun FilterRow(filter: ListingFilter, onFilterChange: ((ListingFilter) -> ListingFilter) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = filter.from,
                onValueChange = { onFilterChange { f -> f.copy(from = it) } },
                label = { Text("Nereden") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = filter.to,
                onValueChange = { onFilterChange { f -> f.copy(to = it) } },
                label = { Text("Nereye") },
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ListingStatus.values().forEach { status ->
                FilterChip(
                    selected = filter.status == status,
                    onClick = { onFilterChange { f -> f.copy(status = status) } },
                    label = { Text(status.label) }
                )
            }
            FilterChip(selected = filter.status == null, onClick = { onFilterChange { it.copy(status = null) } }, label = { Text("Tümü") })
        }
    }
}

@Composable
fun ListingCard(listing: Listing, owner: User?, onClick: () -> Unit) {
    Card(onClick = onClick) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("${listing.fromCity} ➜ ${listing.toCity}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("${listing.departureDate} • ${listing.packageSize.label}", style = MaterialTheme.typography.bodyMedium)
            Text(listing.description, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text("Durum: ${listing.status.label}", color = MaterialTheme.colorScheme.primary)
            if (owner != null) {
                Text("Gönderen: ${owner.fullName}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun CreateListingScreen(
    uiState: GoGoUiState,
    onSubmit: (String, String, String, Boolean, String, PackageSize, String, String) -> Unit,
    onBack: () -> Unit
) {
    var from by remember { mutableStateOf(uiState.currentUser?.city ?: "") }
    var to by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var flexible by remember { mutableStateOf(true) }
    var price by remember { mutableStateOf("") }
    var size by remember { mutableStateOf(PackageSize.MEDIUM) }
    var importance by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    Scaffold(topBar = {
        SmallTopAppBar(title = { Text("İlan Oluştur") }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.Outlined.AccountCircle, contentDescription = null) }
        })
    }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.currentUser == null) {
                Text("İlan oluşturmak için giriş yapın", color = Color.Red)
                Button(onClick = onBack) { Text("Geri") }
            } else {
                OutlinedTextField(value = from, onValueChange = { from = it }, label = { Text("Çıkış ili") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = to, onValueChange = { to = it }, label = { Text("Varış ili") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Gönderim tarihi") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Bütçe / teklif") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = importance, onValueChange = { importance = it }, label = { Text("Önem derecesi") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Açıklama") }, modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = flexible, onCheckedChange = { flexible = it })
                    Text("Tarih esnek")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PackageSize.values().forEach {
                        FilterChip(selected = size == it, onClick = { size = it }, label = { Text(it.label) })
                    }
                }
                Button(onClick = { onSubmit(from, to, date, flexible, price, size, importance, desc) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Yayınla")
                }
            }
        }
    }
}

@Composable
fun ListingDetailScreen(
    uiState: GoGoUiState,
    listingId: String,
    onStartChat: (Listing) -> Unit,
    onCreateAppointment: (Listing, String, String) -> Unit,
    onBack: () -> Unit
) {
    val listing = uiState.repoState.listings.firstOrNull { it.id == listingId }
    val owner = uiState.repoState.users.firstOrNull { it.id == listing?.ownerId }
    var appointmentNote by remember { mutableStateOf("") }
    var appointmentDate by remember { mutableStateOf("") }

    Scaffold(topBar = {
        SmallTopAppBar(title = { Text("İlan Detayı") }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.Outlined.AccountCircle, contentDescription = null) }
        })
    }) { padding ->
        if (listing == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("İlan bulunamadı") }
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("${listing.fromCity} ➜ ${listing.toCity}", style = MaterialTheme.typography.headlineSmall)
            Text(listing.description)
            Text("Durum: ${listing.status.label}", color = MaterialTheme.colorScheme.primary)
            Text("Paket boyutu: ${listing.packageSize.label}")
            owner?.let {
                Text("İletişim: ${it.fullName} (${it.phone})")
            }
            Button(onClick = { onStartChat(listing) }, enabled = uiState.currentUser != null) {
                Text("Mesaj Gönder")
            }
            if (uiState.currentUser?.role == UserRole.DRIVER) {
                OutlinedTextField(value = appointmentDate, onValueChange = { appointmentDate = it }, label = { Text("Randevu tarihi") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = appointmentNote, onValueChange = { appointmentNote = it }, label = { Text("Not") }, modifier = Modifier.fillMaxWidth())
                Button(onClick = { onCreateAppointment(listing, appointmentDate, appointmentNote) }, enabled = appointmentDate.isNotBlank()) {
                    Text("Randevu Oluştur")
                }
            }
        }
    }
}

@Composable
fun MessagesScreen(uiState: GoGoUiState, onOpenThread: (MessageThread) -> Unit, onBack: () -> Unit) {
    val user = uiState.currentUser
    Scaffold(topBar = {
        SmallTopAppBar(title = { Text("Mesajlarım") }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.Outlined.AccountCircle, contentDescription = null) }
        })
    }) { padding ->
        if (user == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Mesajlar için giriş yapın")
            }
        } else {
            val threads = uiState.repoState.threads.filter { it.driverId == user.id || it.senderId == user.id }
            LazyColumn(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(threads) { thread ->
                    val listing = uiState.repoState.listings.firstOrNull { it.id == thread.listingId }
                    Card(onClick = { onOpenThread(thread) }) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(listing?.let { "${it.fromCity} ➜ ${it.toCity}" } ?: "İlan")
                            val unread = thread.unreadBy.contains(user.id)
                            Text(
                                if (unread) "Yeni mesaj" else "Son mesaj ${thread.messages.lastOrNull()?.body ?: "Yok"}",
                                color = if (unread) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ThreadScreen(
    uiState: GoGoUiState,
    threadId: String,
    onSend: (String, String) -> Unit,
    onRead: (String) -> Unit,
    onBack: () -> Unit
) {
    val user = uiState.currentUser
    val thread = uiState.repoState.threads.firstOrNull { it.id == threadId }
    var message by remember { mutableStateOf("") }

    LaunchedEffect(threadId) {
        if (user != null) onRead(threadId)
    }

    Scaffold(topBar = {
        SmallTopAppBar(title = { Text("Sohbet") }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.Outlined.AccountCircle, contentDescription = null) }
        })
    }) { padding ->
        if (thread == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Sohbet bulunamadı") }
            return@Scaffold
        }
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(modifier = Modifier.weight(1f).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(thread.messages) { msg ->
                    val isMine = msg.authorId == user?.id
                    Column(horizontalAlignment = if (isMine) Alignment.End else Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                        Surface(color = if (isMine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface, tonalElevation = 4.dp) {
                            Text(
                                text = msg.body,
                                color = if (isMine) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = message, onValueChange = { message = it }, modifier = Modifier.weight(1f), label = { Text("Mesaj yaz") })
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    if (user != null) {
                        onSend(thread.id, message)
                        message = ""
                    }
                }, enabled = user != null && message.isNotBlank()) {
                    Text("Gönder")
                }
            }
        }
    }
}

@Composable
fun AppointmentScreen(uiState: GoGoUiState, onBack: () -> Unit) {
    val user = uiState.currentUser
    Scaffold(topBar = {
        SmallTopAppBar(title = { Text("Randevularım") }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.Outlined.AccountCircle, contentDescription = null) }
        })
    }) { padding ->
        if (user == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Randevu görmek için giriş yapın")
            }
        } else {
            val appointments = uiState.repoState.appointments.filter {
                it.driverId == user.id || it.senderId == user.id
            }
            LazyColumn(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(appointments) { appointment ->
                    val listing = uiState.repoState.listings.firstOrNull { it.id == appointment.listingId }
                    Card {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(listing?.let { "${it.fromCity} ➜ ${it.toCity}" } ?: "İlan")
                            Text("Planlanan: ${appointment.scheduledFor}")
                            Text("Not: ${appointment.note}")
                        }
                    }
                }
            }
        }
    }
}
