package com.example.totalsecurity


import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// --- CONFIGURACIÓN DE FUENTE ---
val MiFuentePersonalizada = FontFamily(
    Font(R.font.fuente_app, FontWeight.Normal)
)

val AppTypography = Typography(
    headlineMedium = TextStyle(fontFamily = MiFuentePersonalizada, fontWeight = FontWeight.Bold, fontSize = 28.sp),
    titleLarge = TextStyle(fontFamily = MiFuentePersonalizada, fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
    titleMedium = TextStyle(fontFamily = MiFuentePersonalizada, fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    bodyLarge = TextStyle(fontFamily = MiFuentePersonalizada, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = MiFuentePersonalizada, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    labelLarge = TextStyle(fontFamily = MiFuentePersonalizada, fontWeight = FontWeight.Bold, fontSize = 14.sp)
)

// Simulación de persistencia de datos de usuario
var usuarioActual: String? = null
var emailActual: String? = null
var passwordActual: String? = null

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isDarkTheme by remember { mutableStateOf(true) }
            val colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme()

            MaterialTheme(
                colorScheme = colorScheme,
                typography = AppTypography
            ) {
                AppNavigation(
                    isDarkTheme = isDarkTheme,
                    onThemeChange = { isDarkTheme = it }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(isDarkTheme: Boolean, onThemeChange: (Boolean) -> Unit) {
    val navController = rememberNavController()
    val historialAlarmas = remember { mutableStateListOf<String>() }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Rutas sin barras de navegación
    val rutasSinBarras = listOf("splash", "onboarding", "login", "registro", "recuperar")
    val mostrarBarras = currentRoute !in rutasSinBarras

    // Solo mostrar el diálogo de permisos automático si NO estamos en flujo de bienvenida
    if (mostrarBarras) {
        DialogoPermisos()
    }

    Scaffold(
        topBar = {
            if (mostrarBarras) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.logo_app),
                                contentDescription = "Logo",
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Total Security", fontFamily = MiFuentePersonalizada, fontWeight = FontWeight.Bold)
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate("configuracion") }) {
                            Icon(Icons.Default.Settings, contentDescription = "Configuración")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        },
        bottomBar = {
            if (mostrarBarras) {
                BottomNavigationBar(navController, currentRoute)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("splash") { PantallaSplash(navController) }
            composable("onboarding") { PantallaOnboarding(navController) }
            composable("login") { PantallaLogin(navController) }
            composable("registro") { PantallaRegistro(navController) }
            composable("recuperar") { PantallaRecuperar(navController) }
            composable("alarma") { PantallaAlarma(historialAlarmas) }
            composable("historial") { PantallaHistorial(historialAlarmas) }
            composable("perfil") { PantallaPerfil(navController) }
            composable("configuracion") { PantallaConfiguracion(isDarkTheme, onThemeChange) }
        }
    }
}

@Composable
fun PantallaSplash(navController: NavController) {
    LaunchedEffect(key1 = true) {
        delay(2000)
        navController.navigate("onboarding") {
            popUpTo("splash") { inclusive = true }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(id = R.drawable.splash_screen),
            contentDescription = "Splash Screen",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PantallaOnboarding(navController: NavController) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var mostrarVentanaNotificaciones by remember { mutableStateOf(false) }
    var mostrarVentanaUbicacion by remember { mutableStateOf(false) }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) Toast.makeText(context, "Notificaciones activadas", Toast.LENGTH_SHORT).show()
        scope.launch { pagerState.animateScrollToPage(2) }
    }

    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.any { it }) Toast.makeText(context, "Ubicación concedida", Toast.LENGTH_SHORT).show()
        scope.launch { pagerState.animateScrollToPage(3) }
    }

    // Diálogos de sistema (ventanas) explicativos antes del permiso real
    if (mostrarVentanaNotificaciones) {
        AlertDialog(
            onDismissRequest = { mostrarVentanaNotificaciones = false },
            icon = { Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Alertas de Seguridad", fontWeight = FontWeight.Bold) },
            text = { Text("Deseamos enviarte avisos inmediatos cuando se detecte una amenaza cerca de ti. ¿Quieres activar las ventanas de aviso?") },
            confirmButton = {
                Button(onClick = {
                    mostrarVentanaNotificaciones = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        scope.launch { pagerState.animateScrollToPage(2) }
                    }
                }) { Text("Activar") }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarVentanaNotificaciones = false
                    scope.launch { pagerState.animateScrollToPage(2) }
                }) { Text("Ahora no") }
            }
        )
    }

    if (mostrarVentanaUbicacion) {
        AlertDialog(
            onDismissRequest = { mostrarVentanaUbicacion = false },
            icon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Localización en Emergencias", fontWeight = FontWeight.Bold) },
            text = { Text("Para que el sistema de rescate sea efectivo, necesitamos levantar tu posición exacta al usar el botón SOS.") },
            confirmButton = {
                Button(onClick = {
                    mostrarVentanaUbicacion = false
                    locationLauncher.launch(arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ))
                }) { Text("Permitir") }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarVentanaUbicacion = false
                    scope.launch { pagerState.animateScrollToPage(3) }
                }) { Text("Omitir") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            userScrollEnabled = false
        ) { page ->
            when (page) {
                0 -> OnboardingStep(
                    title = "Seguridad Total",
                    description = "Bienvenido a la aplicación líder en protección. Descubre cómo cuidamos de ti paso a paso.",
                    imageRes = R.drawable.logo_app,
                    buttonText = "Comenzar",
                    onButtonClick = { scope.launch { pagerState.animateScrollToPage(1) } }
                )
                1 -> OnboardingStep(
                    title = "Avisos del Sistema",
                    description = "Configura las ventanas de alerta para no perderte ninguna notificación crítica de seguridad.",
                    icon = Icons.Default.Notifications,
                    buttonText = "Configurar Ventanas",
                    onButtonClick = { mostrarVentanaNotificaciones = true }
                )
                2 -> OnboardingStep(
                    title = "Ubicación Protegida",
                    description = "La base de nuestra seguridad es saber dónde estás para enviarte ayuda en segundos si la necesitas.",
                    icon = Icons.Default.LocationOn,
                    buttonText = "Activar Localización",
                    onButtonClick = { mostrarVentanaUbicacion = true }
                )
                3 -> OnboardingStep(
                    title = "Registro Completo",
                    description = "Has configurado los pilares de tu protección. Ya puedes acceder al panel principal de seguridad.",
                    icon = Icons.Default.CheckCircle,
                    buttonText = "Finalizar",
                    onButtonClick = {
                        navController.navigate("login") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    }
                )
            }
        }

        Row(
            modifier = Modifier.height(60.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(4) { iteration ->
                val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else Color.Gray
                Box(modifier = Modifier.padding(4.dp).clip(CircleShape).background(color).size(10.dp))
            }
        }
    }
}

@Composable
fun OnboardingStep(
    title: String,
    description: String,
    imageRes: Int? = null,
    icon: ImageVector? = null,
    buttonText: String,
    onButtonClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (imageRes != null) {
            Image(painter = painterResource(id = imageRes), contentDescription = null, modifier = Modifier.size(160.dp))
        } else if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(160.dp), tint = MaterialTheme.colorScheme.primary)
        }
        
        Spacer(modifier = Modifier.height(40.dp))
        Text(text = title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = description, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.height(64.dp))
        Button(
            onClick = onButtonClick,
            modifier = Modifier.fillMaxWidth().height(55.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(buttonText, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PantallaLogin(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(painter = painterResource(id = R.drawable.logo_app), contentDescription = null, modifier = Modifier.size(100.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text("Iniciar Sesión", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.Email, null) }
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = pass,
            onValueChange = { pass = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            visualTransformation = PasswordVisualTransformation()
        )

        TextButton(onClick = { navController.navigate("recuperar") }, modifier = Modifier.align(Alignment.End)) {
            Text("¿Has olvidado tu contraseña?")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (email == emailActual && pass == passwordActual && email.isNotEmpty()) {
                    navController.navigate("alarma") {
                        popUpTo("login") { inclusive = true }
                    }
                } else {
                    Toast.makeText(context, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(55.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("ENTRAR", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { navController.navigate("registro") },
            modifier = Modifier.fillMaxWidth().height(55.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("REGISTRARSE", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PantallaRegistro(navController: NavController) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Crear Cuenta", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(30.dp))

        OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre Completo") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Person, null) })
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Correo Electrónico") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Email, null) })
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Contraseña") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Lock, null) }, visualTransformation = PasswordVisualTransformation())

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (nombre.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty()) {
                    usuarioActual = nombre
                    emailActual = email
                    passwordActual = pass
                    Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                } else {
                    Toast.makeText(context, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(55.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("GUARDAR Y CONTINUAR", fontWeight = FontWeight.Bold)
        }

        TextButton(onClick = { navController.popBackStack() }) {
            Text("¿Ya tienes cuenta? Inicia sesión")
        }
    }
}

@Composable
fun PantallaRecuperar(navController: NavController) {
    var email by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(24.dp))
        Text("Recuperar Cuenta", style = MaterialTheme.typography.headlineMedium)
        Text("Introduce tu correo para enviarte tu contraseña.", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (email == emailActual && email.isNotEmpty()) {
                    Toast.makeText(context, "Tu contraseña es: $passwordActual", Toast.LENGTH_LONG).show()
                    navController.popBackStack()
                } else {
                    Toast.makeText(context, "El correo no está registrado", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(55.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("RECUPERAR", fontWeight = FontWeight.Bold)
        }

        TextButton(onClick = { navController.popBackStack() }) {
            Text("Volver al Login")
        }
    }
}

@Composable
fun PantallaPerfil(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.AccountCircle, contentDescription = "Perfil", modifier = Modifier.size(100.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        Text(usuarioActual ?: "Usuario Invitado", style = MaterialTheme.typography.headlineMedium)
        Text(emailActual ?: "invitado@mail.com", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth().height(55.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("CERRAR SESIÓN", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DialogoPermisos() {
    var mostrarDialogo by remember { mutableStateOf(true) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        mostrarDialogo = false
    }

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = "Atención") },
            title = { Text("Permisos del Sistema", fontFamily = MiFuentePersonalizada, fontWeight = FontWeight.Bold) },
            text = {
                Text("Esta aplicación requiere permisos de sistema para garantizar tu seguridad (cámara, micrófono y notificaciones).", fontFamily = MiFuentePersonalizada)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val permisosRequeridos = mutableListOf(
                            Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permisosRequeridos.add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        launcher.launch(permisosRequeridos.toTypedArray())
                    }
                ) {
                    Text("Conceder Permisos", fontFamily = MiFuentePersonalizada, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) {
                    Text("Cerrar", fontFamily = MiFuentePersonalizada)
                }
            }
        )
    }
}

@Composable
fun BottomNavigationBar(navController: NavController, currentRoute: String?) {
    val items = listOf(
        Triple("Inicio", Icons.Default.Home, "alarma"),
        Triple("Historial", Icons.AutoMirrored.Filled.List, "historial"),
        Triple("Perfil", Icons.Default.Person, "perfil")
    )

    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
        items.forEach { (titulo, icono, ruta) ->
            NavigationBarItem(
                icon = { Icon(icono, contentDescription = titulo) },
                label = { Text(titulo, fontFamily = MiFuentePersonalizada) },
                selected = currentRoute == ruta,
                onClick = {
                    navController.navigate(ruta) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
fun PantallaAlarma(historial: MutableList<String>) {
    val context = LocalContext.current
    var mostrarConfirmacion by remember { mutableStateOf(false) }

    if (mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacion = false },
            title = { Text("Confirmar Alerta SOS", fontWeight = FontWeight.Bold) },
            text = { Text("¿Estás seguro de que deseas enviar una alerta de emergencia al sistema de seguridad?") },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarConfirmacion = false
                        val horaActual = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
                        historial.add("SOS: $horaActual")
                        Toast.makeText(context, "¡Emergencia notificada!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("SÍ, AVISAR") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacion = false }) { Text("Cancelar") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Panel de Control", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
        Text("Presiona el botón en caso de emergencia.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { mostrarConfirmacion = true },
            modifier = Modifier.width(180.dp).height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Warning, contentDescription = "Alerta", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("BOTÓN SOS", color = Color.White, fontFamily = MiFuentePersonalizada, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(40.dp))
        Text("Actividad Reciente", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(8.dp))

        val recientes = historial.takeLast(3).reversed()
        if (recientes.isEmpty()) {
            Text("No hay alertas recientes.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = MiFuentePersonalizada)
        } else {
            recientes.forEach { aviso -> CardItemAlarma(aviso) }
        }
    }
}

@Composable
fun PantallaHistorial(historial: List<String>) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Historial Completo", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(16.dp))

        if (historial.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("El historial está vacío.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = MiFuentePersonalizada)
            }
        } else {
            LazyColumn {
                items(historial.reversed()) { aviso -> CardItemAlarma(aviso) }
            }
        }
    }
}

@Composable
fun CardItemAlarma(texto: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = texto, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun PantallaConfiguracion(isDarkTheme: Boolean, onThemeChange: (Boolean) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Configuración", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(32.dp))

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (isDarkTheme) Icons.Default.Star else Icons.Default.Face, "Tema", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Modo Oscuro", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                }
                Switch(checked = isDarkTheme, onCheckedChange = onThemeChange)
            }
        }
    }
}
