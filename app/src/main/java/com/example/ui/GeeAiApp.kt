package com.example.ui

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.*
import kotlinx.coroutines.launch

@Composable
fun GeeAiApp(
    viewModel: AppViewModel = viewModel(),
    darkTheme: Boolean = isSystemInDarkTheme(),
    onThemeToggle: () -> Unit = {}
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val activeProject by viewModel.activeProject.collectAsState()
    val activeScenes by viewModel.activeScenes.collectAsState()
    val currentEmail by viewModel.currentEmail.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()

    // Base Scaffold
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (currentScreen == Screen.LOGIN) {
            LoginScreen(viewModel = viewModel)
        } else {
            var isSidebarExpanded by remember { mutableStateOf(false) }

            // SaaS Main Dashboard layout with sidebar navigation
            SaaSLayout(
                viewModel = viewModel,
                userProfile = userProfile,
                currentScreen = currentScreen,
                currentRole = currentRole,
                darkTheme = darkTheme,
                onThemeToggle = onThemeToggle,
                activeProject = activeProject,
                isSidebarExpanded = isSidebarExpanded,
                onSidebarToggle = { isSidebarExpanded = it }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                ) {
                    // Global Brand Header from "Sleek Interface" design
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            IconButton(
                                onClick = { isSidebarExpanded = true },
                                modifier = Modifier.testTag("open_sidebar_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Menu,
                                    contentDescription = "Open Menu",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "GEE AI",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.5).sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Video Generation Suite",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "CREDITS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                                Text(
                                    text = "${userProfile?.creditsRemaining ?: 1240}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            val initials = remember(userProfile?.name) {
                                val name = userProfile?.name ?: "Alex Creator"
                                val parts = name.trim().split(" ")
                                if (parts.size >= 2) {
                                    "${parts[0].firstOrNull() ?: 'A'}${parts[1].firstOrNull() ?: 'C'}".uppercase()
                                } else {
                                    "${name.firstOrNull() ?: 'A'}D".uppercase()
                                }
                            }
                            
                            var showProfileDropdown by remember { mutableStateOf(false) }

                            Box {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
                                        .clickable { showProfileDropdown = true }
                                        .testTag("user_profile_top_icon"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = initials,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }

                                DropdownMenu(
                                    expanded = showProfileDropdown,
                                    onDismissRequest = { showProfileDropdown = false },
                                    modifier = Modifier
                                        .width(300.dp)
                                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                ) {
                                    // User Profile Header Info
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(RoundedCornerShape(50))
                                                    .background(MaterialTheme.colorScheme.primary),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = initials,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimary
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = userProfile?.name ?: "Alex Creator",
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = userProfile?.email ?: "creator@geeai.studio",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                                                Text(
                                                    text = "${userProfile?.subscriptionTier ?: "Pro"} Tier",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Text(
                                                text = "${userProfile?.creditsRemaining ?: 0} Credits Remaining",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                                    // Profile Selection Items (as requested: My Video Projects, AI Presenters, My Media Library)
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text("My Video Projects", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                                Text("Manage drafts, renders & timeline assets", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        },
                                        leadingIcon = { Icon(Icons.Filled.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                        onClick = {
                                            viewModel.setScreen(Screen.MY_PROJECTS)
                                            showProfileDropdown = false
                                        },
                                        modifier = Modifier.testTag("profile_menu_my_projects")
                                    )

                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text("AI Digital Presenters", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                                Text("Custom avatars, lip-sync & persona models", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        },
                                        leadingIcon = { Icon(Icons.Filled.Face, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                        onClick = {
                                            viewModel.setScreen(Screen.AI_CHARACTERS)
                                            showProfileDropdown = false
                                        },
                                        modifier = Modifier.testTag("profile_menu_ai_presenters")
                                    )

                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text("My Media Library", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                                Text("Background music, logos & watermark assets", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        },
                                        leadingIcon = { Icon(Icons.Filled.PermMedia, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                        onClick = {
                                            viewModel.setScreen(Screen.MEDIA_LIBRARY)
                                            showProfileDropdown = false
                                        },
                                        modifier = Modifier.testTag("profile_menu_media_library")
                                    )

                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                                    DropdownMenuItem(
                                        text = { Text("Billing & Subscription", fontSize = 14.sp) },
                                        leadingIcon = { Icon(Icons.Filled.Payment, contentDescription = null) },
                                        onClick = {
                                            viewModel.setScreen(Screen.BILLING)
                                            showProfileDropdown = false
                                        }
                                    )

                                    DropdownMenuItem(
                                        text = { Text("Account Settings", fontSize = 14.sp) },
                                        leadingIcon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                                        onClick = {
                                            viewModel.setScreen(Screen.SETTINGS)
                                            showProfileDropdown = false
                                        }
                                    )

                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                                    DropdownMenuItem(
                                        text = { Text("Sign Out", color = MaterialTheme.colorScheme.error, fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                                        leadingIcon = { Icon(Icons.Filled.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                        onClick = {
                                            viewModel.logout()
                                            showProfileDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Main view area with content transitions
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                            },
                            label = "screen_transition"
                        ) { target ->
                            when (target) {
                                Screen.DASHBOARD -> DashboardPane(viewModel, userProfile)
                                Screen.CREATE_VIDEO -> CreateVideoWizardPane(viewModel)
                                Screen.IMAGE_TO_VIDEO -> ImageToVideoPane(viewModel)
                                Screen.UGC_VIDEO -> UgcVideoPane(viewModel)
                                Screen.AVATAR_VIDEO -> AvatarVideoPane(viewModel)
                                Screen.MY_PROJECTS -> MyProjectsPane(viewModel)
                                Screen.AI_SCRIPT_WRITER -> ScriptWriterPane(viewModel)
                                Screen.AI_VOICE_STUDIO -> VoiceStudioPane(viewModel)
                                Screen.AI_CHARACTERS -> AICharactersPane()
                                Screen.MEDIA_LIBRARY -> MediaLibraryPane(viewModel)
                                Screen.VIDEO_HISTORY -> VideoHistoryPane(viewModel)
                                Screen.BILLING -> BillingPane(viewModel, userProfile)
                                Screen.SETTINGS -> SettingsPane(viewModel, userProfile)
                                Screen.BACKEND_SETTINGS -> BackendSettingsPane(viewModel)
                                Screen.HELP -> HelpPane()
                                Screen.ADMIN_PANEL -> AdminPanelPane(viewModel)
                                Screen.SCENE_BUILDER -> SceneBuilderPane(viewModel, activeProject, activeScenes)
                                Screen.VIDEO_EDITOR -> VideoEditorPane(viewModel, activeProject, activeScenes)
                                Screen.THUMBNAIL_GENERATOR -> ThumbnailGeneratorPane(viewModel, activeProject)
                                Screen.SEO_GENERATOR -> SeoGeneratorPane(viewModel, activeProject)
                                Screen.EXPORT_PREVIEW -> ExportPreviewPane(viewModel, activeProject)
                                else -> DashboardPane(viewModel, userProfile)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// Authentication Login View
// ==========================================

@Composable
fun LoginScreen(viewModel: AppViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf("User") } // Role check toggle
    val loginError by viewModel.loginError.collectAsState()
    val isGoogleSigningIn by viewModel.isGoogleSigningIn.collectAsState()
    val googleSignInError by viewModel.googleSignInError.collectAsState()
    val context = LocalContext.current

    val backgroundBrush = if (isSystemInDarkTheme() || !isSystemInDarkTheme()) {
        Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.background,
                MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.background,
                MaterialTheme.colorScheme.background
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // LOGIN & SIGN IN CONTAINER CARD
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Sleek Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.VideoCall,
                        contentDescription = "Logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "GEE AI",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = (-1).sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

            Text(
                text = "Video Generation Suite",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Text(
                text = if (isSignUp) "Create Your Account" else "Welcome Back",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Role selector simulation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                listOf("User", "Admin").forEach { role ->
                    Button(
                        onClick = { selectedRole = role },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedRole == role) MaterialTheme.colorScheme.primary else Color.Transparent,
                            contentColor = if (selectedRole == role) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("role_select_${role.lowercase()}"),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text(role, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("email_input")
                    .padding(bottom = 12.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("password_input")
                    .padding(bottom = 16.dp)
            )

            if (loginError != null) {
                Text(
                    text = loginError ?: "",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            Button(
                onClick = { viewModel.login(email.ifBlank { "demo_creator@geeai.com" }, selectedRole) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_button")
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isSignUp) "Sign Up" else "Login with Email",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Google Sign In with Credential Manager
            OutlinedButton(
                onClick = {
                    val activity = context as? android.app.Activity
                    if (activity != null) {
                        viewModel.signInWithGoogle(activity)
                    } else {
                        viewModel.login("uzo.creator@gmail.com", "User")
                    }
                },
                enabled = !isGoogleSigningIn,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("google_login_button")
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isGoogleSigningIn) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Signing in with Google...", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = "Google Icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign in with Google", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (googleSignInError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = googleSignInError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (isSignUp) "Already have an account? Sign In" else "Don't have an account? Create one",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                modifier = Modifier
                    .clickable { isSignUp = !isSignUp }
                    .padding(4.dp)
            )
        }
    }
}
}

// ==========================================
// Sidebar and SaaS Navigation Structure
// ==========================================

@Composable
fun SaaSLayout(
    viewModel: AppViewModel,
    userProfile: UserProfile?,
    currentScreen: Screen,
    currentRole: String,
    darkTheme: Boolean,
    onThemeToggle: () -> Unit,
    activeProject: Project?,
    isSidebarExpanded: Boolean,
    onSidebarToggle: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isDesktopWindows = maxWidth >= 840.dp

        if (isDesktopWindows) {
            // Windows PC Desktop Mode: Docked or collapsible sidebar side-by-side with main content
            Row(modifier = Modifier.fillMaxSize()) {
                AnimatedVisibility(
                    visible = isSidebarExpanded,
                    enter = expandHorizontally() + fadeIn(),
                    exit = shrinkHorizontally() + fadeOut()
                ) {
                    SidebarContent(
                        userProfile = userProfile,
                        currentScreen = currentScreen,
                        currentRole = currentRole,
                        darkTheme = darkTheme,
                        onThemeToggle = onThemeToggle,
                        viewModel = viewModel,
                        onSidebarToggle = onSidebarToggle,
                        isDesktopWindows = true
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    content()
                }
            }
        } else {
            // Android Phone Mobile Mode: Fullscreen layout with overlay drawer and dim scrim
            Box(modifier = Modifier.fillMaxSize()) {
                // 1. Main Screen Content
                Box(modifier = Modifier.fillMaxSize()) {
                    content()
                }

                // 2. Dim Scrim overlay when Sidebar is open
                AnimatedVisibility(
                    visible = isSidebarExpanded,
                    enter = fadeIn(animationSpec = tween(250)),
                    exit = fadeOut(animationSpec = tween(250))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f))
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null
                            ) {
                                onSidebarToggle(false)
                            }
                    )
                }

                // 3. Sliding Drawer content
                AnimatedVisibility(
                    visible = isSidebarExpanded,
                    enter = slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = tween(280, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                    ) + fadeIn(),
                    exit = slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(280, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                    ) + fadeOut()
                ) {
                    Row(modifier = Modifier.fillMaxHeight()) {
                        SidebarContent(
                            userProfile = userProfile,
                            currentScreen = currentScreen,
                            currentRole = currentRole,
                            darkTheme = darkTheme,
                            onThemeToggle = onThemeToggle,
                            viewModel = viewModel,
                            onSidebarToggle = onSidebarToggle,
                            isDesktopWindows = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SidebarContent(
    userProfile: UserProfile?,
    currentScreen: Screen,
    currentRole: String,
    darkTheme: Boolean,
    onThemeToggle: () -> Unit,
    viewModel: AppViewModel,
    onSidebarToggle: (Boolean) -> Unit,
    isDesktopWindows: Boolean
) {
    Row(modifier = Modifier.fillMaxHeight()) {
        Column(
            modifier = Modifier
                .width(290.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surface)
                .clickable(enabled = false) {} // Prevent clicking through
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp, horizontal = 16.dp)
        ) {
            // Header Brand Logo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.VideoCall,
                    contentDescription = "Brand Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "GEE AI",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { onSidebarToggle(false) }) {
                    Icon(Icons.Filled.MenuOpen, contentDescription = "Collapse Menu")
                }
            }

            // Profile Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = userProfile?.name ?: "Alex Creator",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Badge(containerColor = MaterialTheme.colorScheme.primary) {
                            Text(
                                userProfile?.subscriptionTier ?: "Pro",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Credits: ${userProfile?.creditsRemaining ?: 0}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Navigation Items
            val menuItems = remember(currentRole) {
                mutableListOf(
                    SidebarMenuItem(Screen.DASHBOARD, "Home Dashboard", Icons.Filled.Dashboard),
                    SidebarMenuItem(Screen.CREATE_VIDEO, "Create AI Video", Icons.Filled.VideoCall),
                    SidebarMenuItem(Screen.IMAGE_TO_VIDEO, "AI Image to Video", Icons.Filled.Image),
                    SidebarMenuItem(Screen.UGC_VIDEO, "UGC Video", Icons.Filled.Campaign),
                    SidebarMenuItem(Screen.AVATAR_VIDEO, "Avatar Video", Icons.Filled.Person),
                    SidebarMenuItem(Screen.AI_VOICE_STUDIO, "AI Voice Studio", Icons.Filled.RecordVoiceOver),
                    SidebarMenuItem(Screen.VIDEO_HISTORY, "Video History", Icons.Filled.History),
                    SidebarMenuItem(Screen.BILLING, "Billing & Active Plan", Icons.Filled.Payment),
                    SidebarMenuItem(Screen.SETTINGS, "Application Settings", Icons.Filled.Settings),
                    SidebarMenuItem(Screen.BACKEND_SETTINGS, "Cloud & API Backend", Icons.Filled.Key),
                    SidebarMenuItem(Screen.HELP, "Help & Support Center", Icons.Filled.Help)
                ).apply {
                    if (currentRole == "Admin") {
                        add(SidebarMenuItem(Screen.ADMIN_PANEL, "Admin Control Board", Icons.Filled.AdminPanelSettings))
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                menuItems.forEach { item ->
                    val isSelected = currentScreen == item.screen ||
                            (item.screen == Screen.CREATE_VIDEO &&
                                     (currentScreen == Screen.SCENE_BUILDER ||
                                             currentScreen == Screen.VIDEO_EDITOR ||
                                             currentScreen == Screen.THUMBNAIL_GENERATOR ||
                                             currentScreen == Screen.SEO_GENERATOR ||
                                             currentScreen == Screen.EXPORT_PREVIEW))

                    NavigationDrawerItem(
                        label = { 
                            Text(
                                text = item.title,
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                letterSpacing = 0.15.sp
                            ) 
                        },
                        selected = isSelected,
                        icon = { 
                            Icon(
                                imageVector = item.icon, 
                                contentDescription = item.title,
                                modifier = Modifier.size(22.dp)
                            ) 
                        },
                        onClick = { 
                            viewModel.setScreen(item.screen) 
                            if (!isDesktopWindows) {
                                onSidebarToggle(false)
                            }
                        },
                        modifier = Modifier
                            .testTag("sidebar_${item.title.lowercase().replace(" ", "_")}")
                            .height(50.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Bottom Sidebar Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onThemeToggle,
                    modifier = Modifier.testTag("theme_toggle")
                ) {
                    Icon(
                        imageVector = if (darkTheme) Icons.Filled.WbSunny else Icons.Filled.NightsStay,
                        contentDescription = "Toggle Theme"
                    )
                }

                TextButton(
                    onClick = { 
                        viewModel.logout()
                        onSidebarToggle(false)
                    },
                    modifier = Modifier.testTag("logout_button")
                ) {
                    Icon(Icons.Filled.Logout, contentDescription = "Logout")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Logout", fontSize = 13.sp)
                }
            }
        }
        VerticalDivider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
            modifier = Modifier.fillMaxHeight().width(1.dp)
        )
    }
}

data class SidebarMenuItem(val screen: Screen, val title: String, val icon: ImageVector)

// ==========================================
// PANE: Dashboard View
// ==========================================

@Composable
fun DashboardPane(viewModel: AppViewModel, userProfile: UserProfile?) {
    val projects by viewModel.allProjects.collectAsState()
    val recentProjects = remember(projects) { projects.take(4) }
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Welcome Header Banner
        item {
            Column {
                Text(
                    text = "Welcome to GEE AI Dashboard",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Create professional YouTube videos in minutes using state-of-the-art AI generation tools.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // SaaS Metrics row
        item {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                if (maxWidth < 650.dp) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCard(
                            title = "Videos Generated",
                            value = "${userProfile?.videosGeneratedCount ?: 0}",
                            subtitle = "+2 this week",
                            icon = Icons.Filled.VideoCall,
                            modifier = Modifier.fillMaxWidth()
                        )
                        MetricCard(
                            title = "AI Credits Remaining",
                            value = "${userProfile?.creditsRemaining ?: 0}",
                            subtitle = "Resets on Jul 25",
                            icon = Icons.Filled.Payment,
                            modifier = Modifier.fillMaxWidth()
                        )
                        MetricCard(
                            title = "Storage Used",
                            value = String.format("%.1f MB", userProfile?.storageUsedMb ?: 120.0),
                            subtitle = "of 10,000 MB limit",
                            icon = Icons.Filled.PermMedia,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        MetricCard(
                            title = "Videos Generated",
                            value = "${userProfile?.videosGeneratedCount ?: 0}",
                            subtitle = "+2 this week",
                            icon = Icons.Filled.VideoCall,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "AI Credits Remaining",
                            value = "${userProfile?.creditsRemaining ?: 0}",
                            subtitle = "Resets on Jul 25",
                            icon = Icons.Filled.Payment,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "Storage Used",
                            value = String.format("%.1f MB", userProfile?.storageUsedMb ?: 120.0),
                            subtitle = "of 10,000 MB limit",
                            icon = Icons.Filled.PermMedia,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Active Quick Create Row (Redesigned into "Generation Console Card")
        item {
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val pulseAlpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulse_alpha"
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    // Engine Pulse Indicator Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha))
                        )
                        Text(
                            text = "AI VIDEO ENGINE ACTIVE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Console Heading
                    Text(
                        text = "What will we create today?",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Translucent Prompt Area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "\"A cinematic tour of the Swiss Alps in 4K with a calming documentary voiceover...\"",
                            fontSize = 14.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Launch Engine Button
                    Button(
                        onClick = { viewModel.setScreen(Screen.CREATE_VIDEO) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onSecondary,
                            contentColor = MaterialTheme.colorScheme.secondary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("quick_create_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Generate Video",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // Section 4: UGC Video Studio
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Campaign,
                                    contentDescription = "UGC Video",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "UGC Video",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "User Generated Content Creator Suite",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Badge(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)) {
                            Text(
                                text = "High Conversion",
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Produce authentic creator-style product reviews, viral TikTok hooks, unboxings, and conversion-optimized promotional videos powered by realistic synthetic actors.",
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("9:16 Vertical Video", "Viral Social Hooks", "Natural Demographics").forEach { tag ->
                            SuggestionChip(
                                onClick = {},
                                label = { Text(tag, fontSize = 11.sp, fontWeight = FontWeight.Medium) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.setScreen(Screen.UGC_VIDEO) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("launch_ugc_video_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Filled.Campaign, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open UGC Video Studio", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }

        // Section 5: Avatar Video Studio
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = "Avatar Video",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Avatar Video",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Photorealistic Digital Presenter Studio",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                            Text(
                                text = "4K Neural Lip-Sync",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Transform presentations and scripts into lifelike avatar broadcasts with frame-accurate lip synchronization, customized studio environments, and multi-language cadence.",
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Elena (Executive)", "Marcus (Tech)", "Sophia (Educator)").forEach { avatarName ->
                            SuggestionChip(
                                onClick = {},
                                label = { Text(avatarName, fontSize = 11.sp, fontWeight = FontWeight.Medium) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.setScreen(Screen.AVATAR_VIDEO) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("launch_avatar_video_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Avatar Video Studio", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }

        // Recent Projects Section
        item {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Projects",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { viewModel.setScreen(Screen.MY_PROJECTS) }) {
                        Text("View All")
                        Icon(Icons.Filled.ArrowForward, contentDescription = "All Projects", modifier = Modifier.size(16.dp))
                    }
                }

                if (recentProjects.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.FolderOpen,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No projects found yet. Tap 'Create Video' to begin!",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        recentProjects.take(2).forEach { project ->
                            ProjectCard(
                                project = project,
                                onEdit = { viewModel.selectProjectForEditing(project) },
                                onDelete = { viewModel.deleteProject(project) },
                                onDuplicate = { viewModel.duplicateProject(project) },
                                onCopyLink = {
                                    viewModel.copyVideoLink(context, project.id, project.name)
                                },
                                onShare = {
                                    viewModel.shareVideoLink(context, project.id, project.name)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Section: Recommended from Firebase Storage
        item {
            val storageItems by viewModel.recommendedStorageItems.collectAsState()
            val context = LocalContext.current

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.CloudDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Column {
                            Text("Recommended from Cloud Storage", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("Curated templates, 4K b-roll, and UGC hooks hosted on your Firebase Storage bucket", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                        Text("Firebase Storage", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(storageItems) { item ->
                        StorageRecommendationCard(
                            item = item,
                            onCopyLink = { viewModel.copyVideoLink(context, item.id, item.title) },
                            onShare = { viewModel.shareVideoLink(context, item.id, item.title) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(title: String, value: String, subtitle: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ==========================================
// PANE: Create Video Wizard
// ==========================================

@Composable
fun CreateVideoWizardPane(viewModel: AppViewModel) {
    var wizardStep by remember { mutableStateOf(1) } // Steps 1, 2, 3

    val topic by viewModel.wizardTopic.collectAsState()
    val promptText by viewModel.wizardPrompt.collectAsState()
    val audience by viewModel.wizardAudience.collectAsState()
    val lengthType by viewModel.wizardLength.collectAsState()
    val language by viewModel.wizardLanguage.collectAsState()
    val tone by viewModel.wizardTone.collectAsState()
    val style by viewModel.wizardStyle.collectAsState()

    val presenter by viewModel.wizardPresenter.collectAsState()
    val voice by viewModel.wizardVoice.collectAsState()
    val accent by viewModel.wizardAccent.collectAsState()
    val music by viewModel.wizardMusic.collectAsState()
    val format by viewModel.wizardFormat.collectAsState()
    val resolution by viewModel.wizardResolution.collectAsState()

    val scriptText by viewModel.wizardScript.collectAsState()
    val isGeneratingScript by viewModel.isGeneratingScript.collectAsState()

    val wizardDurationSeconds by viewModel.wizardDurationSeconds.collectAsState()
    val wizardQualityTier by viewModel.wizardQualityTier.collectAsState()
    val wizardTargetPlatforms by viewModel.wizardTargetPlatforms.collectAsState()
    val showPlatformModal by viewModel.showPlatformConfirmationModal.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    val tokenBreakdown = remember(wizardDurationSeconds, wizardQualityTier) {
        viewModel.calculateTokenDeduction(wizardDurationSeconds, wizardQualityTier)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Step Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Create Video Wizard",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            // Step circles indicators
            Row(verticalAlignment = Alignment.CenterVertically) {
                listOf(1, 2, 3).forEach { s ->
                    val isDone = s < wizardStep
                    val isCurrent = s == wizardStep
                    val circleBg = when {
                        isDone -> MaterialTheme.colorScheme.primary
                        isCurrent -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                    val textColor = if (isDone || isCurrent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(circleBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = s.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textColor)
                    }
                    if (s < 3) {
                        HorizontalDivider(
                            modifier = Modifier.width(32.dp),
                            color = if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                when (wizardStep) {
                    1 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("Step 1: Video Concept & Context", fontSize = 18.sp, fontWeight = FontWeight.Bold)

                            OutlinedTextField(
                                value = topic,
                                onValueChange = { viewModel.wizardTopic.value = it },
                                label = { Text("Video Topic") },
                                placeholder = { Text("e.g. Master React in 10 minutes") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("wizard_topic_input")
                            )

                            OutlinedTextField(
                                value = promptText,
                                onValueChange = { viewModel.wizardPrompt.value = it },
                                label = { Text("Detailed Prompt for AI Script") },
                                placeholder = { Text("Describe what should happen, key points to hit, visual ideas...") },
                                minLines = 3,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("wizard_prompt_input")
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                OutlinedTextField(
                                    value = audience,
                                    onValueChange = { viewModel.wizardAudience.value = it },
                                    label = { Text("Target Audience") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                // Manual video duration selector (up to 10 seconds only)
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Manual Length (Max 10s)",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                                Text(
                                                    text = "${wizardDurationSeconds}s",
                                                    color = MaterialTheme.colorScheme.onPrimary,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp)
                                                )
                                            }
                                        }

                                        Slider(
                                            value = wizardDurationSeconds.toFloat(),
                                            onValueChange = { viewModel.wizardDurationSeconds.value = it.toInt() },
                                            valueRange = 1f..10f,
                                            steps = 8,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("video_length_slider")
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            listOf(3, 5, 8, 10).forEach { s ->
                                                FilterChip(
                                                    selected = wizardDurationSeconds == s,
                                                    onClick = { viewModel.wizardDurationSeconds.value = s },
                                                    label = { Text("${s}s", fontSize = 11.sp) },
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                                        selectedLabelColor = MaterialTheme.colorScheme.primary
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                OutlinedTextField(
                                    value = language,
                                    onValueChange = { viewModel.wizardLanguage.value = it },
                                    label = { Text("Language") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = tone,
                                    onValueChange = { viewModel.wizardTone.value = it },
                                    label = { Text("Tone") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = style,
                                    onValueChange = { viewModel.wizardStyle.value = it },
                                    label = { Text("Visual Style Preset") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    2 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("Step 2: AI Avatar & Production Assets", fontSize = 18.sp, fontWeight = FontWeight.Bold)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                OutlinedTextField(
                                    value = presenter,
                                    onValueChange = { viewModel.wizardPresenter.value = it },
                                    label = { Text("AI Presenter Avatar") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = voice,
                                    onValueChange = { viewModel.wizardVoice.value = it },
                                    label = { Text("AI Voice Accent/Character") },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                OutlinedTextField(
                                    value = accent,
                                    onValueChange = { viewModel.wizardAccent.value = it },
                                    label = { Text("Language Accent") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = music,
                                    onValueChange = { viewModel.wizardMusic.value = it },
                                    label = { Text("Background Music") },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Format Dropdown
                                Column(modifier = Modifier.weight(1f)) {
                                    var formatExpanded by remember { mutableStateOf(false) }
                                    Box {
                                        OutlinedTextField(
                                            value = format,
                                            onValueChange = {},
                                            label = { Text("Video Aspect Ratio") },
                                            readOnly = true,
                                            trailingIcon = { IconButton(onClick = { formatExpanded = true }) { Icon(Icons.Filled.ArrowDropDown, null) } },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        DropdownMenu(expanded = formatExpanded, onDismissRequest = { formatExpanded = false }) {
                                            listOf("16:9 (Landscape)", "9:16 (Shorts/Vertical)", "1:1 (Square)").forEach { f ->
                                                DropdownMenuItem(
                                                    text = { Text(f) },
                                                    onClick = {
                                                        viewModel.wizardFormat.value = f.split(" ").first()
                                                        formatExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                // Video Quality Dropdown (p to Ultra HD)
                                Column(modifier = Modifier.weight(1f)) {
                                    var resExpanded by remember { mutableStateOf(false) }
                                    Box {
                                        OutlinedTextField(
                                            value = wizardQualityTier,
                                            onValueChange = {},
                                            label = { Text("Video Quality (SD to Ultra HD)") },
                                            readOnly = true,
                                            trailingIcon = { IconButton(onClick = { resExpanded = true }) { Icon(Icons.Filled.ArrowDropDown, null) } },
                                            modifier = Modifier.fillMaxWidth().testTag("video_quality_selector")
                                        )
                                        DropdownMenu(expanded = resExpanded, onDismissRequest = { resExpanded = false }) {
                                            listOf(
                                                "480p (SD Standard)",
                                                "720p (HD Standard)",
                                                "1080p (Full HD)",
                                                "1440p (2K Quad HD)",
                                                "4K (Ultra HD)"
                                            ).forEach { r ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(r, fontWeight = if (wizardQualityTier == r) FontWeight.Bold else FontWeight.Normal)
                                                            val tokens = viewModel.calculateTokenDeduction(wizardDurationSeconds, r).totalTokens
                                                            Text("~$tokens tokens", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                                        }
                                                    },
                                                    onClick = {
                                                        viewModel.wizardQualityTier.value = r
                                                        viewModel.wizardResolution.value = r.split(" ").first()
                                                        resExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Dynamic Claude & Firebase Token Deduction Estimator Card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("token_deduction_card"),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Token,
                                                contentDescription = "Tokens",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = "Token & Credit Deduction Estimator",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                            Text(
                                                text = "${tokenBreakdown.totalTokens} Tokens",
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "• Claude AI Script & Prompt Tokens:",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${tokenBreakdown.scriptClaudeTokens} Tokens",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "• Firebase Video Render Compute (${tokenBreakdown.durationSeconds}s @ ${tokenBreakdown.quality}):",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${tokenBreakdown.renderComputeTokens} Tokens",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "• Account Credits Remaining After Generation:",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        val curBalance = userProfile?.creditsRemaining ?: 1000
                                        Text(
                                            text = "${maxOf(0, curBalance - tokenBreakdown.totalTokens)} Credits",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    3 -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Step 3: Edit AI Script", fontSize = 18.sp, fontWeight = FontWeight.Bold)

                            if (isGeneratingScript) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CircularProgressIndicator()
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text("AI is writing your long-form YouTube script...", style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            } else {
                                OutlinedTextField(
                                    value = scriptText,
                                    onValueChange = { viewModel.wizardScript.value = it },
                                    label = { Text("Complete Script Narrative (Editable)") },
                                    minLines = 8,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("wizard_script_field")
                                )

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Info,
                                            contentDescription = "Credits Info",
                                            tint = MaterialTheme.colorScheme.secondary
                                        )
                                        Text(
                                            text = "Note: Building the project will divide the script into visual scenes. Visual generation for each scene costs 60 credits.",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }

                                Button(
                                    onClick = { viewModel.generateScript() },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    modifier = Modifier.testTag("rewrite_script_button")
                                ) {
                                    Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Re-write Script with AI")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (wizardStep > 1) {
                OutlinedButton(onClick = { wizardStep-- }) {
                    Text("Back")
                }
            } else {
                Spacer(modifier = Modifier.width(10.dp))
            }

            Button(
                onClick = {
                    if (wizardStep < 3) {
                        if (wizardStep == 1 && scriptText.isBlank()) {
                            viewModel.generateScript()
                        }
                        wizardStep++
                    } else {
                        // Prompt user to select target platforms and review token deduction
                        viewModel.requestCreateVideoConfirmation()
                    }
                },
                modifier = Modifier.testTag("wizard_next_button")
            ) {
                Text(if (wizardStep == 3) "Create Video (Choose Platforms)" else "Continue")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Filled.ArrowForward, contentDescription = null)
            }
        }

        // Before video generation: Ask user about 7 platforms and show token deduction
        if (showPlatformModal) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissPlatformConfirmation() },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Platforms",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                },
                title = {
                    Text(
                        text = "Select Target Platforms (7 Platforms)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Choose which social platforms you plan to post this video on so GEE AI can format aspect ratios, framerates, and metadata:",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                viewModel.allSevenPlatforms.forEach { platform ->
                                    val isChecked = wizardTargetPlatforms.contains(platform)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { viewModel.toggleTargetPlatform(platform) }
                                            .padding(vertical = 4.dp, horizontal = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Checkbox(
                                                checked = isChecked,
                                                onCheckedChange = { viewModel.toggleTargetPlatform(platform) },
                                                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                                            )
                                            Text(
                                                text = platform,
                                                fontSize = 14.sp,
                                                fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        if (isChecked) {
                                            Badge(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)) {
                                                Text(
                                                    "Selected",
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Summary of Tokens to Deduct
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Generation & Token Breakdown:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "• Length: ${wizardDurationSeconds}s (Manual selection, max 10s)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "• Quality: $wizardQualityTier",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "• Deducting: ${tokenBreakdown.totalTokens} Tokens (${tokenBreakdown.scriptClaudeTokens} Claude + ${tokenBreakdown.renderComputeTokens} Render)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                val remaining = maxOf(0, (userProfile?.creditsRemaining ?: 1000) - tokenBreakdown.totalTokens)
                                Text(
                                    text = "• Credits remaining after generation: $remaining",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.buildScenesAndProject() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("confirm_create_video_button")
                    ) {
                        Text("Confirm & Create Video")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissPlatformConfirmation() }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

// ==========================================
// PANE: Image to Video Pane
// ==========================================

@Composable
fun ImageToVideoPane(viewModel: AppViewModel) {
    val imgUri by viewModel.imgToVideoImageUri.collectAsState()
    val motionType by viewModel.imgToVideoMotion.collectAsState()
    val duration by viewModel.imgToVideoDuration.collectAsState()
    val isGenerating by viewModel.isGeneratingImageToVideo.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    // Android Photo Picker Launcher for uploading device photos
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.imgToVideoImageUri.value = uri.toString()
        }
    }

    val sampleImages = listOf(
        "https://images.unsplash.com/photo-1626544827763-d516dce335e2?q=80&w=600&auto=format&fit=crop" to "Cyberpunk City",
        "https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?q=80&w=600&auto=format&fit=crop" to "Luxury Cosmetic",
        "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?q=80&w=600&auto=format&fit=crop" to "Tropical Beach",
        "https://images.unsplash.com/photo-1503376780353-7e6692767b70?q=80&w=600&auto=format&fit=crop" to "Supercar Motion"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("AI Image-to-Video Engine", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "Upload static imagery and apply neural camera motions (zoom, orbit, pan) to generate high-retention video clips.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                Text(
                    text = "25 Credits / Clip",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Settings Column
                Column(
                    modifier = Modifier
                        .weight(1.1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Image Source & Upload", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)

                    // Primary Upload Button
                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("upload_image_to_video_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Filled.AddPhotoAlternate, contentDescription = "Upload Photo", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Upload Image from Device Gallery", fontWeight = FontWeight.Bold)
                    }

                    // Preset Quick-Pick Images
                    Text("Or Select Sample Inspiration:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        sampleImages.forEach { (url, label) ->
                            FilterChip(
                                selected = imgUri == url,
                                onClick = { viewModel.imgToVideoImageUri.value = url },
                                label = { Text(label, fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(14.dp))
                                }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = imgUri,
                        onValueChange = { viewModel.imgToVideoImageUri.value = it },
                        label = { Text("Image URL or Uploaded Path") },
                        placeholder = { Text("Selected image URI or custom link...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Camera Motion Style", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Slow Zoom In", "Pan Right", "Tilt Up", "Orbit Rotation", "Hyper-lapse Push").forEach { motion ->
                            FilterChip(
                                selected = motionType == motion,
                                onClick = { viewModel.imgToVideoMotion.value = motion },
                                label = { Text(motion) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = duration,
                        onValueChange = { viewModel.imgToVideoDuration.value = it },
                        label = { Text("Output Clip Duration (Capped at 10s max)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Monetization & Credit Deduction Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Filled.MonetizationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    Text("Credit Deduction & Profit Margin", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Text("-25 Credits", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("• Retail Value ($0.10/credit):", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$2.50 USD", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("• Estimated Compute Cost:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("~$0.08 USD", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("• Your SaaS Profit Margin:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text("96.8% Gross Margin", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            val currentCredits = userProfile?.creditsRemaining ?: 1000
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("• Balance after generation:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${maxOf(0, currentCredits - 25)} Credits", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Button(
                        onClick = { viewModel.generateImageToVideo() },
                        enabled = !isGenerating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("generate_img_to_video"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Synthesizing Video Frame Motion...")
                        } else {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate Image To Video (Deduct 25 Credits)", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Preview Column
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (imgUri.isNotBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imgUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Source Image Preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                    )
                                ),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Motion: $motionType ($duration)",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Ready to synthesize into fluid video",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                            Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No Source Image Selected", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Upload a photo from device or pick a preset to preview neural animation.",
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// PANE: Section 4 - UGC Video Studio
// ==========================================

@Composable
fun UgcVideoPane(viewModel: AppViewModel) {
    val productName by viewModel.ugcProductName.collectAsState()
    val style by viewModel.ugcStyle.collectAsState()
    val platform by viewModel.ugcPlatform.collectAsState()
    val persona by viewModel.ugcCreatorPersona.collectAsState()
    val features by viewModel.ugcKeyFeatures.collectAsState()
    val hook by viewModel.ugcHookStyle.collectAsState()
    val cta by viewModel.ugcCallToAction.collectAsState()
    val isGenerating by viewModel.isGeneratingUgc.collectAsState()
    val script by viewModel.ugcGeneratedScript.collectAsState()
    val renderedUrl by viewModel.ugcRenderedVideoUrl.collectAsState()

    var isPlayingPreview by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "UGC Video Generator",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                        Text(
                            text = "Section 4",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "Generate authentic creator-led marketing videos, reviews, and unboxings tailored for TikTok, Reels, and Shorts.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val isCompact = maxWidth < 850.dp

                if (isCompact) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        UgcConfigSection(
                            viewModel = viewModel,
                            productName = productName,
                            style = style,
                            platform = platform,
                            persona = persona,
                            features = features,
                            hook = hook,
                            cta = cta,
                            isGenerating = isGenerating
                        )

                        UgcPreviewSection(
                            isGenerating = isGenerating,
                            renderedUrl = renderedUrl,
                            script = script,
                            isPlayingPreview = isPlayingPreview,
                            onTogglePlay = { isPlayingPreview = !isPlayingPreview },
                            persona = persona,
                            style = style,
                            onOpenEditor = { viewModel.setScreen(Screen.VIDEO_EDITOR) }
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1.1f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState())
                        ) {
                            UgcConfigSection(
                                viewModel = viewModel,
                                productName = productName,
                                style = style,
                                platform = platform,
                                persona = persona,
                                features = features,
                                hook = hook,
                                cta = cta,
                                isGenerating = isGenerating
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(0.9f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState())
                        ) {
                            UgcPreviewSection(
                                isGenerating = isGenerating,
                                renderedUrl = renderedUrl,
                                script = script,
                                isPlayingPreview = isPlayingPreview,
                                onTogglePlay = { isPlayingPreview = !isPlayingPreview },
                                persona = persona,
                                style = style,
                                onOpenEditor = { viewModel.setScreen(Screen.VIDEO_EDITOR) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UgcConfigSection(
    viewModel: AppViewModel,
    productName: String,
    style: String,
    platform: String,
    persona: String,
    features: String,
    hook: String,
    cta: String,
    isGenerating: Boolean
) {
    val productImageUri by viewModel.ugcProductImageUri.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    val ugcPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.ugcProductImageUri.value = uri.toString()
        }
    }

    val productPresets = listOf(
        "https://images.unsplash.com/photo-1620916566398-39f1143ab7be?q=80&w=600&auto=format&fit=crop" to "Botanical Face Oil",
        "https://images.unsplash.com/photo-1590658268037-6bf12165a8df?q=80&w=600&auto=format&fit=crop" to "Wireless ANC Pods",
        "https://images.unsplash.com/photo-1605100804763-247f67b3557e?q=80&w=600&auto=format&fit=crop" to "Smart Titanium Ring",
        "https://images.unsplash.com/photo-1556881286-fc6915169721?q=80&w=600&auto=format&fit=crop" to "Organic Energy Drink"
    )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Campaign & Product Configuration", fontSize = 18.sp, fontWeight = FontWeight.Bold)

        // Upload Product Image section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Product Image / Packaging Asset", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    if (productImageUri.isNotBlank()) {
                        Badge(containerColor = MaterialTheme.colorScheme.primary) {
                            Text("Image Attached", color = MaterialTheme.colorScheme.onPrimary, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 4.dp))
                        }
                    }
                }

                // Button for user to upload image inside UGC video
                Button(
                    onClick = {
                        ugcPhotoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("upload_ugc_product_image_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Filled.AddPhotoAlternate, contentDescription = "Upload", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Upload Product / Brand Image", fontWeight = FontWeight.Bold)
                }

                Text("Or Choose Sample Product Photo:", style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    productPresets.forEach { (url, label) ->
                        FilterChip(
                            selected = productImageUri == url,
                            onClick = {
                                viewModel.ugcProductImageUri.value = url
                                if (productName.isBlank() || productName.contains("GlowSkin")) {
                                    viewModel.ugcProductName.value = label
                                }
                            },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                if (productImageUri.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(productImageUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Product preview",
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Selected Product Asset Active", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("AI UGC creator will showcase this item in review frames.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { viewModel.ugcProductImageUri.value = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        OutlinedTextField(
            value = productName,
            onValueChange = { viewModel.ugcProductName.value = it },
            label = { Text("Product or Brand Name") },
            placeholder = { Text("e.g. Lumina Glow Botanical Face Oil") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ugc_product_name_input")
        )

        Text("UGC Video Format Style", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "Customer Honest Review",
                "Product Unboxing",
                "Problem & Solution",
                "Viral TikTok Trend",
                "30-Day Before & After"
            ).forEach { item ->
                FilterChip(
                    selected = style == item,
                    onClick = { viewModel.ugcStyle.value = item },
                    label = { Text(item, fontSize = 12.sp) }
                )
            }
        }

        Text("Target Social Platform & Aspect Ratio", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "TikTok / Reels (9:16)",
                "YouTube Shorts (9:16)",
                "Instagram Feed (1:1)",
                "YouTube Full (16:9)"
            ).forEach { item ->
                FilterChip(
                    selected = platform == item,
                    onClick = { viewModel.ugcPlatform.value = item },
                    label = { Text(item, fontSize = 12.sp) }
                )
            }
        }

        Text("AI Creator Persona & Demographic", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "Skincare & Beauty Enthusiast",
                "Tech Gadget Reviewer",
                "Fitness & Wellness Coach",
                "Lifestyle Influencer",
                "Gen-Z Trendsetter"
            ).forEach { item ->
                FilterChip(
                    selected = persona == item,
                    onClick = { viewModel.ugcCreatorPersona.value = item },
                    label = { Text(item, fontSize = 12.sp) }
                )
            }
        }

        OutlinedTextField(
            value = hook,
            onValueChange = { viewModel.ugcHookStyle.value = it },
            label = { Text("Opening 3-Second Viral Hook") },
            placeholder = { Text("e.g. Stop scrolling if you have stubborn dry skin...") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = features,
            onValueChange = { viewModel.ugcKeyFeatures.value = it },
            label = { Text("Key Value Props & Benefits (Comma separated)") },
            placeholder = { Text("e.g. 100% organic, 24hr hydration, non-sticky feel") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        OutlinedTextField(
            value = cta,
            onValueChange = { viewModel.ugcCallToAction.value = it },
            label = { Text("Call To Action (CTA)") },
            placeholder = { Text("e.g. Tap below for 25% off + free delivery!") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Monetization & Credit Deduction Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Filled.MonetizationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Text("UGC Credit Deduction & Margins", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Text("-50 Credits", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("• Retail Package Value ($0.10/credit):", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$5.00 USD", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("• Estimated Neural Compute Cost:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("~$0.14 USD", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("• Your SaaS Profit Margin:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("97.2% Gross Profit", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                val currentCredits = userProfile?.creditsRemaining ?: 1000
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("• Account Balance after generation:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${maxOf(0, currentCredits - 50)} Credits", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Button(
            onClick = { viewModel.generateUgcVideo() },
            enabled = !isGenerating,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("generate_ugc_video_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            if (isGenerating) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Synthesizing UGC Actor & Viral Hook (50 Credits)...")
            } else {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate UGC Video (Deduct 50 Credits)", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun UgcPreviewSection(
    isGenerating: Boolean,
    renderedUrl: String?,
    script: String,
    isPlayingPreview: Boolean,
    onTogglePlay: () -> Unit,
    persona: String,
    style: String,
    onOpenEditor: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Live Production Preview", fontSize = 18.sp, fontWeight = FontWeight.Bold)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (isGenerating) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(modifier = Modifier.size(48.dp), color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("Rendering authentic creator frames...", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("Synchronizing conversational natural speech...", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else if (renderedUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(renderedUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "UGC Render Result",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.25f))
                    )

                    IconButton(
                        onClick = onTogglePlay,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.9f))
                    ) {
                        Icon(
                            imageVector = if (isPlayingPreview) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = "Play Preview",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        contentAlignment = Alignment.TopStart
                    ) {
                        Badge(containerColor = MaterialTheme.colorScheme.primary) {
                            Text("UGC Verified • 9:16", color = MaterialTheme.colorScheme.onPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                        }
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(20.dp)) {
                        Icon(Icons.Filled.Campaign, contentDescription = null, modifier = Modifier.size(60.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("No UGC video rendered yet.", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Configure options on the left and tap Generate.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    }
                }
            }
        }

        if (script.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Synthesized Script Breakdown", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Persona: $persona", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                    Text(
                        text = script,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            OutlinedButton(
                onClick = onOpenEditor,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Open in Full Video Editor Suite")
            }
        }
    }
}

// ==========================================
// PANE: Section 5 - Avatar Video Studio
// ==========================================

@Composable
fun AvatarVideoPane(viewModel: AppViewModel) {
    val avatarName by viewModel.avatarName.collectAsState()
    val framing by viewModel.avatarFraming.collectAsState()
    val emotion by viewModel.avatarEmotion.collectAsState()
    val backdrop by viewModel.avatarBackdrop.collectAsState()
    val script by viewModel.avatarScript.collectAsState()
    val voice by viewModel.avatarVoice.collectAsState()
    val gestures by viewModel.avatarGestures.collectAsState()
    val isGenerating by viewModel.isGeneratingAvatar.collectAsState()
    val renderedUrl by viewModel.avatarRenderedVideoUrl.collectAsState()

    var isPlayingPreview by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Avatar Video Studio",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Badge(containerColor = MaterialTheme.colorScheme.tertiaryContainer) {
                        Text(
                            text = "Section 5",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "Turn presentations and corporate briefs into photorealistic talking avatar broadcasts with neural lip-sync.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val isCompact = maxWidth < 850.dp

                if (isCompact) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        AvatarConfigSection(
                            viewModel = viewModel,
                            avatarName = avatarName,
                            framing = framing,
                            emotion = emotion,
                            backdrop = backdrop,
                            script = script,
                            voice = voice,
                            gestures = gestures,
                            isGenerating = isGenerating
                        )

                        AvatarPreviewSection(
                            isGenerating = isGenerating,
                            renderedUrl = renderedUrl,
                            script = script,
                            isPlayingPreview = isPlayingPreview,
                            onTogglePlay = { isPlayingPreview = !isPlayingPreview },
                            avatarName = avatarName,
                            backdrop = backdrop,
                            onOpenEditor = { viewModel.setScreen(Screen.VIDEO_EDITOR) }
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1.1f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState())
                        ) {
                            AvatarConfigSection(
                                viewModel = viewModel,
                                avatarName = avatarName,
                                framing = framing,
                                emotion = emotion,
                                backdrop = backdrop,
                                script = script,
                                voice = voice,
                                gestures = gestures,
                                isGenerating = isGenerating
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(0.9f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState())
                        ) {
                            AvatarPreviewSection(
                                isGenerating = isGenerating,
                                renderedUrl = renderedUrl,
                                script = script,
                                isPlayingPreview = isPlayingPreview,
                                onTogglePlay = { isPlayingPreview = !isPlayingPreview },
                                avatarName = avatarName,
                                backdrop = backdrop,
                                onOpenEditor = { viewModel.setScreen(Screen.VIDEO_EDITOR) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AvatarConfigSection(
    viewModel: AppViewModel,
    avatarName: String,
    framing: String,
    emotion: String,
    backdrop: String,
    script: String,
    voice: String,
    gestures: String,
    isGenerating: Boolean
) {
    val availableAvatars by viewModel.availableAvatars.collectAsState()
    val selectedAvatarId by viewModel.selectedAvatarId.collectAsState()
    val customAvatarUri by viewModel.avatarCustomImageUri.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    val avatarPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.setCustomAvatarImage(uri.toString())
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Digital Presenter & Face Model Selection", fontSize = 18.sp, fontWeight = FontWeight.Bold)

        // Custom Photo Upload Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Upload Photo for Custom Avatar Clone", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Badge(containerColor = MaterialTheme.colorScheme.secondary) {
                        Text("Neural Cloning", color = MaterialTheme.colorScheme.onSecondary, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 4.dp))
                    }
                }

                Button(
                    onClick = {
                        avatarPhotoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("upload_avatar_photo_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Filled.AddPhotoAlternate, contentDescription = "Upload Face", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Upload Face Image to Use as Avatar", fontWeight = FontWeight.Bold)
                }

                if (customAvatarUri.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(customAvatarUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Custom face preview",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(24.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Custom Face Cloned & Ready", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Neural lip-sync will animate your uploaded face photo.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Multiple Avatars Catalog (Scrollable Visual Cards)
        Text("Choose from Multiple AI Avatars (${availableAvatars.size} Available):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(availableAvatars) { avatar ->
                val isSelected = selectedAvatarId == avatar.id
                Card(
                    modifier = Modifier
                        .width(140.dp)
                        .clickable { viewModel.selectAvatar(avatar) }
                        .testTag("avatar_card_${avatar.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(avatar.imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = avatar.name,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(32.dp))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(32.dp)
                                ),
                            contentScale = ContentScale.Crop
                        )
                        Text(
                            text = avatar.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                        Badge(containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant) {
                            Text(
                                text = avatar.category,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                        Text(
                            text = avatar.role,
                            fontSize = 10.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Text("Camera Shot & Framing", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "Studio Close-up",
                "Desk Presentation",
                "Modern Newsroom",
                "Casual Podcast",
                "Virtual Stage"
            ).forEach { item ->
                FilterChip(
                    selected = framing == item,
                    onClick = { viewModel.avatarFraming.value = item },
                    label = { Text(item, fontSize = 12.sp) }
                )
            }
        }

        Text("Studio Backdrop & Environment", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "Modern Minimalist Office",
                "Gradient Studio Glow",
                "Tech Innovation Lab",
                "Green Screen (Transparent)",
                "Podcast Lounge"
            ).forEach { item ->
                FilterChip(
                    selected = backdrop == item,
                    onClick = { viewModel.avatarBackdrop.value = item },
                    label = { Text(item, fontSize = 12.sp) }
                )
            }
        }

        Text("Emotional Delivery & Tone", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "Warm & Authoritative",
                "Enthusiastic & High Energy",
                "Empathetic & Calming",
                "Confident & Professional"
            ).forEach { item ->
                FilterChip(
                    selected = emotion == item,
                    onClick = { viewModel.avatarEmotion.value = item },
                    label = { Text(item, fontSize = 12.sp) }
                )
            }
        }

        Text("Voice Cadence & Accent", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "Bella - Warm Professional (US)",
                "James - Crisp British (UK)",
                "Amara - Expressive African (NG)",
                "Kai - Smooth Australian (AU)"
            ).forEach { item ->
                FilterChip(
                    selected = voice == item,
                    onClick = { viewModel.avatarVoice.value = item },
                    label = { Text(item, fontSize = 12.sp) }
                )
            }
        }

        OutlinedTextField(
            value = script,
            onValueChange = { viewModel.avatarScript.value = it },
            label = { Text("Speech Narration & Teleprompter Text") },
            placeholder = { Text("Type or paste speech for the avatar to articulate...") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("avatar_script_input"),
            minLines = 4
        )

        // Monetization & Credit Deduction Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Filled.MonetizationOn, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                        Text("Avatar Credit Deduction & Economics", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Text("-75 Credits", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary, fontSize = 13.sp)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("• Retail User Charge ($0.10/credit):", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$7.50 USD", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("• Estimated Neural Lip-Sync Compute Cost:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("~$0.18 USD", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("• Platform Owner Profit Margin:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    Text("97.6% Gross Profit", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                }
                val currentCredits = userProfile?.creditsRemaining ?: 1000
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("• Account Balance after generation:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${maxOf(0, currentCredits - 75)} Credits", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Button(
            onClick = { viewModel.generateAvatarVideo() },
            enabled = !isGenerating,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("generate_avatar_video_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            if (isGenerating) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onSecondary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Rendering 4K Neural Lip-Sync (75 Credits)...")
            } else {
                Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate Avatar Video (Deduct 75 Credits)", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun AvatarPreviewSection(
    isGenerating: Boolean,
    renderedUrl: String?,
    script: String,
    isPlayingPreview: Boolean,
    onTogglePlay: () -> Unit,
    avatarName: String,
    backdrop: String,
    onOpenEditor: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Studio Broadcast Viewport", fontSize = 18.sp, fontWeight = FontWeight.Bold)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (isGenerating) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(modifier = Modifier.size(48.dp), color = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("Synthesizing neural facial expressions...", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("Rendering 60fps micro-movements & phonemes...", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else if (renderedUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(renderedUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Avatar Render Result",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.2f))
                    )

                    IconButton(
                        onClick = onTogglePlay,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f))
                    ) {
                        Icon(
                            imageVector = if (isPlayingPreview) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = "Play Avatar Video",
                            tint = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        contentAlignment = Alignment.TopStart
                    ) {
                        Badge(containerColor = MaterialTheme.colorScheme.secondary) {
                            Text("4K Neural Studio • $avatarName", color = MaterialTheme.colorScheme.onSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                        }
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(20.dp)) {
                        Icon(Icons.Filled.Face, contentDescription = null, modifier = Modifier.size(60.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("No avatar video synthesized yet.", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Select your presenter, configure backdrop, and tap Generate.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Teleprompter & Backstage Info", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(backdrop, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.SemiBold)
                }
                Text(
                    text = script.ifBlank { "No script entered." },
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        OutlinedButton(
            onClick = onOpenEditor,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Filled.MovieCreation, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Open in Timeline Studio Editor")
        }
    }
}

// ==========================================
// PANE: Scene Builder pane
// ==========================================

@Composable
fun SceneBuilderPane(viewModel: AppViewModel, activeProject: Project?, activeScenes: List<Scene>) {
    var editingScene by remember { mutableStateOf<Scene?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Scene Builder - ${activeProject?.name}", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "Your script is divided into separate visual scenes. Generate, edit or customize narration on a scene-by-scene level.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = { viewModel.setScreen(Screen.VIDEO_EDITOR) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Go to Timeline Editor")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Filled.Timeline, contentDescription = null)
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            // Left Scene list
            LazyColumn(
                modifier = Modifier
                    .weight(1.5f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Button(
                        onClick = { viewModel.generateAllVisuals() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("generate_all_scenes_btn")
                    ) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate Visuals for All Scenes (60 Credits per scene)")
                    }
                }

                items(activeScenes) { scene ->
                    SceneConfigCard(
                        scene = scene,
                        onEdit = { editingScene = scene },
                        onDelete = { viewModel.deleteScene(scene) },
                        onGenerateSingle = { viewModel.generateSingleVisual(scene) }
                    )
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Right Scene Edit panel
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            ) {
                if (editingScene != null) {
                    val scene = editingScene!!
                    var narration by remember(scene.id) { mutableStateOf(scene.narration) }
                    var promptVal by remember(scene.id) { mutableStateOf(scene.visualPrompt) }
                    var durationSec by remember(scene.id) { mutableStateOf(scene.durationSeconds.toString()) }
                    var transitionVal by remember(scene.id) { mutableStateOf(scene.transition) }
                    var cameraDir by remember(scene.id) { mutableStateOf(scene.cameraDirection) }
                    var captions by remember(scene.id) { mutableStateOf(scene.captionText) }

                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Edit Scene #${scene.sceneIndex + 1}", fontSize = 18.sp, fontWeight = FontWeight.Bold)

                        OutlinedTextField(
                            value = narration,
                            onValueChange = { narration = it },
                            label = { Text("Narration / Script Voiceover") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = promptVal,
                            onValueChange = { promptVal = it },
                            label = { Text("Visual Prompt for Image AI") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = durationSec,
                                onValueChange = { durationSec = it },
                                label = { Text("Duration (sec)") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = transitionVal,
                                onValueChange = { transitionVal = it },
                                label = { Text("Transition") },
                                modifier = Modifier.weight(1.5f)
                            )
                        }

                        OutlinedTextField(
                            value = cameraDir,
                            onValueChange = { cameraDir = it },
                            label = { Text("Camera Direction") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = captions,
                            onValueChange = { captions = it },
                            label = { Text("Caption Text") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                val updated = scene.copy(
                                    narration = narration,
                                    visualPrompt = promptVal,
                                    durationSeconds = durationSec.toIntOrNull() ?: 10,
                                    transition = transitionVal,
                                    cameraDirection = cameraDir,
                                    captionText = captions
                                )
                                viewModel.updateSceneDetail(updated)
                                editingScene = null
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_scene_edit"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Save Changes")
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Select a Scene card to edit detailed scene instructions", color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.padding(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SceneConfigCard(scene: Scene, onEdit: () -> Unit, onDelete: () -> Unit, onGenerateSingle: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            // Left Mini Image
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(scene.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                if (scene.status == "Generating") {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Scene #${scene.sceneIndex + 1}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Badge(containerColor = if (scene.status == "Ready") Color(0xFF4CAF50) else Color(0xFFFF9800)) {
                        Text(scene.status, color = Color.White, fontSize = 9.sp)
                    }
                    Badge(containerColor = MaterialTheme.colorScheme.tertiaryContainer) {
                        Text("60 Credits", color = MaterialTheme.colorScheme.onTertiaryContainer, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                // High contrast, highly readable narration text box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = scene.narration,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Prompt: ${scene.visualPrompt}",
                    fontSize = 11.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${scene.durationSeconds}s | Transition: ${scene.transition} | Cam: ${scene.cameraDirection}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Edit Actions
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.End
            ) {
                IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit Scene", modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onGenerateSingle, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = "Generate Visual", modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete Scene", modifier = Modifier.size(16.dp), tint = Color.Red)
                }
            }
        }
    }
}

// ==========================================
// PANE: My Projects
// ==========================================

@Composable
fun MyProjectsPane(viewModel: AppViewModel) {
    val projects by viewModel.allProjects.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredList by viewModel.filteredProjects.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("My Video Projects", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("Search projects...") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                singleLine = true,
                modifier = Modifier
                    .width(280.dp)
                    .testTag("search_projects")
            )
        }

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("No projects found matching that criteria.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 260.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredList) { project ->
                    val context = LocalContext.current
                    ProjectCard(
                        project = project,
                        onEdit = { viewModel.selectProjectForEditing(project) },
                        onDelete = { viewModel.deleteProject(project) },
                        onDuplicate = { viewModel.duplicateProject(project) },
                        onCopyLink = { viewModel.copyVideoLink(context, project.id, project.name) },
                        onShare = { viewModel.shareVideoLink(context, project.id, project.name) }
                    )
                }
            }
        }
    }
}

@Composable
fun ProjectCard(
    project: Project,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onCopyLink: (() -> Unit)? = null,
    onShare: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(Color.Gray.copy(alpha = 0.1f))
            ) {
                // Background visual asset
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data("https://images.unsplash.com/photo-1626544827763-d516dce335e2?q=80&w=600&auto=format&fit=crop")
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Badge(
                        containerColor = if (project.status == "Completed") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        contentColor = Color.White
                    ) {
                        Text(project.status, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = project.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Topic: " + project.topic,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${project.resolution} | ${project.videoFormat} | ${project.lengthType}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onEdit) {
                        Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit", fontSize = 12.sp)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (onCopyLink != null) {
                            IconButton(onClick = onCopyLink, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Filled.Link, contentDescription = "Copy Video Link", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        if (onShare != null) {
                            IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Filled.Share, contentDescription = "Share Video Link", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                            }
                        }
                        IconButton(onClick = onDuplicate, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = "Duplicate Project", modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp), tint = Color.Red)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// PANE: AI Script Writer pane
// ==========================================

@Composable
fun ScriptWriterPane(viewModel: AppViewModel) {
    val topic by viewModel.wizardTopic.collectAsState()
    val promptVal by viewModel.wizardPrompt.collectAsState()
    val lengthVal by viewModel.wizardLength.collectAsState()
    val scriptVal by viewModel.wizardScript.collectAsState()
    val isGen by viewModel.isGeneratingScript.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("AI Script Writer Studio", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Settings", fontSize = 18.sp, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = topic,
                        onValueChange = { viewModel.wizardTopic.value = it },
                        label = { Text("Topic") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = promptVal,
                        onValueChange = { viewModel.wizardPrompt.value = it },
                        label = { Text("Custom prompt requirements") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = { viewModel.generateScript() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("run_script_gen"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Draft Script with AI")
                    }
                }

                Spacer(modifier = Modifier.width(20.dp))

                Column(modifier = Modifier.weight(1.5f)) {
                    Text("AI Output Script Content", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                    if (isGen) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        OutlinedTextField(
                            value = scriptVal,
                            onValueChange = { viewModel.wizardScript.value = it },
                            label = { Text("Script Output") },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// PANE: AI Voice Studio
// ==========================================

@Composable
fun VoiceStudioPane(viewModel: AppViewModel) {
    val text by viewModel.voiceText.collectAsState()
    val gender by viewModel.voiceGender.collectAsState()
    val speed by viewModel.voiceSpeed.collectAsState()
    val pitch by viewModel.voicePitch.collectAsState()
    val emotion by viewModel.voiceEmotion.collectAsState()
    val accent by viewModel.voiceAccent.collectAsState()
    val isGen by viewModel.isGeneratingVoice.collectAsState()
    val audioPath by viewModel.generatedAudioPath.collectAsState()

    var isPlaying by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("AI Voice Studio (TTS)", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                // Controls
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Voice Configuration", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)

                    OutlinedTextField(
                        value = text,
                        onValueChange = { viewModel.voiceText.value = it },
                        label = { Text("Narration Script") },
                        minLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Male", "Female").forEach { g ->
                            FilterChip(
                                selected = gender == g,
                                onClick = { viewModel.voiceGender.value = g },
                                label = { Text(g) }
                            )
                        }
                    }

                    Column {
                        Text("Speed rate: " + String.format("%.1f", speed) + "x", style = MaterialTheme.typography.labelSmall)
                        Slider(value = speed, onValueChange = { viewModel.voiceSpeed.value = it }, valueRange = 0.5f..2.0f)
                    }

                    Column {
                        Text("Pitch frequency: " + String.format("%.1f", pitch), style = MaterialTheme.typography.labelSmall)
                        Slider(value = pitch, onValueChange = { viewModel.voicePitch.value = it }, valueRange = 0.5f..2.0f)
                    }

                    OutlinedTextField(
                        value = emotion,
                        onValueChange = { viewModel.voiceEmotion.value = it },
                        label = { Text("Voice Emotion Type") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = accent,
                        onValueChange = { viewModel.voiceAccent.value = it },
                        label = { Text("Accent Preset") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = { viewModel.generateVoiceStudioNarration() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("synthesize_btn")
                    ) {
                        if (isGen) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        } else {
                            Text("Synthesize Audio Speech (10 credits)")
                        }
                    }
                }

                // Audio Player Visualizer Simulation
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (audioPath != null) {
                        Icon(Icons.Filled.CheckCircle, "Completed", modifier = Modifier.size(64.dp), tint = Color(0xFF4CAF50))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Audio Generated Successfully", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Filename: $audioPath", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(modifier = Modifier.height(24.dp))

                        // Waveform visualization
                        Row(
                            modifier = Modifier
                                .height(50.dp)
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf(0.4f, 0.7f, 0.5f, 0.9f, 0.3f, 0.8f, 0.6f, 0.2f, 0.8f, 0.5f, 0.9f, 0.4f, 0.7f).forEach { h ->
                                Box(
                                    modifier = Modifier
                                        .width(6.dp)
                                        .fillMaxHeight(if (isPlaying) h else 0.1f)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        IconButton(
                            onClick = { isPlaying = !isPlaying },
                            modifier = Modifier
                                .size(56.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(28.dp)),
                            colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary)
                        ) {
                            Icon(if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = "Play Audio", modifier = Modifier.size(28.dp))
                        }
                    } else {
                        Icon(Icons.Filled.AudioFile, "No File", modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No voice generated yet. Adjust parameters and click Synthesize.", color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

// ==========================================
// PANE: Video Editor (Timeline View)
// ==========================================

@Composable
fun VideoEditorPane(viewModel: AppViewModel, activeProject: Project?, activeScenes: List<Scene>) {
    var selectedSceneId by remember { mutableStateOf("") }
    var currentSubtitlesText by remember { mutableStateOf("") }

    val brandNameText by viewModel.brandNameText.collectAsState()
    val brandPosition by viewModel.brandPosition.collectAsState()

    val selectedScene = remember(selectedSceneId, activeScenes) {
        activeScenes.find { it.id == selectedSceneId } ?: activeScenes.firstOrNull()
    }

    LaunchedEffect(selectedScene) {
        if (selectedScene != null) {
            currentSubtitlesText = selectedScene.narration
            selectedSceneId = selectedScene.id
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Upper Title bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Video Timeline Editor - ${activeProject?.name ?: "No project"}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Reorder clips, configure backgrounds, append subtitles, and manage video presets.", fontSize = 12.sp, color = Color.Gray)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { viewModel.setScreen(Screen.SCENE_BUILDER) }) {
                    Icon(Icons.Filled.List, null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Scene Config")
                }
                OutlinedButton(onClick = { viewModel.setScreen(Screen.THUMBNAIL_GENERATOR) }) {
                    Icon(Icons.Filled.Photo, null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Thumbnail Idea")
                }
                OutlinedButton(onClick = { viewModel.setScreen(Screen.SEO_GENERATOR) }) {
                    Icon(Icons.Filled.TrendingUp, null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("YouTube SEO")
                }
                Button(onClick = { viewModel.setScreen(Screen.EXPORT_PREVIEW) }) {
                    Icon(Icons.Filled.Publish, null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Render MP4 Clip")
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.2f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Screen Preview Canvas
            Card(
                modifier = Modifier
                    .weight(1.5f)
                    .fillMaxHeight(),
                colors = CardDefaults.cardColors(containerColor = Color.Black)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (selectedScene != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(selectedScene.imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Active scene graphic render",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Subtitle overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Text(
                                text = selectedScene.narration,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                textAlign = TextAlign.Center
                            )
                        }

                        // Custom Watermark Branding overlay
                        if (brandNameText.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = if (brandPosition == "Upper") Alignment.TopEnd else Alignment.BottomEnd
                            ) {
                                Row(
                                    modifier = Modifier
                                        .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(6.dp))
                                        .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Copyright,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = brandNameText,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    } else {
                        Text("No scenes built yet.", color = Color.LightGray)
                    }
                }
            }

            // Quick Parameters Editing panel
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (selectedScene != null) {
                        item { Text("Active Scene Properties", fontSize = 16.sp, fontWeight = FontWeight.Bold) }

                        item {
                            OutlinedTextField(
                                value = selectedScene.narration,
                                onValueChange = {
                                    viewModel.updateSceneDetail(selectedScene.copy(narration = it))
                                },
                                label = { Text("Narration & Subtitles Text") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = selectedScene.visualPrompt,
                                onValueChange = {
                                    viewModel.updateSceneDetail(selectedScene.copy(visualPrompt = it))
                                },
                                label = { Text("AI Video/Image Prompt") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = selectedScene.durationSeconds.toString(),
                                    onValueChange = {
                                        val d = it.toIntOrNull() ?: selectedScene.durationSeconds
                                        viewModel.updateSceneDetail(selectedScene.copy(durationSeconds = d))
                                    },
                                    label = { Text("Duration (s)") },
                                    modifier = Modifier.weight(1f)
                                )

                                OutlinedTextField(
                                    value = selectedScene.transition,
                                    onValueChange = {
                                        viewModel.updateSceneDetail(selectedScene.copy(transition = it))
                                    },
                                    label = { Text("Transition Preset") },
                                    modifier = Modifier.weight(1.5f)
                                )
                            }
                        }

                        item {
                            Button(
                                onClick = { viewModel.generateSingleVisual(selectedScene) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("regenerate_scene_visual_btn")
                            ) {
                                Icon(Icons.Filled.AutoAwesome, null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Re-Generate Scene Visual")
                            }
                        }

                        item {
                            Button(
                                onClick = { viewModel.addSceneToEnd() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Filled.Add, null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Append New Scene to Timeline")
                            }
                        }
                    } else {
                        item {
                            Text("No Scene Selected", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Horizontal Timeline Tracks Scroll Area
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Clips Timeline Track", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(activeScenes) { scene ->
                        val isSelected = scene.id == selectedSceneId
                        Card(
                            modifier = Modifier
                                .width(180.dp)
                                .fillMaxHeight()
                                .clickable { selectedSceneId = scene.id }
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(scene.imageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.4f))
                                        .padding(8.dp),
                                    contentAlignment = Alignment.BottomStart
                                ) {
                                    Column {
                                        Text(
                                            text = "Scene ${scene.sceneIndex + 1} (${scene.durationSeconds}s)",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = scene.narration,
                                            color = Color.LightGray,
                                            fontSize = 9.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                // Quick ordering triggers
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopEnd) {
                                    Row(modifier = Modifier.padding(2.dp)) {
                                        IconButton(
                                            onClick = { viewModel.reorderScenes(scene.sceneIndex, scene.sceneIndex - 1) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Filled.ArrowBack, "Move Up", tint = Color.White, modifier = Modifier.size(14.dp))
                                        }
                                        IconButton(
                                            onClick = { viewModel.reorderScenes(scene.sceneIndex, scene.sceneIndex + 1) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Filled.ArrowForward, "Move Down", tint = Color.White, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// PANE: Thumbnail Generator Screen
// ==========================================

@Composable
fun ThumbnailGeneratorPane(viewModel: AppViewModel, activeProject: Project?) {
    val thumbnailIdeas by viewModel.thumbnailIdeas.collectAsState()
    val isGen by viewModel.isGeneratingThumbnails.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("AI YouTube Thumbnail Generator", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Generate multiple distinct visual concepts for video clickable banners.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Button(
            onClick = { viewModel.generateVideoThumbnails() },
            modifier = Modifier.testTag("run_thumbnail_gen")
        ) {
            Icon(Icons.Filled.AutoAwesome, null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Synthesize Thumbnail Suggestions (8 Credits)")
        }

        if (isGen) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 220.dp),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(thumbnailIdeas) { idea ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clickable { viewModel.selectThumbnail(idea.id) }
                            .border(
                                width = if (idea.isSelected) 4.dp else 1.dp,
                                color = if (idea.isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column {
                            Box(modifier = Modifier.weight(1f)) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(idea.imageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = idea.description,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(idea.description, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 2)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = idea.isSelected, onClick = { viewModel.selectThumbnail(idea.id) })
                                    Text("Select Concept", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// PANE: YouTube SEO Generator Screen
// ==========================================

@Composable
fun SeoGeneratorPane(viewModel: AppViewModel, activeProject: Project?) {
    val seoData by viewModel.seoData.collectAsState()
    val isGen by viewModel.isGeneratingSeo.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("AI YouTube SEO Meta Suite", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Button(
            onClick = { viewModel.generateVideoSeo() },
            modifier = Modifier.testTag("run_seo_gen")
        ) {
            Icon(Icons.Filled.TrendingUp, null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Generate Title, Descr, Chapters & Tags (4 Credits)")
        }

        if (isGen) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (seoData != null) {
            val seo = seoData!!
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    SeoItemBox("Recommended YouTube Title", seo.youtubeTitle)
                }
                item {
                    SeoItemBox("Video Description", seo.description)
                }
                item {
                    SeoItemBox("YouTube Tags", seo.tags)
                }
                item {
                    SeoItemBox("Timestamp Chapters", seo.chapters)
                }
                item {
                    SeoItemBox("Suggested Hashtags", seo.hashtags)
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("Click Generate to run Gemini SEO metadata search algorithms.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun SeoItemBox(label: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy Content", modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(content, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

// ==========================================
// PANE: Export and rendering previews
// ==========================================

@Composable
fun ExportPreviewPane(viewModel: AppViewModel, activeProject: Project?) {
    val renderProgress by viewModel.activeRenderProgress.collectAsState()
    val isRendering by viewModel.isRenderingVideo.collectAsState()
    val filePath by viewModel.renderedFilePath.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.widthIn(max = 500.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Export Render Engine", fontSize = 22.sp, fontWeight = FontWeight.Bold)

                if (isRendering) {
                    CircularProgressIndicator(progress = renderProgress / 100f, modifier = Modifier.size(100.dp), strokeWidth = 8.dp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Rendering MP4 video tracks: $renderProgress%", fontWeight = FontWeight.Bold)
                    Text("Stitching background audio, scenes, subtitles...", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LinearProgressIndicator(progress = renderProgress / 100f, modifier = Modifier.fillMaxWidth())
                } else if (filePath != null) {
                    Icon(Icons.Filled.CheckCircle, null, modifier = Modifier.size(80.dp), tint = Color(0xFF4CAF50))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Render Complete!", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Saved Path: $filePath", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = { viewModel.setScreen(Screen.DASHBOARD) }) {
                            Text("Go to Dashboard")
                        }
                        OutlinedButton(onClick = {}) {
                            Icon(Icons.Filled.Share, null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share Link")
                        }
                    }
                } else {
                    Text("Format Preset: ${activeProject?.videoFormat}")
                    Text("Resolution Preset: ${activeProject?.resolution}")

                    Button(
                        onClick = { viewModel.triggerVideoRender() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("render_video_action_btn")
                    ) {
                        Icon(Icons.Filled.MovieCreation, null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Start Cloud Video Rendering (25 credits)")
                    }
                }
            }
        }
    }
}

// ==========================================
// PANE: Billing and Subscription
// ==========================================

@Composable
fun BillingPane(viewModel: AppViewModel, userProfile: UserProfile?) {
    val invoices by viewModel.allInvoices.collectAsState()
    val billingStatus by viewModel.playBillingManager.billingStatusMessage.collectAsState()
    val context = LocalContext.current
    var isYearlyGlobal by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Billing & Premium Pricing Plans",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Upgrade your plan, top up AI generation credits, or choose yearly billing to save up to 21%.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Badge(
                        containerColor = Color(0xFF1B5E20),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = "Play Billing 8.0.0 Active",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // AdMob Rewarded Ad Option: Earn free credits
        item {
            WatchAdForCreditsCard(
                onEarnCredits = { amount ->
                    viewModel.awardAdBonusCredits(amount)
                }
            )
        }

        // Global Period Switcher
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Monthly Payment",
                    fontWeight = if (!isYearlyGlobal) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp,
                    color = if (!isYearlyGlobal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(14.dp))
                Switch(
                    checked = isYearlyGlobal,
                    onCheckedChange = { isYearlyGlobal = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
                Spacer(modifier = Modifier.width(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Yearly Payment",
                        fontWeight = if (isYearlyGlobal) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp,
                        color = if (isYearlyGlobal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF4CAF50).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Save up to 21%",
                            color = Color(0xFF2E7D32),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Free Plan Card
        item {
            PlanCard(
                tierName = "Free",
                monthlyPrice = "$0.00",
                yearlyMonthlyEquivalent = "$0.00",
                yearlyTotalPrice = "$0.00",
                yearlySavingsLabel = "Free Forever",
                creditsMonthly = "50 credits/mo",
                creditsYearly = "50 credits/mo",
                features = listOf(
                    "720p HD Renderings",
                    "Standard AI Voices",
                    "3 Saved Project Drafts",
                    "Community Discord Support"
                ),
                isSelected = userProfile?.subscriptionTier?.startsWith("Free") == true,
                isCurrentYearly = false,
                initialYearly = isYearlyGlobal,
                onSelectMonthly = { viewModel.selectSubscriptionTier("Free", false) },
                onSelectYearly = { viewModel.selectSubscriptionTier("Free", false) }
            )
        }

        // Starter Plan Card
        item {
            val activity = context as? Activity
            PlanCard(
                tierName = "Starter",
                monthlyPrice = "$19.00",
                yearlyMonthlyEquivalent = "$15.00",
                yearlyTotalPrice = "$180.00",
                yearlySavingsLabel = "Save 21% (Save $48/year)",
                creditsMonthly = "500 credits/mo",
                creditsYearly = "7,000 credits/yr (+1,000 bonus credits!)",
                features = listOf(
                    "Up to 1080p Full HD Export",
                    "Premium Neural AI Voices",
                    "Unsplash Image-to-Video Synthesis",
                    "10 Project Cloud Folders",
                    "Remove Watermark & Custom Logo Overlay",
                    "Ad-Free Rendering Experience",
                    "Extended Video Duration (Up to 10 mins)"
                ),
                isSelected = userProfile?.subscriptionTier?.startsWith("Starter") == true,
                isCurrentYearly = userProfile?.subscriptionTier?.contains("Yearly") == true,
                initialYearly = isYearlyGlobal,
                onSelectMonthly = {
                    if (activity != null) {
                        viewModel.launchPlayBillingPurchase(activity, "Starter", false)
                    } else {
                        viewModel.selectSubscriptionTier("Starter", false)
                    }
                },
                onSelectYearly = {
                    if (activity != null) {
                        viewModel.launchPlayBillingPurchase(activity, "Starter", true)
                    } else {
                        viewModel.selectSubscriptionTier("Starter", true)
                    }
                }
            )
        }

        // Pro Plan Card
        item {
            val activity = context as? Activity
            PlanCard(
                tierName = "Pro",
                monthlyPrice = "$49.00",
                yearlyMonthlyEquivalent = "$39.00",
                yearlyTotalPrice = "$468.00",
                yearlySavingsLabel = "Save 20% (Save $120/year)",
                creditsMonthly = "1,500 credits/mo",
                creditsYearly = "20,000 credits/yr (+2,000 bonus credits!)",
                features = listOf(
                    "Ultra 4K Broadcast-Quality Renderings",
                    "Voice Studio Custom Neural Voice Clones",
                    "All AI Presenter & Digital Avatars",
                    "Unlimited Cloud Folder Storage",
                    "Remove Watermark & Custom Logo Overlay",
                    "Ad-Free Rendering Experience",
                    "Extended Video Duration (Up to 20 mins)",
                    "Priority Cloud Rendering Queue"
                ),
                isSelected = userProfile?.subscriptionTier?.startsWith("Pro") == true,
                isCurrentYearly = userProfile?.subscriptionTier?.contains("Yearly") == true,
                initialYearly = isYearlyGlobal,
                onSelectMonthly = {
                    if (activity != null) {
                        viewModel.launchPlayBillingPurchase(activity, "Pro", false)
                    } else {
                        viewModel.selectSubscriptionTier("Pro", false)
                    }
                },
                onSelectYearly = {
                    if (activity != null) {
                        viewModel.launchPlayBillingPurchase(activity, "Pro", true)
                    } else {
                        viewModel.selectSubscriptionTier("Pro", true)
                    }
                }
            )
        }

        // Business Plan Card
        item {
            val activity = context as? Activity
            PlanCard(
                tierName = "Business",
                monthlyPrice = "$149.00",
                yearlyMonthlyEquivalent = "$119.00",
                yearlyTotalPrice = "$1,428.00",
                yearlySavingsLabel = "Save 20% (Save $360/year)",
                creditsMonthly = "5,000 credits/mo",
                creditsYearly = "70,000 credits/yr (+10,000 bonus credits!)",
                features = listOf(
                    "Dedicated High-Compute GPU Pipeline",
                    "Full Commercial & Reseller Ownership Rights",
                    "Custom REST API & Webhook Endpoints",
                    "Multi-Seat Admin Dashboard Access",
                    "Remove Watermark & Custom Logo Overlay",
                    "Ad-Free Rendering Experience",
                    "Maximum Video Duration (Up to 60 mins)",
                    "Dedicated 24/7 VIP Account Manager"
                ),
                isSelected = userProfile?.subscriptionTier?.startsWith("Business") == true,
                isCurrentYearly = userProfile?.subscriptionTier?.contains("Yearly") == true,
                initialYearly = isYearlyGlobal,
                onSelectMonthly = {
                    if (activity != null) {
                        viewModel.launchPlayBillingPurchase(activity, "Business", false)
                    } else {
                        viewModel.selectSubscriptionTier("Business", false)
                    }
                },
                onSelectYearly = {
                    if (activity != null) {
                        viewModel.launchPlayBillingPurchase(activity, "Business", true)
                    } else {
                        viewModel.selectSubscriptionTier("Business", true)
                    }
                }
            )
        }

        // AdMob Banner Ad at Bottom of Billing Page
        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SPONSORED ADVERTISEMENT (ADMOB)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    letterSpacing = 1.sp
                )
                AdMobBanner()
            }
        }

        // Invoice History Table
        item {
            Text("Invoices & Payment History", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }

        if (invoices.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No billing transactions found yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            items(invoices) { invoice ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(invoice.id, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("Plan / Package: ${invoice.planName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Date: ${invoice.dateString}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(text = "+${invoice.creditsAdded} Credits", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(invoice.amount, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            Badge(containerColor = Color(0xFF4CAF50)) {
                                Text(invoice.status, color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlanCard(
    tierName: String,
    monthlyPrice: String,
    yearlyMonthlyEquivalent: String,
    yearlyTotalPrice: String,
    yearlySavingsLabel: String,
    creditsMonthly: String,
    creditsYearly: String,
    features: List<String>,
    isSelected: Boolean,
    isCurrentYearly: Boolean,
    initialYearly: Boolean,
    onSelectMonthly: () -> Unit,
    onSelectYearly: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCycleIsYearly by remember(initialYearly) { mutableStateOf(initialYearly) }
    val isFree = tierName == "Free"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .testTag("plan_card_${tierName.lowercase()}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row: Plan Name & Active Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = tierName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    if (isSelected) {
                        Badge(containerColor = Color(0xFF2E7D32)) {
                            Text(
                                text = if (isCurrentYearly) "Active (Yearly)" else "Active Plan",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Price display reflecting current chosen cycle
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isFree) "$0.00" else if (selectedCycleIsYearly) yearlyMonthlyEquivalent else monthlyPrice,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 30.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isFree) "free forever" else if (selectedCycleIsYearly) "per month, billed annually ($yearlyTotalPrice/yr)" else "per month",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Per-Plan Yearly Payment Upgrade Toggle (for paid tiers)
            if (!isFree) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Monthly Option Tab
                        Button(
                            onClick = { selectedCycleIsYearly = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!selectedCycleIsYearly) MaterialTheme.colorScheme.surface else Color.Transparent,
                                contentColor = if (!selectedCycleIsYearly) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp),
                            elevation = if (!selectedCycleIsYearly) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Text("Monthly: $monthlyPrice/mo", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        // Yearly Option Tab
                        Button(
                            onClick = { selectedCycleIsYearly = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedCycleIsYearly) MaterialTheme.colorScheme.primary else Color.Transparent,
                                contentColor = if (selectedCycleIsYearly) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp),
                            elevation = if (selectedCycleIsYearly) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                            modifier = Modifier.weight(1.2f),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Yearly: $yearlyMonthlyEquivalent/mo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(yearlySavingsLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Credit allocation text
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Filled.Payment, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                Text(
                    text = if (selectedCycleIsYearly && !isFree) creditsYearly else creditsMonthly,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                modifier = Modifier.padding(vertical = 2.dp)
            )

            // Features list
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                features.forEach { feature ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = feature,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Action Buttons: Monthly selection and Dedicated Yearly Upgrade Button
            if (isFree) {
                Button(
                    onClick = onSelectMonthly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("subscribe_to_${tierName.lowercase()}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = if (isSelected) "Active Free Plan" else "Select Free Plan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Standard Monthly Option Button
                    OutlinedButton(
                        onClick = onSelectMonthly,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("subscribe_monthly_${tierName.lowercase()}"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Pay Monthly ($monthlyPrice)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Highlighted Yearly Payment Upgrade Button
                    Button(
                        onClick = onSelectYearly,
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp)
                            .testTag("upgrade_yearly_${tierName.lowercase()}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected && isCurrentYearly) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        )
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isSelected && isCurrentYearly) "Active Yearly Plan" else "Upgrade to Yearly",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "$yearlyTotalPrice/yr (Save 20%+)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// PANE: Admin Panel View
// ==========================================

@Composable
fun AdminPanelPane(viewModel: AppViewModel) {
    val users by viewModel.adminMockUsers.collectAsState()
    val revenue by viewModel.adminRevenue.collectAsState()
    val apiUsage by viewModel.adminApiUsage.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text("Admin SaaS Analytics & Controls", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        // Metrics Summary Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MetricCard(
                    title = "Total SaaS Revenue",
                    value = revenue,
                    subtitle = "This Month to Date",
                    icon = Icons.Filled.AttachMoney,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "API Integrations Calls",
                    value = "$apiUsage",
                    subtitle = "Total network model calls",
                    icon = Icons.Filled.Analytics,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Active Users Registered",
                    value = "${users.size}",
                    subtitle = "SaaS subscribers count",
                    icon = Icons.Filled.People,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // User Account management List
        item {
            Text("Registered Subscribers Accounts Management", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        items(users) { user ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(user.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(user.email, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Badge(containerColor = if (user.subscriptionTier == "Suspended") Color.Red else MaterialTheme.colorScheme.secondary) {
                                Text(user.subscriptionTier, color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 4.dp))
                            }
                            Text("Credits: ${user.creditsRemaining}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.adminAdjustCredits(user.email, user.creditsRemaining + 100) },
                            modifier = Modifier.testTag("admin_add_credits_${user.email.split("@").first()}")
                        ) {
                            Text("+100 Credits")
                        }

                        OutlinedButton(onClick = { viewModel.adminAdjustSubscription(user.email, "Pro") }) {
                            Text("Grant Pro")
                        }

                        OutlinedButton(onClick = { viewModel.adminAdjustSubscription(user.email, "Business") }) {
                            Text("Grant Business")
                        }

                        Button(
                            onClick = { viewModel.adminSuspendAccount(user.email) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            modifier = Modifier.testTag("admin_suspend_${user.email.split("@").first()}")
                        ) {
                            Text("Suspend", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// PANES: Minimal static wrappers
// ==========================================

@Composable
fun AICharactersPane() {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("AI Avatar & Characters Library", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Connect diverse virtual announcers, customize clothing and backgrounds.", fontSize = 14.sp)
        LazyVerticalGrid(columns = GridCells.Adaptive(minSize = 220.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(listOf("Alex (Presenter)", "Emma (Lifestyle Vlogger)", "Hiro (Tech Educator)", "Sophia (Corporate Speaker)")) { char ->
                Card(modifier = Modifier.height(200.dp)) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Gray.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Face, null, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(char, fontWeight = FontWeight.Bold)
                            Text("Ready for TTS lipsync", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MediaLibraryPane(viewModel: AppViewModel) {
    val uploadedMusic by viewModel.uploadedMusic.collectAsState()
    val uploadedLogos by viewModel.uploadedLogos.collectAsState()
    val brandNameText by viewModel.brandNameText.collectAsState()
    val brandPosition by viewModel.brandPosition.collectAsState()

    var newMusicName by remember { mutableStateOf("") }
    var newLogoName by remember { mutableStateOf("") }
    var tempBrandName by remember { mutableStateOf("") }
    var tempBrandPos by remember { mutableStateOf("Upper") }

    LaunchedEffect(brandNameText, brandPosition) {
        tempBrandName = brandNameText
        tempBrandPos = brandPosition
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Column {
                Text("Personal Media Library", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text("Upload custom background audio tracks, brand watermarks, and control video overlay placements.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // 1. Asset Uploads Block
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Background Music Section
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.LibraryMusic, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Background Music Tracks", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        
                        // Upload control
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newMusicName,
                                onValueChange = { newMusicName = it },
                                label = { Text("New Track Name", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    if (newMusicName.isNotBlank()) {
                                        viewModel.uploadMusic(newMusicName.trim())
                                        newMusicName = ""
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Icon(Icons.Filled.Add, "Upload Track")
                            }
                        }

                        // Tracks list
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            uploadedMusic.forEach { track ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.MusicNote, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(track, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                                    Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                                        Text("10 credits", color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Brand Logos & Pictures Section
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Image, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Brand Logos & Graphics", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }

                        // Upload control
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newLogoName,
                                onValueChange = { newLogoName = it },
                                label = { Text("Logo Asset Name", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    if (newLogoName.isNotBlank()) {
                                        viewModel.uploadLogo(newLogoName.trim())
                                        newLogoName = ""
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Icon(Icons.Filled.Add, "Upload Logo")
                            }
                        }

                        // Logos list
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            uploadedLogos.forEach { logo ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.Photo, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(logo, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                                    Text("PNG", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Video Branding Overlay Settings Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Copyright, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Active Branding & Watermark Overlay Settings", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Settings Input Panel
                        Column(
                            modifier = Modifier.weight(1.2f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = tempBrandName,
                                onValueChange = { tempBrandName = it },
                                label = { Text("Brand Name / Watermark Text") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text("Watermark Overlay Position:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                listOf("Upper", "Footer").forEach { pos ->
                                    val isSelected = tempBrandPos == pos
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                            .clickable { tempBrandPos = pos }
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = if (pos == "Upper") "Upper (Top)" else "Footer (Bottom)",
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    viewModel.updateBrandSettings(tempBrandName, tempBrandPos)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.Save, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Save Watermark Branding Settings")
                            }
                        }

                        // Live Video Preview Frame Simulation
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Watermark Visual Live-Preview", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .background(Color.Black, RoundedCornerShape(12.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            ) {
                                // Mock video content image
                                AsyncImage(
                                    model = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?q=80&w=400&auto=format&fit=crop",
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                                )

                                // Dim Overlay
                                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))

                                // 1. Brand Overlay Watermark depending on selected state
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp),
                                    contentAlignment = if (tempBrandPos == "Upper") Alignment.TopCenter else Alignment.BottomCenter
                                ) {
                                    if (tempBrandName.isNotBlank()) {
                                        Row(
                                            modifier = Modifier
                                                .background(Color.Black.copy(alpha = 0.85f), RoundedCornerShape(6.dp))
                                                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(Icons.Filled.Copyright, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                                            Text(
                                                text = tempBrandName,
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                // 2. Subtitles simulation (placed oppositely or at the bottom)
                                if (tempBrandPos == "Upper") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(bottom = 12.dp),
                                        contentAlignment = Alignment.BottomCenter
                                    ) {
                                        Text(
                                            text = "[Narrator]: Live preview of custom watermark.",
                                            color = Color.Yellow,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier
                                                .background(Color.Black.copy(alpha = 0.85f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Recommended Video Templates & Media Assets from Firebase Storage
        item {
            val storageItems by viewModel.recommendedStorageItems.collectAsState()
            val context = LocalContext.current

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Column {
                            Text("Recommended Cloud Storage Assets", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("Downloadable stock scenes, UGC assets, and templates hosted in Firebase Storage", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                        Text("Cloud Bucket", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(storageItems) { item ->
                        StorageRecommendationCard(
                            item = item,
                            onCopyLink = { viewModel.copyVideoLink(context, item.id, item.title) },
                            onShare = { viewModel.shareVideoLink(context, item.id, item.title) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VideoHistoryPane(viewModel: AppViewModel) {
    val projects by viewModel.allProjects.collectAsState()
    val completedOnly = remember(projects) { projects.filter { it.status == "Completed" } }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("SaaS Render History", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        if (completedOnly.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No rendered completed YouTube videos found in cache history.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(completedOnly) { proj ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Movie, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(proj.name, fontWeight = FontWeight.Bold)
                                Text("Rendered at: ${proj.resolution} | ${proj.videoFormat}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(
                                onClick = { viewModel.copyVideoLink(context, proj.id, proj.name) },
                                modifier = Modifier.size(36.dp).testTag("copy_link_${proj.id}")
                            ) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = "Copy Link", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                            IconButton(
                                onClick = { viewModel.shareVideoLink(context, proj.id, proj.name) },
                                modifier = Modifier.size(36.dp).testTag("share_link_${proj.id}")
                            ) {
                                Icon(Icons.Filled.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Button(onClick = { viewModel.selectProjectForEditing(proj) }) {
                                Text("Edit/SEO Suite")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsPane(viewModel: AppViewModel, userProfile: UserProfile?) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Account Settings & API Configurations", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Profile Settings", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                OutlinedTextField(value = userProfile?.name ?: "User Creator", onValueChange = {}, label = { Text("Display Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = userProfile?.email ?: "", onValueChange = {}, label = { Text("Email") }, readOnly = true, modifier = Modifier.fillMaxWidth())

                HorizontalDivider()

                Text("Cloud API Integrations Status", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CheckCircle, null, tint = Color(0xFF4CAF50))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gemini REST AI Engine: Live & Online")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CheckCircle, null, tint = Color(0xFF4CAF50))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cloud Text-to-Speech Engine: Active")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CheckCircle, null, tint = Color(0xFF4CAF50))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Firebase Crashlytics & Storage: Integrated")
                }
            }
        }
    }
}

@Composable
fun BackendSettingsPane(viewModel: AppViewModel) {
    val apiKey by viewModel.geminiApiKey.collectAsState()
    val isKeyValid by viewModel.isGeminiKeyValid.collectAsState()
    val testMessage by viewModel.geminiKeyTestMessage.collectAsState()
    val isTesting by viewModel.isTestingGemini.collectAsState()

    val storageBucket by viewModel.customStorageBucket.collectAsState()
    val crashlyticsStatus by viewModel.crashlyticsTestStatus.collectAsState()
    val storageItems by viewModel.recommendedStorageItems.collectAsState()

    var inputKey by remember(apiKey) { mutableStateOf(apiKey) }
    var showKey by remember { mutableStateOf(false) }
    var inputBucket by remember(storageBucket) { mutableStateOf(storageBucket) }

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Cloud Backend & API Secret Management",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Configure your Google AI Studio Gemini API keys, Firebase Cloud Storage bucket, and monitor Firebase Crashlytics telemetry.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // CARD 1: Gemini API Secret Key Configuration
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Filled.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                            Column {
                                Text("Gemini AI API Key & Secret Storage", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text("Powers AI Script Writing, Scene Generation, and Video Synthesis", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Badge(containerColor = if (apiKey.isNotBlank()) Color(0xFF1B5E20) else MaterialTheme.colorScheme.errorContainer) {
                            Text(
                                text = if (apiKey.isNotBlank()) "Key Configured" else "Key Missing",
                                color = if (apiKey.isNotBlank()) Color.White else MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // How to get Gemini API Key instructions card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("How to get your Gemini API Key:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                            Text("1. Visit Google AI Studio at: aistudio.google.com/app/apikey", fontSize = 12.sp)
                            Text("2. Sign in with your Google account and click \"Create API Key\".", fontSize = 12.sp)
                            Text("3. Copy your key, paste it into the field below, and click \"Save Secret API Key\".", fontSize = 12.sp)
                            Text("4. You can also configure MY_API_KEY in the AI Studio Secrets panel or .env file for automated build injection.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Input Field
                    OutlinedTextField(
                        value = inputKey,
                        onValueChange = { inputKey = it },
                        label = { Text("Gemini Secret API Key") },
                        placeholder = { Text("AIzaSy...") },
                        singleLine = true,
                        visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showKey = !showKey }) {
                                Icon(
                                    imageVector = if (showKey) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (showKey) "Hide key" else "Show key"
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("gemini_api_key_input")
                    )

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.saveGeminiApiKey(inputKey) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_gemini_key_button")
                        ) {
                            Icon(Icons.Filled.Save, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Secret API Key")
                        }

                        OutlinedButton(
                            onClick = { viewModel.testGeminiApiKey() },
                            enabled = !isTesting,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("test_gemini_key_button")
                        ) {
                            if (isTesting) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Connecting...")
                            } else {
                                Icon(Icons.Filled.NetworkCheck, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Test Connection")
                            }
                        }
                    }

                    // Status Result
                    if (testMessage.isNotBlank()) {
                        val isSuccess = isKeyValid == true
                        val isError = isKeyValid == false
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when {
                                isSuccess -> Color(0xFF1B5E20).copy(alpha = 0.15f)
                                isError -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            border = BorderStroke(
                                1.dp,
                                when {
                                    isSuccess -> Color(0xFF4CAF50)
                                    isError -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.outline
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = when {
                                        isSuccess -> Icons.Filled.CheckCircle
                                        isError -> Icons.Filled.Error
                                        else -> Icons.Filled.Info
                                    },
                                    contentDescription = null,
                                    tint = when {
                                        isSuccess -> Color(0xFF2E7D32)
                                        isError -> MaterialTheme.colorScheme.error
                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                )
                                Text(
                                    text = testMessage,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // CARD 2: Firebase Storage Bucket & Link Sharing Configuration
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Filled.CloudUpload, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                            Column {
                                Text("Firebase Storage Bucket & Video Link Sharing", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text("Storage destination for user videos, UGC clips, and media downloads", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                            Text("Active Bucket", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    OutlinedTextField(
                        value = inputBucket,
                        onValueChange = { inputBucket = it },
                        label = { Text("Storage Bucket Domain") },
                        placeholder = { Text("studio-4528506733-318e2.firebasestorage.app") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("storage_bucket_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.saveStorageBucket(inputBucket) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_storage_bucket_button")
                        ) {
                            Icon(Icons.Filled.Save, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Bucket")
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.copyVideoLink(context, "sample_ugc_video_01", "Sample GEE AI Video")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("copy_sample_link_button")
                        ) {
                            Icon(Icons.Filled.ContentCopy, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Copy Sample Video Link")
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Live Shareable URL Format:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "https://firebasestorage.googleapis.com/v0/b/$storageBucket/o/user_videos%2F{videoId}.mp4?alt=media",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // CARD 3: Firebase Crashlytics Integration
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Filled.BugReport, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(24.dp))
                            Column {
                                Text("Firebase Crashlytics Health & Stability", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text("Realtime error reporting, exception tracing, and session monitoring", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Badge(containerColor = Color(0xFF1B5E20)) {
                            Text("Crashlytics Active", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                            Text("Crashlytics SDK initialized & bound to application lifecycle", fontSize = 13.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                            Text("Automatic user tagging with authenticated Firebase UID", fontSize = 13.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                            Text("Custom key tracking: has_gemini_api_key, active_plan, user_role", fontSize = 13.sp)
                        }
                    }

                    Button(
                        onClick = { viewModel.triggerCrashlyticsTest() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("test_crashlytics_button")
                    ) {
                        Icon(Icons.Filled.Send, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Send Diagnostic Report to Firebase Crashlytics")
                    }

                    if (crashlyticsStatus.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1B5E20).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color(0xFF4CAF50)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Filled.CheckCircle, null, tint = Color(0xFF2E7D32))
                                Text(crashlyticsStatus, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }

        // CARD 4: Recommended Storage Items Showcase
        item {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.FolderSpecial, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Column {
                            Text("Curated Assets in Firebase Storage", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("Copy shareable links or distribute templates to collaborators", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(storageItems) { item ->
                        StorageRecommendationCard(
                            item = item,
                            onCopyLink = { viewModel.copyVideoLink(context, item.id, item.title) },
                            onShare = { viewModel.shareVideoLink(context, item.id, item.title) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StorageRecommendationCard(
    item: StorageRecommendedItem,
    onCopyLink: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .testTag("storage_card_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                AsyncImage(
                    model = item.thumbnailUrl,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                                startY = 50f
                            )
                        )
                )

                // Category Badge
                Badge(
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = item.category,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }

                // Duration & Size pill
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Schedule, null, tint = Color.White, modifier = Modifier.size(10.dp))
                    Text("${item.durationText} • ${item.fileSizeMb}MB", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = item.description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Filled.Download, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                        Text("${item.downloadsCount} used", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = onCopyLink,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("copy_link_${item.id}")
                        ) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = "Copy Link", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        }
                        IconButton(
                            onClick = onShare,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("share_link_${item.id}")
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HelpPane() {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Help & Developer Support Knowledgebase", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Learn how to master long-form video output using GEE AI templates.", fontSize = 14.sp)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Frequently Asked Questions", fontWeight = FontWeight.Bold)
                Text("Q: What is the maximum duration I can generate?", fontWeight = FontWeight.Bold)
                Text("A: In our Pro plan, you can generate complete scene scripts of up to 30 minutes. Clips render in standard cloud queues.", fontSize = 13.sp)

                Text("Q: How can I change the background soundtrack?", fontWeight = FontWeight.Bold)
                Text("A: In Step 2 of the Creation Wizard or directly in the Timeline Tracks editor, click on the Background Soundtrack dropdown to view available instrumental scores.", fontSize = 13.sp)

                Text("Q: Where are videos stored and how do I share them?", fontWeight = FontWeight.Bold)
                Text("A: Rendered videos are automatically uploaded to your Firebase Cloud Storage bucket. Each video has a 'Copy Link' button and 'Share' action to easily send direct links to clients and social platforms.", fontSize = 13.sp)
            }
        }
    }
}

