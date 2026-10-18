package com.example.data

import android.app.Activity
import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

enum class Screen {
    LOGIN,
    DASHBOARD,
    CREATE_VIDEO,
    IMAGE_TO_VIDEO,
    UGC_VIDEO,
    AVATAR_VIDEO,
    MY_PROJECTS,
    AI_SCRIPT_WRITER,
    AI_VOICE_STUDIO,
    AI_CHARACTERS,
    MEDIA_LIBRARY,
    VIDEO_HISTORY,
    BILLING,
    SETTINGS,
    BACKEND_SETTINGS,
    HELP,
    ADMIN_PANEL,
    SCENE_BUILDER,
    VIDEO_EDITOR,
    THUMBNAIL_GENERATOR,
    SEO_GENERATOR,
    EXPORT_PREVIEW
}

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val db = GeeDatabase.getDatabase(application)
    val projectDao = db.projectDao()
    val sceneDao = db.sceneDao()
    val userProfileDao = db.userProfileDao()
    val invoiceDao = db.invoiceDao()

    // Service Integrations
    private val scriptWriter = ScriptWriterService()
    private val voiceStudio = VoiceStudioService()
    private val videoStudio = VideoStudioService()
    private val thumbnailService = ThumbnailService()
    private val seoService = SeoService()
    private val videoRenderer = VideoRenderer()
    private val paymentService = PaymentService()
    val firebaseManager = FirebaseManager(application)
    val playBillingManager = PlayBillingManager(application) { productId, _ ->
        val tier = when {
            productId.startsWith("starter") -> "Starter"
            productId.startsWith("pro") -> "Pro"
            productId.startsWith("business") -> "Business"
            else -> "Starter"
        }
        val isYearly = productId.endsWith("yearly")
        selectSubscriptionTier(tier, isYearly)
    }

    // Navigation State
    private val _currentScreen = MutableStateFlow(Screen.LOGIN)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Auth State
    private val _currentEmail = MutableStateFlow("")
    val currentEmail: StateFlow<String> = _currentEmail.asStateFlow()

    private val _currentRole = MutableStateFlow("User") // "Admin" or "User"
    val currentRole: StateFlow<String> = _currentRole.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    val isGoogleSigningIn = MutableStateFlow(false)
    val googleSignInError = MutableStateFlow<String?>(null)

    // User Profile Flow (room-backed)
    val userProfile: StateFlow<UserProfile?> = userProfileDao.getUserProfileFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Projects Flow (room-backed)
    val allProjects: StateFlow<List<Project>> = projectDao.getAllProjectsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search Query State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    val filteredProjects: StateFlow<List<Project>> = combine(allProjects, searchQuery) { list, query ->
        if (query.isBlank()) {
            list
        } else {
            list.filter { it.name.contains(query, ignoreCase = true) || it.topic.contains(query, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active project state
    private val _activeProject = MutableStateFlow<Project?>(null)
    val activeProject: StateFlow<Project?> = _activeProject.asStateFlow()

    // Active scenes state
    private val _activeScenes = MutableStateFlow<List<Scene>>(emptyList())
    val activeScenes: StateFlow<List<Scene>> = _activeScenes.asStateFlow()

    // Video creation wizard state
    val wizardTopic = MutableStateFlow("")
    val wizardPrompt = MutableStateFlow("")
    val wizardAudience = MutableStateFlow("General Creators")
    // Manual video duration selection capped at 10 seconds maximum
    val wizardDurationSeconds = MutableStateFlow(10) // 1 to 10 seconds only
    val wizardLength = MutableStateFlow("10 Seconds (Max 10s)") 
    val wizardLanguage = MutableStateFlow("English")
    val wizardTone = MutableStateFlow("Professional")
    val wizardStyle = MutableStateFlow("Cinematic")

    val wizardPresenter = MutableStateFlow("Alex (AI Avatar)")
    val wizardVoice = MutableStateFlow("Bella (Warm/Friendly)")
    val wizardAccent = MutableStateFlow("US American")
    val wizardMusic = MutableStateFlow("Ambient Cinematic")
    val wizardFormat = MutableStateFlow("9:16 (Shorts/Reels)")
    // Quality selection from standard p up to Ultra HD
    val wizardQualityTier = MutableStateFlow("1080p (Full HD)")
    val wizardResolution = MutableStateFlow("1080p")

    // The 7 Target Platforms required before video creation
    val allSevenPlatforms = listOf(
        "YouTube / Shorts",
        "TikTok",
        "Instagram (Reels & Feed)",
        "Facebook (Reels & Stories)",
        "X (Twitter)",
        "LinkedIn Video",
        "Pinterest Watch"
    )
    val wizardTargetPlatforms = MutableStateFlow(
        setOf("YouTube / Shorts", "TikTok", "Instagram (Reels & Feed)")
    )
    val showPlatformConfirmationModal = MutableStateFlow(false)

    val wizardScript = MutableStateFlow("")
    val isGeneratingScript = MutableStateFlow(false)

    fun toggleTargetPlatform(platform: String) {
        val current = wizardTargetPlatforms.value.toMutableSet()
        if (current.contains(platform)) {
            if (current.size > 1) { // Keep at least one selected
                current.remove(platform)
            }
        } else {
            current.add(platform)
        }
        wizardTargetPlatforms.value = current
    }

    /**
     * Look up tokens for each video quality based on Claude script reasoning & Firebase compute rates
     * Deducts user according to video duration (up to 10s) and quality tier
     */
    fun calculateTokenDeduction(durationSec: Int = wizardDurationSeconds.value, quality: String = wizardQualityTier.value): TokenBreakdown {
        val clampedSec = durationSec.coerceIn(1, 10)
        val scriptClaudeTokens = when {
            quality.contains("480p", ignoreCase = true) -> 20
            quality.contains("720p", ignoreCase = true) -> 25
            quality.contains("1080p", ignoreCase = true) -> 35
            quality.contains("1440p", ignoreCase = true) -> 50
            quality.contains("4K", ignoreCase = true) || quality.contains("Ultra HD", ignoreCase = true) -> 75
            else -> 35
        }
        val computeRatePerSec = when {
            quality.contains("480p", ignoreCase = true) -> 6
            quality.contains("720p", ignoreCase = true) -> 10
            quality.contains("1080p", ignoreCase = true) -> 16
            quality.contains("1440p", ignoreCase = true) -> 25
            quality.contains("4K", ignoreCase = true) || quality.contains("Ultra HD", ignoreCase = true) -> 40
            else -> 16
        }
        val renderComputeTokens = computeRatePerSec * clampedSec
        return TokenBreakdown(
            scriptClaudeTokens = scriptClaudeTokens,
            renderComputeTokens = renderComputeTokens,
            totalTokens = scriptClaudeTokens + renderComputeTokens,
            durationSeconds = clampedSec,
            quality = quality
        )
    }

    // Voice Studio Custom Generation states
    val voiceText = MutableStateFlow("Enter narration script text to synthesize...")
    val voiceGender = MutableStateFlow("Female")
    val voiceSpeed = MutableStateFlow(1.0f)
    val voicePitch = MutableStateFlow(1.0f)
    val voiceEmotion = MutableStateFlow("Friendly")
    val voiceAccent = MutableStateFlow("US American")
    val isGeneratingVoice = MutableStateFlow(false)
    val generatedAudioPath = MutableStateFlow<String?>(null)

    // Image-to-Video parameters
    val imgToVideoImageUri = MutableStateFlow("")
    val imgToVideoMotion = MutableStateFlow("Slow Zoom In")
    val imgToVideoDuration = MutableStateFlow("6 seconds")
    val isGeneratingImageToVideo = MutableStateFlow(false)

    // Scene Generation Visual Progress
    val sceneProgressMap = MutableStateFlow<Map<String, Int>>(emptyMap()) // sceneId -> progress
    val activeRenderProgress = MutableStateFlow(0)
    val isRenderingVideo = MutableStateFlow(false)
    val renderedFilePath = MutableStateFlow<String?>(null)

    // Thumbnail Generation states
    val thumbnailIdeas = MutableStateFlow<List<ThumbnailIdea>>(emptyList())
    val isGeneratingThumbnails = MutableStateFlow(false)

    // SEO Generation states
    val seoData = MutableStateFlow<SeoMetadata?>(null)
    val isGeneratingSeo = MutableStateFlow(false)

    // Media Library Assets & Branding state
    val uploadedMusic = MutableStateFlow(listOf("Ambient Cinematic", "Lofi Beats", "Happy Corporate", "Upbeat Rock"))
    val uploadedLogos = MutableStateFlow(listOf("Standard Logo", "Minimal Watermark", "Gee AI Badge"))
    val brandNameText = MutableStateFlow("Gee AI Creator")
    val brandPosition = MutableStateFlow("Upper") // "Upper" or "Footer"
    val imgToVideoHasMusic = MutableStateFlow(false)

    // UGC Video Generator State
    val ugcProductImageUri = MutableStateFlow("")
    val ugcProductName = MutableStateFlow("GlowSkin Natural Botanical Face Serum")
    val ugcStyle = MutableStateFlow("Customer Honest Review")
    val ugcPlatform = MutableStateFlow("TikTok / Reels (9:16)")
    val ugcCreatorPersona = MutableStateFlow("Skincare & Beauty Enthusiast")
    val ugcKeyFeatures = MutableStateFlow("Hydrates in 5 mins, 100% vegan, clean ingredients, visible radiant glow")
    val ugcCallToAction = MutableStateFlow("Tap the link below for an exclusive 25% discount today!")
    val ugcHookStyle = MutableStateFlow("Stop scrolling if you want glass skin with zero harsh chemicals!")
    val ugcGeneratedScript = MutableStateFlow("")
    val isGeneratingUgc = MutableStateFlow(false)
    val ugcRenderedVideoUrl = MutableStateFlow<String?>(null)

    // Avatar Video Studio State
    val avatarCustomImageUri = MutableStateFlow("")
    val avatarName = MutableStateFlow("Elena Vance (Corporate)")
    val avatarFraming = MutableStateFlow("Studio Close-up")
    val avatarEmotion = MutableStateFlow("Warm & Authoritative")
    val avatarBackdrop = MutableStateFlow("Modern Minimalist Office")
    val avatarScript = MutableStateFlow("Welcome to our executive brief. Today, we're demonstrating the next generation of digital avatars powered by photorealistic neural rendering and natural cadence.")
    val avatarVoice = MutableStateFlow("Bella - Warm Professional (US)")
    val avatarGestures = MutableStateFlow("Subtle Natural Hand Gestures")
    val isGeneratingAvatar = MutableStateFlow(false)
    val avatarRenderedVideoUrl = MutableStateFlow<String?>(null)

    // Diverse Multiple Avatars Preset Catalogue
    val defaultAvatars = listOf(
        AvatarOption(
            id = "elena",
            name = "Elena Vance",
            role = "Executive AI & Keynote Speaker",
            category = "Corporate",
            imageUrl = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?q=80&w=600&auto=format&fit=crop",
            voiceRecommendation = "Bella - Warm Professional (US)",
            description = "High-status corporate briefings, quarterly shareholder reports, executive leadership."
        ),
        AvatarOption(
            id = "marcus",
            name = "Marcus Chen",
            role = "Tech Evangelist & Dev Advocate",
            category = "Technology",
            imageUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?q=80&w=600&auto=format&fit=crop",
            voiceRecommendation = "James - Crisp British (UK)",
            description = "SaaS software walkthroughs, API tutorials, silicon valley keynote announcements."
        ),
        AvatarOption(
            id = "sophia",
            name = "Sophia Miller",
            role = "STEM Educator & Master Trainer",
            category = "Education",
            imageUrl = "https://images.unsplash.com/photo-1580489944761-15a19d654956?q=80&w=600&auto=format&fit=crop",
            voiceRecommendation = "Bella - Warm Professional (US)",
            description = "E-learning course modules, customer onboarding series, technical certifications."
        ),
        AvatarOption(
            id = "david",
            name = "David Reynolds",
            role = "Global News Anchor",
            category = "Broadcast",
            imageUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?q=80&w=600&auto=format&fit=crop",
            voiceRecommendation = "James - Crisp British (UK)",
            description = "Breaking industry news broadcasts, financial daily reports, press releases."
        ),
        AvatarOption(
            id = "chloe",
            name = "Chloe Dubois",
            role = "Creative Director & Influencer",
            category = "Creative",
            imageUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=600&auto=format&fit=crop",
            voiceRecommendation = "Amara - Expressive African (NG)",
            description = "Social-first brand narratives, fashion launches, viral entertainment promos."
        ),
        AvatarOption(
            id = "liam",
            name = "Liam O'Connor",
            role = "Performance & Fitness Coach",
            category = "Wellness",
            imageUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?q=80&w=600&auto=format&fit=crop",
            voiceRecommendation = "Kai - Smooth Australian (AU)",
            description = "Motivational speaking, high-impact athletic branding, fitness routines."
        ),
        AvatarOption(
            id = "aisha",
            name = "Aisha Al-Mansoor",
            role = "Venture Analyst & Strategist",
            category = "Finance",
            imageUrl = "https://images.unsplash.com/photo-1567532939604-b6b5b0db2604?q=80&w=600&auto=format&fit=crop",
            voiceRecommendation = "Bella - Warm Professional (US)",
            description = "Fintech product reviews, investment portfolio breakdowns, crypto analysis."
        ),
        AvatarOption(
            id = "kenji",
            name = "Kenji Takahashi",
            role = "Esports & Gaming Host",
            category = "Gaming",
            imageUrl = "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?q=80&w=600&auto=format&fit=crop",
            voiceRecommendation = "Kai - Smooth Australian (AU)",
            description = "Livestream recaps, gaming hardware benchmarks, interactive tournaments."
        )
    )

    val availableAvatars = MutableStateFlow<List<AvatarOption>>(defaultAvatars)
    val selectedAvatarId = MutableStateFlow("elena")

    fun selectAvatar(avatar: AvatarOption) {
        selectedAvatarId.value = avatar.id
        avatarName.value = "${avatar.name} (${avatar.category})"
        avatarVoice.value = avatar.voiceRecommendation
    }

    fun setCustomAvatarImage(uriString: String) {
        avatarCustomImageUri.value = uriString
        val customOption = AvatarOption(
            id = "custom_${System.currentTimeMillis()}",
            name = "My Custom Clone",
            role = "User Face Model (Neural Sync)",
            category = "Custom Face",
            imageUrl = uriString,
            voiceRecommendation = avatarVoice.value,
            description = "Custom face photo uploaded from device gallery for neural talking avatar articulation.",
            isCustom = true
        )
        availableAvatars.value = listOf(customOption) + defaultAvatars
        selectedAvatarId.value = customOption.id
        avatarName.value = "${customOption.name} (Custom Neural)"
    }

    // Platform Monetization Economics (Showing Owner Profit Margins)
    val monetizationBreakdown = listOf(
        MonetizationTier("AI Video (10s 4K Ultra HD)", creditCost = 90, retailUsdValue = 9.00, estimatedComputeCostUsd = 0.22, profitMarginPercent = 97.5, "4K Neural Frame Rendering + Claude AI Script"),
        MonetizationTier("AI Video (10s 1080p HD)", creditCost = 45, retailUsdValue = 4.50, estimatedComputeCostUsd = 0.12, profitMarginPercent = 97.3, "Full HD Scene Assembly + Script"),
        MonetizationTier("Image-to-Video Synthesis", creditCost = 25, retailUsdValue = 2.50, estimatedComputeCostUsd = 0.08, profitMarginPercent = 96.8, "Camera Motion Interpolation & Upscaling"),
        MonetizationTier("UGC Viral Video", creditCost = 50, retailUsdValue = 5.00, estimatedComputeCostUsd = 0.14, profitMarginPercent = 97.2, "Viral Hook + Actor Persona + Product Sync"),
        MonetizationTier("Talking Neural Avatar", creditCost = 75, retailUsdValue = 7.50, estimatedComputeCostUsd = 0.18, profitMarginPercent = 97.6, "Photorealistic Lip-Sync & Face Articulation")
    )

    // ============================================================
    // BACKEND SETTINGS, GEMINI API KEY & CRASHLYTICS / STORAGE STATE
    // ============================================================
    private val backendPrefs by lazy {
        application.getSharedPreferences("gee_ai_backend_config", Context.MODE_PRIVATE)
    }

    val geminiApiKey = MutableStateFlow(
        backendPrefs.getString("gemini_api_key", "") ?: ""
    )
    val isGeminiKeyValid = MutableStateFlow<Boolean?>(null)
    val geminiKeyTestMessage = MutableStateFlow("")
    val isTestingGemini = MutableStateFlow(false)

    val customStorageBucket = MutableStateFlow(
        backendPrefs.getString("storage_bucket", firebaseManager.customStorageBucket) ?: firebaseManager.customStorageBucket
    )
    val crashlyticsTestStatus = MutableStateFlow<String>("")

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    // Recommended Items Hosted on Firebase Storage
    val recommendedStorageItems = MutableStateFlow<List<StorageRecommendedItem>>(
        listOf(
            StorageRecommendedItem(
                id = "storage_ugc_01",
                title = "Viral Skincare Unboxing Hook",
                category = "UGC Ad Template",
                storagePath = "templates/ugc_skincare_01.mp4",
                directUrl = firebaseManager.getVideoShareUrl("storage_ugc_01", "Viral Skincare Unboxing Hook"),
                thumbnailUrl = "https://images.unsplash.com/photo-1556228720-195a672e8a03?q=80&w=600&auto=format&fit=crop",
                durationText = "0:15s",
                fileSizeMb = 4.2,
                downloadsCount = 1420,
                description = "High-retention UGC customer review format with dynamic b-roll overlays and hook caption."
            ),
            StorageRecommendedItem(
                id = "storage_avatar_01",
                title = "4K Corporate Studio Anchor",
                category = "Neural Avatar Preset",
                storagePath = "avatars/studio_anchor_4k.mp4",
                directUrl = firebaseManager.getVideoShareUrl("storage_avatar_01", "4K Corporate Studio Anchor"),
                thumbnailUrl = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?q=80&w=600&auto=format&fit=crop",
                durationText = "0:30s",
                fileSizeMb = 12.8,
                downloadsCount = 980,
                description = "Photorealistic digital presenter with natural gestures and broadcast-grade lip sync."
            ),
            StorageRecommendedItem(
                id = "storage_short_01",
                title = "Cyberpunk Neo-Tokyo Cinematic",
                category = "Image-to-Video Preset",
                storagePath = "presets/cyberpunk_tokyo.mp4",
                directUrl = firebaseManager.getVideoShareUrl("storage_short_01", "Cyberpunk Neo-Tokyo Cinematic"),
                thumbnailUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?q=80&w=600&auto=format&fit=crop",
                durationText = "0:10s",
                fileSizeMb = 3.6,
                downloadsCount = 2850,
                description = "Dynamic drone zoom-in motion with volumetric neon lighting and rain reflections."
            ),
            StorageRecommendedItem(
                id = "storage_tech_01",
                title = "SaaS Product Demo Walkthrough",
                category = "UGC Tech Commercial",
                storagePath = "templates/saas_product_demo.mp4",
                directUrl = firebaseManager.getVideoShareUrl("storage_tech_01", "SaaS Product Demo Walkthrough"),
                thumbnailUrl = "https://images.unsplash.com/photo-1460925895917-afdab827c52f?q=80&w=600&auto=format&fit=crop",
                durationText = "0:20s",
                fileSizeMb = 6.4,
                downloadsCount = 1130,
                description = "Feature highlight split-screen with animated cursor tracking and callout pointers."
            ),
            StorageRecommendedItem(
                id = "storage_fitness_01",
                title = "High-Energy Workout Motivation",
                category = "Viral Shorts Template",
                storagePath = "templates/fitness_hype.mp4",
                directUrl = firebaseManager.getVideoShareUrl("storage_fitness_01", "High-Energy Workout Motivation"),
                thumbnailUrl = "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?q=80&w=600&auto=format&fit=crop",
                durationText = "0:12s",
                fileSizeMb = 5.1,
                downloadsCount = 3400,
                description = "Fast-paced rhythmic beat-cuts designed for Instagram Reels & TikTok viral trends."
            ),
            StorageRecommendedItem(
                id = "storage_luxury_01",
                title = "Luxury Watch Macro Slow-Motion",
                category = "Commercial B-Roll",
                storagePath = "broll/luxury_watch_macro.mp4",
                directUrl = firebaseManager.getVideoShareUrl("storage_luxury_01", "Luxury Watch Macro Slow-Motion"),
                thumbnailUrl = "https://images.unsplash.com/photo-1523275335684-37898b6baf30?q=80&w=600&auto=format&fit=crop",
                durationText = "0:08s",
                fileSizeMb = 7.5,
                downloadsCount = 890,
                description = "Macro lens turntable rotation with optical sapphire crystal reflections."
            )
        )
    )

    fun saveGeminiApiKey(key: String) {
        val trimmed = key.trim()
        geminiApiKey.value = trimmed
        backendPrefs.edit().putString("gemini_api_key", trimmed).apply()
        firebaseManager.setCrashlyticsCustomKey("has_gemini_api_key", (trimmed.isNotBlank()).toString())
        firebaseManager.logMessage("User updated Gemini API Key in Backend Settings")
        isGeminiKeyValid.value = null
        geminiKeyTestMessage.value = "API Key saved securely in backend storage."
    }

    fun saveStorageBucket(bucket: String) {
        val trimmed = bucket.trim().removePrefix("gs://").removeSuffix("/")
        if (trimmed.isNotBlank()) {
            customStorageBucket.value = trimmed
            backendPrefs.edit().putString("storage_bucket", trimmed).apply()
            firebaseManager.updateStorageBucket(trimmed)
            // Refresh recommended items direct URLs with new bucket
            recommendedStorageItems.value = recommendedStorageItems.value.map { item ->
                item.copy(directUrl = firebaseManager.getVideoShareUrl(item.id, item.title))
            }
        }
    }

    fun testGeminiApiKey() {
        val key = geminiApiKey.value.trim()
        if (key.isBlank()) {
            isGeminiKeyValid.value = false
            geminiKeyTestMessage.value = "Please enter a valid Gemini API key first."
            return
        }

        viewModelScope.launch {
            isTestingGemini.value = true
            geminiKeyTestMessage.value = "Connecting to Google AI Studio Gemini API..."
            withContext(Dispatchers.IO) {
                try {
                    val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$key"
                    val requestBodyJson = JSONObject().apply {
                        put("contents", JSONArray().apply {
                            put(JSONObject().apply {
                                put("parts", JSONArray().apply {
                                    put(JSONObject().apply {
                                        put("text", "Respond with 'OK: Gemini Video Neural Engine connected successfully.'")
                                    })
                                })
                            })
                        })
                    }

                    val request = Request.Builder()
                        .url(url)
                        .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
                        .build()

                    val startTime = System.currentTimeMillis()
                    val response = httpClient.newCall(request).execute()
                    val latencyMs = System.currentTimeMillis() - startTime
                    val responseBody = response.body?.string() ?: ""

                    if (response.isSuccessful) {
                        isGeminiKeyValid.value = true
                        geminiKeyTestMessage.value = "Verified! Google Gemini 2.5 Flash connected successfully (${latencyMs}ms latency)."
                        firebaseManager.logMessage("Gemini API Key verification succeeded ($latencyMs ms)")
                        firebaseManager.setCrashlyticsCustomKey("gemini_api_status", "CONNECTED")
                    } else {
                        isGeminiKeyValid.value = false
                        val errorDetail = try {
                            JSONObject(responseBody).optJSONObject("error")?.optString("message") ?: response.message
                        } catch (e: Exception) {
                            response.message
                        }
                        geminiKeyTestMessage.value = "Verification failed (HTTP ${response.code}): $errorDetail"
                        firebaseManager.logMessage("Gemini API Key verification failed: HTTP ${response.code}")
                    }
                } catch (e: Exception) {
                    isGeminiKeyValid.value = false
                    geminiKeyTestMessage.value = "Connection error: ${e.localizedMessage ?: e.message}"
                    firebaseManager.logException(e, "Gemini API test connection exception")
                } finally {
                    isTestingGemini.value = false
                }
            }
        }
    }

    fun triggerCrashlyticsTest() {
        val success = firebaseManager.triggerTestCrashlyticsReport()
        crashlyticsTestStatus.value = if (success) {
            "Diagnostic test report sent! Check your Firebase Console -> Crashlytics."
        } else {
            "Failed to send Crashlytics test report. Ensure network connectivity."
        }
    }

    /**
     * Copy public Firebase Storage link for any video to system clipboard
     */
    fun copyVideoLink(context: Context, videoId: String, videoTitle: String = "GEE AI Video", onCopied: () -> Unit = {}) {
        val url = firebaseManager.getVideoShareUrl(videoId, videoTitle)
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText("GEE AI Video Link", url)
        clipboard?.setPrimaryClip(clip)
        Toast.makeText(context, "Video link copied to clipboard!\n$url", Toast.LENGTH_SHORT).show()
        firebaseManager.logMessage("User copied video link: $videoId")
        onCopied()
    }

    /**
     * Share public Firebase Storage link for any video via Android system share sheet
     */
    fun shareVideoLink(context: Context, videoId: String, videoTitle: String = "GEE AI Video") {
        val url = firebaseManager.getVideoShareUrl(videoId, videoTitle)
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Watch my AI-generated video created with GEE AI: $videoTitle\n$url")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share GEE AI Video with Others")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
        firebaseManager.logMessage("User shared video link: $videoId")
    }

    fun uploadMusic(name: String) {
        if (name.isNotBlank()) {
            uploadedMusic.value = uploadedMusic.value + name
        }
    }

    fun uploadLogo(name: String) {
        if (name.isNotBlank()) {
            uploadedLogos.value = uploadedLogos.value + name
        }
    }

    fun updateBrandSettings(name: String, position: String) {
        brandNameText.value = name
        brandPosition.value = position
    }

    // Billing and Invoices Flow
    val allInvoices: StateFlow<List<Invoice>> = invoiceDao.getAllInvoicesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin states
    val adminMockUsers = MutableStateFlow<List<UserProfile>>(emptyList())
    val adminRevenue = MutableStateFlow("$12,450.00")
    val adminApiUsage = MutableStateFlow(1850) // API calls made

    init {
        // Prepare seed admin users and seed data
        viewModelScope.launch {
            val existing = userProfileDao.getUserProfile()
            if (existing == null) {
                userProfileDao.insertUserProfile(
                    UserProfile(
                        email = "user@geeai.com",
                        name = "Alex Creator",
                        role = "User",
                        creditsRemaining = 850,
                        storageUsedMb = 256.4,
                        subscriptionTier = "Pro",
                        videosGeneratedCount = 14
                    )
                )
            }
            loadAdminUsers()
        }
    }

    private fun loadAdminUsers() {
        adminMockUsers.value = listOf(
            UserProfile("user@geeai.com", "Alex Creator", "User", 850, 256.4, "Pro", 14),
            UserProfile("admin@geeai.com", "Main Administrator", "Admin", 99999, 1024.5, "Business", 85),
            UserProfile("sarah@company.com", "Sarah Jenkins", "User", 150, 12.8, "Starter", 2),
            UserProfile("mike.h@design.io", "Michael Hudson", "User", 0, 512.0, "Free", 1)
        )
    }

    // Auth functions
    fun login(email: String, role: String) {
        if (email.isBlank()) {
            _loginError.value = "Please enter a valid email address."
            return
        }
        viewModelScope.launch {
            _currentEmail.value = email
            _currentRole.value = role
            _loginError.value = null

            // Update user profile record in local room
            val existing = userProfileDao.getUserProfile()
            val newProfile = UserProfile(
                email = email,
                name = email.split("@").first().replaceFirstChar { it.uppercase() } + " Creator",
                role = role,
                creditsRemaining = existing?.creditsRemaining ?: 1000,
                storageUsedMb = existing?.storageUsedMb ?: 120.0,
                subscriptionTier = existing?.subscriptionTier ?: "Pro",
                videosGeneratedCount = existing?.videosGeneratedCount ?: 5
            )
            userProfileDao.insertUserProfile(newProfile)
            _currentScreen.value = Screen.DASHBOARD
        }
    }

    /**
     * Interactive Google Sign-In with Jetpack Credential Manager & Firebase
     */
    fun signInWithGoogle(activity: Activity) {
        isGoogleSigningIn.value = true
        googleSignInError.value = null
        val credentialManager = CredentialManager.create(activity)
        firebaseManager.signInWithGoogle(
            activity = activity,
            credentialManager = credentialManager,
            scope = viewModelScope,
            onSuccess = { user ->
                isGoogleSigningIn.value = false
                _currentEmail.value = user.email ?: "google_user@gmail.com"
                _currentRole.value = "User"
                _loginError.value = null
                
                // Load or sync profile from Firestore into local Room
                viewModelScope.launch {
                    val synced = firebaseManager.syncUserProfileOnSignIn(user)
                    userProfileDao.insertUserProfile(synced)
                    firebaseManager.attachProfileListener(user.uid) { liveProfile ->
                        viewModelScope.launch {
                            userProfileDao.insertUserProfile(liveProfile)
                        }
                    }
                }
                _currentScreen.value = Screen.DASHBOARD
            },
            onError = { err ->
                isGoogleSigningIn.value = false
                googleSignInError.value = err
            },
            onCancelled = {
                isGoogleSigningIn.value = false
            }
        )
    }

    fun logout() {
        firebaseManager.detachListeners()
        _currentScreen.value = Screen.LOGIN
        _currentEmail.value = ""
        _activeProject.value = null
    }

    fun setScreen(screen: Screen) {
        _currentScreen.value = screen
    }

    // Video Wizard generation
    fun generateScript() {
        viewModelScope.launch {
            isGeneratingScript.value = true
            val script = scriptWriter.generateScript(
                topic = wizardTopic.value,
                prompt = wizardPrompt.value,
                audience = wizardAudience.value,
                length = "${wizardDurationSeconds.value} seconds (Max 10s)",
                language = wizardLanguage.value,
                tone = wizardTone.value,
                style = wizardStyle.value
            )
            wizardScript.value = script
            isGeneratingScript.value = false
        }
    }

    // Confirmation step before creating video
    fun requestCreateVideoConfirmation() {
        showPlatformConfirmationModal.value = true
    }

    fun dismissPlatformConfirmation() {
        showPlatformConfirmationModal.value = false
    }

    // Scene builder conversion with token deduction
    fun buildScenesAndProject() {
        showPlatformConfirmationModal.value = false
        viewModelScope.launch {
            val projId = UUID.randomUUID().toString()
            val scriptText = wizardScript.value
            val chosenSeconds = wizardDurationSeconds.value.coerceIn(1, 10)
            val chosenQuality = wizardQualityTier.value
            val selectedPlatformsList = wizardTargetPlatforms.value.toList()

            val project = Project(
                id = projId,
                name = wizardTopic.value.ifBlank { "AI Video (${chosenSeconds}s - $chosenQuality)" },
                topic = wizardTopic.value,
                prompt = wizardPrompt.value,
                targetAudience = wizardAudience.value,
                lengthType = "${chosenSeconds}s (10s max)",
                language = wizardLanguage.value,
                tone = wizardTone.value,
                style = wizardStyle.value,
                presenter = wizardPresenter.value,
                voice = wizardVoice.value,
                accent = wizardAccent.value,
                backgroundMusic = wizardMusic.value,
                videoFormat = wizardFormat.value,
                resolution = chosenQuality,
                script = scriptText,
                createdAt = System.currentTimeMillis(),
                status = "Draft"
            )

            // Auto split script text into scenes matching duration up to 10 seconds
            val paragraphs = scriptText.split(Regex("\n\n+")).filter { it.isNotBlank() }
            val sentences = if (paragraphs.size <= 1) {
                scriptText.split(Regex("(?<=[.!?])\\s+")).filter { it.isNotBlank() }
            } else paragraphs

            val sceneCount = if (sentences.isNotEmpty()) minOf(sentences.size, 3) else 2
            val durationPerScene = maxOf(2, chosenSeconds / sceneCount)

            val sceneList = (0 until sceneCount).map { idx ->
                val narrationText = sentences.getOrNull(idx) ?: "Visual scene illustrating ${project.topic}"
                val visualDesc = when (idx) {
                    0 -> "Opening viral hook for ${project.topic}, high fidelity ${project.style} visual, platforms: ${selectedPlatformsList.joinToString()}"
                    1 -> "Engaging core insight on ${project.topic}, dynamic angles, vibrant lighting, optimized for social playback"
                    else -> "Call to action and conclusion, cinematic motion, ultra sharp graphics"
                }
                Scene(
                    id = "$projId-scene-$idx",
                    projectId = projId,
                    sceneIndex = idx,
                    narration = narrationText.trim(),
                    visualPrompt = visualDesc,
                    durationSeconds = if (idx == sceneCount - 1) chosenSeconds - (durationPerScene * (sceneCount - 1)) else durationPerScene,
                    transition = if (idx == 0) "None" else "Cross Dissolve",
                    cameraDirection = when (idx % 3) {
                        0 -> "Slow Zoom In"
                        1 -> "Pan Right"
                        else -> "Static Focus"
                    },
                    captionText = narrationText.trim().take(60) + "...",
                    imageUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?q=80&w=600&auto=format&fit=crop",
                    status = "Pending"
                )
            }

            // Calculate Token Deduction based on quality & 10s duration
            val tokenBreakdown = calculateTokenDeduction(chosenSeconds, chosenQuality)
            val currentCredits = userProfile.value?.creditsRemaining ?: 1000

            // Record generation history
            val generationRecord = GenerationRecord(
                id = UUID.randomUUID().toString(),
                projectId = projId,
                videoTitle = project.name,
                durationSeconds = chosenSeconds,
                quality = chosenQuality,
                tokensDeducted = tokenBreakdown.totalTokens,
                platforms = selectedPlatformsList,
                createdAt = System.currentTimeMillis()
            )

            // Deduct tokens in Firestore if authenticated
            val authUser = firebaseManager.currentUser
            if (authUser != null) {
                firebaseManager.deductTokensAndRecordGeneration(
                    userId = authUser.uid,
                    currentCredits = currentCredits,
                    tokenBreakdown = tokenBreakdown,
                    record = generationRecord
                )
            }

            projectDao.insertProject(project)
            sceneDao.insertScenes(sceneList)

            _activeProject.value = project
            _activeScenes.value = sceneList

            // Deduct in local database
            deductCredits(tokenBreakdown.totalTokens)

            // Direct to Scene Builder
            _currentScreen.value = Screen.SCENE_BUILDER
        }
    }

    // Edit and Manage Active Project Scenes
    fun selectProjectForEditing(project: Project) {
        viewModelScope.launch {
            _activeProject.value = project
            val scenes = sceneDao.getScenesForProject(project.id)
            _activeScenes.value = scenes
            _currentScreen.value = Screen.VIDEO_EDITOR
        }
    }

    fun updateSceneDetail(scene: Scene) {
        viewModelScope.launch {
            sceneDao.updateScene(scene)
            // Reload
            _activeProject.value?.let { proj ->
                _activeScenes.value = sceneDao.getScenesForProject(proj.id)
            }
        }
    }

    fun reorderScenes(fromIndex: Int, toIndex: Int) {
        val list = _activeScenes.value.toMutableList()
        if (fromIndex in list.indices && toIndex in list.indices) {
            val moved = list.removeAt(fromIndex)
            list.add(toIndex, moved)
            // Re-index
            val updated = list.mapIndexed { index, s -> s.copy(sceneIndex = index) }
            _activeScenes.value = updated
            viewModelScope.launch {
                sceneDao.insertScenes(updated)
            }
        }
    }

    fun deleteScene(scene: Scene) {
        viewModelScope.launch {
            sceneDao.deleteSceneById(scene.id)
            _activeProject.value?.let { proj ->
                val remaining = sceneDao.getScenesForProject(proj.id)
                // re-index remaining
                val updated = remaining.mapIndexed { index, s -> s.copy(sceneIndex = index) }
                sceneDao.insertScenes(updated)
                _activeScenes.value = updated
            }
        }
    }

    fun addSceneToEnd() {
        _activeProject.value?.let { proj ->
            val index = _activeScenes.value.size
            val newScene = Scene(
                id = "${proj.id}-scene-$index-${System.currentTimeMillis() % 1000}",
                projectId = proj.id,
                sceneIndex = index,
                narration = "Add new narration narration here...",
                visualPrompt = "Close-up cinematic modern illustration of details",
                durationSeconds = 10,
                transition = "Cross Dissolve",
                cameraDirection = "Slow Zoom In",
                captionText = "Subtitles text",
                imageUrl = "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?q=80&w=600&auto=format&fit=crop",
                status = "Pending"
            )
            viewModelScope.launch {
                sceneDao.insertScene(newScene)
                _activeScenes.value = sceneDao.getScenesForProject(proj.id)
            }
        }
    }

    // Generate visuals for all scenes (AI Video Studio simulation)
    fun generateAllVisuals() {
        viewModelScope.launch {
            val currentList = _activeScenes.value
            currentList.forEach { s ->
                sceneProgressMap.value = sceneProgressMap.value + (s.id to 0)
                viewModelScope.launch {
                    val url = videoStudio.generateSceneVisual(s.visualPrompt, _activeProject.value?.style ?: "Cinematic") { progress ->
                        sceneProgressMap.value = sceneProgressMap.value + (s.id to progress)
                    }
                    val updatedScene = s.copy(imageUrl = url, status = "Ready")
                    sceneDao.updateScene(updatedScene)
                    // Update active list state
                    _activeScenes.value = _activeScenes.value.map { if (it.id == s.id) updatedScene else it }
                    deductCredits(60)
                }
            }
        }
    }

    // Generate single scene visual
    fun generateSingleVisual(scene: Scene) {
        viewModelScope.launch {
            sceneProgressMap.value = sceneProgressMap.value + (scene.id to 0)
            val url = videoStudio.generateSceneVisual(scene.visualPrompt, _activeProject.value?.style ?: "Cinematic") { progress ->
                sceneProgressMap.value = sceneProgressMap.value + (scene.id to progress)
            }
            val updatedScene = scene.copy(imageUrl = url, status = "Ready")
            sceneDao.updateScene(updatedScene)
            _activeScenes.value = _activeScenes.value.map { if (it.id == scene.id) updatedScene else it }
            deductCredits(60)
        }
    }

    // Generate Voice studio speech
    fun generateVoiceStudioNarration() {
        viewModelScope.launch {
            isGeneratingVoice.value = true
            val path = voiceStudio.generateSpeech(
                text = voiceText.value,
                gender = voiceGender.value,
                speed = voiceSpeed.value,
                pitch = voicePitch.value,
                emotion = voiceEmotion.value,
                accent = voiceAccent.value
            )
            generatedAudioPath.value = path
            isGeneratingVoice.value = false
            deductCredits(10)
        }
    }

    // Image to video generation simulation
    fun generateImageToVideo() {
        viewModelScope.launch {
            isGeneratingImageToVideo.value = true
            delay(3000) // Latency simulation
            isGeneratingImageToVideo.value = false

            val durationSeconds = imgToVideoDuration.value.filter { it.isDigit() }.toIntOrNull() ?: 6
            val baseCost = if (durationSeconds in 1..7) 60 else 20
            val musicCost = if (imgToVideoHasMusic.value) 10 else 0
            val finalCost = baseCost + musicCost
            deductCredits(finalCost)

            // Create a mock completed image-to-video project
            val projId = UUID.randomUUID().toString()
            val newProj = Project(
                id = projId,
                name = "Image-to-Video project",
                topic = "Image prompt to Motion",
                prompt = "Animating input image",
                targetAudience = "Social Media",
                lengthType = "6s-1m",
                language = "English",
                tone = "Dynamic",
                style = "Cinematic",
                presenter = "None",
                voice = "None",
                accent = "None",
                backgroundMusic = "None",
                videoFormat = "9:16",
                resolution = "1080p",
                script = "Voiceover description from static visuals",
                createdAt = System.currentTimeMillis(),
                status = "Completed"
            )
            projectDao.insertProject(newProj)
            sceneDao.insertScene(
                Scene(
                    id = "$projId-scene-0",
                    projectId = projId,
                    sceneIndex = 0,
                    narration = "Animated camera movement applied.",
                    visualPrompt = "Original image with movement: " + imgToVideoMotion.value,
                    durationSeconds = 6,
                    transition = "None",
                    cameraDirection = imgToVideoMotion.value,
                    captionText = "Visual motion dynamic render",
                    imageUrl = imgToVideoImageUri.value.ifBlank { "https://images.unsplash.com/photo-1626544827763-d516dce335e2?q=80&w=600&auto=format&fit=crop" },
                    status = "Ready"
                )
            )
            _activeProject.value = newProj
            _activeScenes.value = sceneDao.getScenesForProject(projId)
            _currentScreen.value = Screen.DASHBOARD
        }
    }

    // UGC Video Generation
    fun generateUgcVideo() {
        viewModelScope.launch {
            isGeneratingUgc.value = true
            delay(3200) // Realistic synthesis latency

            val format = if (ugcPlatform.value.contains("1:1")) "1:1" else if (ugcPlatform.value.contains("16:9")) "16:9" else "9:16"
            val projId = UUID.randomUUID().toString()
            val scriptContent = """
                [Hook]: ${ugcHookStyle.value}
                [Story]: I've been testing ${ugcProductName.value} for the past few weeks and the results are unbelievable. ${ugcKeyFeatures.value}.
                [Demo]: Notice how effortlessly it blends and absorbs into the skin without any greasy residue.
                [CTA]: ${ugcCallToAction.value}
            """.trimIndent()

            ugcGeneratedScript.value = scriptContent
            ugcRenderedVideoUrl.value = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=800&auto=format&fit=crop"

            val newProj = Project(
                id = projId,
                name = "UGC: ${ugcProductName.value}",
                topic = "UGC Video - ${ugcStyle.value}",
                prompt = "Persona: ${ugcCreatorPersona.value}, Style: ${ugcStyle.value}, Product: ${ugcProductName.value}",
                targetAudience = ugcCreatorPersona.value,
                lengthType = "6s-1m",
                language = "English",
                tone = "Authentic & Conversational",
                style = "UGC Social Viral",
                presenter = ugcCreatorPersona.value,
                voice = "Natural Conversational",
                accent = "US American",
                backgroundMusic = "Upbeat Lofi Trend",
                videoFormat = format,
                resolution = "1080p",
                script = scriptContent,
                createdAt = System.currentTimeMillis(),
                status = "Completed"
            )
            projectDao.insertProject(newProj)
            sceneDao.insertScene(
                Scene(
                    id = "$projId-scene-0",
                    projectId = projId,
                    sceneIndex = 0,
                    narration = ugcHookStyle.value,
                    visualPrompt = "Dynamic creator authentic camera shot showcasing product",
                    durationSeconds = 8,
                    transition = "Jump Cut",
                    cameraDirection = "Handheld Natural",
                    captionText = ugcHookStyle.value,
                    imageUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=800&auto=format&fit=crop",
                    status = "Ready"
                )
            )
            deductCredits(45)
            isGeneratingUgc.value = false
        }
    }

    // Avatar Video Studio Generation
    fun generateAvatarVideo() {
        viewModelScope.launch {
            isGeneratingAvatar.value = true
            delay(3500) // Neural rendering latency

            val projId = UUID.randomUUID().toString()
            avatarRenderedVideoUrl.value = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?q=80&w=800&auto=format&fit=crop"

            val newProj = Project(
                id = projId,
                name = "Avatar: ${avatarName.value}",
                topic = "AI Avatar Presentation",
                prompt = "Avatar: ${avatarName.value}, Framing: ${avatarFraming.value}, Emotion: ${avatarEmotion.value}, Backdrop: ${avatarBackdrop.value}",
                targetAudience = "Corporate & Professional Audience",
                lengthType = "1m-5m",
                language = "English",
                tone = avatarEmotion.value,
                style = "Hyper-Realistic Digital Avatar",
                presenter = avatarName.value,
                voice = avatarVoice.value,
                accent = "Standard Accent",
                backgroundMusic = "Corporate Minimal",
                videoFormat = "16:9",
                resolution = "4K",
                script = avatarScript.value,
                createdAt = System.currentTimeMillis(),
                status = "Completed"
            )
            projectDao.insertProject(newProj)
            sceneDao.insertScene(
                Scene(
                    id = "$projId-scene-0",
                    projectId = projId,
                    sceneIndex = 0,
                    narration = avatarScript.value,
                    visualPrompt = "${avatarName.value} delivering presentation in ${avatarBackdrop.value}",
                    durationSeconds = 15,
                    transition = "Smooth Fade",
                    cameraDirection = avatarFraming.value,
                    captionText = avatarScript.value.take(60),
                    imageUrl = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?q=80&w=800&auto=format&fit=crop",
                    status = "Ready"
                )
            )
            deductCredits(75)
            isGeneratingAvatar.value = false
        }
    }

    // Thumbnail Generation
    fun generateVideoThumbnails() {
        viewModelScope.launch {
            isGeneratingThumbnails.value = true
            _activeProject.value?.let { proj ->
                val list = thumbnailService.generateThumbnails(proj.topic, proj.style)
                thumbnailIdeas.value = list
            }
            isGeneratingThumbnails.value = false
            deductCredits(8)
        }
    }

    fun selectThumbnail(id: String) {
        thumbnailIdeas.value = thumbnailIdeas.value.map {
            it.copy(isSelected = (it.id == id))
        }
    }

    // SEO Generation
    fun generateVideoSeo() {
        viewModelScope.launch {
            isGeneratingSeo.value = true
            _activeProject.value?.let { proj ->
                val seo = seoService.generateSeoData(proj.topic, proj.prompt, proj.script)
                seoData.value = seo
            }
            isGeneratingSeo.value = false
            deductCredits(4)
        }
    }

    // Export Rendering Simulation
    fun triggerVideoRender() {
        viewModelScope.launch {
            isRenderingVideo.value = true
            activeRenderProgress.value = 0
            _activeProject.value?.let { proj ->
                val path = videoRenderer.renderVideo(
                    projectId = proj.id,
                    resolution = proj.resolution,
                    format = proj.videoFormat
                ) { progress ->
                    activeRenderProgress.value = progress
                }
                renderedFilePath.value = path
                // Mark project as Completed
                val completedProj = proj.copy(status = "Completed")
                projectDao.insertProject(completedProj)
                _activeProject.value = completedProj

                // Increment generated videos count in profile
                userProfile.value?.let { prof ->
                    userProfileDao.updateUserProfile(
                        prof.copy(
                            videosGeneratedCount = prof.videosGeneratedCount + 1,
                            storageUsedMb = prof.storageUsedMb + when (proj.resolution) {
                                "4K" -> 240.0
                                "1080p" -> 85.0
                                else -> 45.0
                            }
                        )
                    )
                }
            }
            isRenderingVideo.value = false
            deductCredits(25)
        }
    }

    // Project Deletion
    fun deleteProject(project: Project) {
        viewModelScope.launch {
            projectDao.deleteProjectById(project.id)
            sceneDao.deleteScenesForProject(project.id)
            if (_activeProject.value?.id == project.id) {
                _activeProject.value = null
                _activeScenes.value = emptyList()
            }
        }
    }

    // Duplicate project
    fun duplicateProject(project: Project) {
        viewModelScope.launch {
            val newId = UUID.randomUUID().toString()
            val duplicatedProj = project.copy(
                id = newId,
                name = "${project.name} (Copy)",
                createdAt = System.currentTimeMillis()
            )
            projectDao.insertProject(duplicatedProj)

            val originalScenes = sceneDao.getScenesForProject(project.id)
            val duplicatedScenes = originalScenes.map { s ->
                s.copy(
                    id = "$newId-scene-${s.sceneIndex}",
                    projectId = newId
                )
            }
            sceneDao.insertScenes(duplicatedScenes)
        }
    }

    // Billing & Credits purchase
    fun selectSubscriptionTier(tier: String, isYearly: Boolean = false) {
        viewModelScope.launch {
            userProfile.value?.let { prof ->
                val invoice = paymentService.processSubscription(prof.email, tier, isYearly)
                invoiceDao.insertInvoice(invoice)

                val updatedTier = if (isYearly && tier != "Free") "$tier (Yearly)" else tier
                userProfileDao.updateUserProfile(
                    prof.copy(
                        subscriptionTier = updatedTier,
                        creditsRemaining = prof.creditsRemaining + invoice.creditsAdded
                    )
                )
            }
        }
    }

    fun launchPlayBillingPurchase(activity: Activity, tier: String, isYearly: Boolean = false) {
        val cycle = if (isYearly) "yearly" else "monthly"
        val productId = "${tier.lowercase()}_$cycle"
        playBillingManager.launchPurchaseFlow(activity, productId, isYearly) {
            selectSubscriptionTier(tier, isYearly)
        }
    }

    fun awardAdBonusCredits(credits: Int = 50) {
        viewModelScope.launch {
            userProfile.value?.let { prof ->
                val invoiceId = "AD-REWARD-${System.currentTimeMillis() % 1000000}"
                val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
                val invoice = Invoice(
                    id = invoiceId,
                    email = prof.email,
                    dateString = dateStr,
                    amount = "$0.00 (AdMob Reward)",
                    creditsAdded = credits,
                    planName = "AdMob Sponsored Ad Reward",
                    status = "Paid"
                )
                invoiceDao.insertInvoice(invoice)
                userProfileDao.updateUserProfile(
                    prof.copy(creditsRemaining = prof.creditsRemaining + credits)
                )
            }
        }
    }

    fun buyCreditsPackage(amountUsd: Double, creditsCount: Int) {
        viewModelScope.launch {
            userProfile.value?.let { prof ->
                val invoiceId = "CRD-${System.currentTimeMillis() % 1000000}"
                val newInvoice = Invoice(
                    id = invoiceId,
                    email = prof.email,
                    dateString = "2026-07-03",
                    amount = "$$amountUsd",
                    creditsAdded = creditsCount,
                    planName = "Credits top-up",
                    status = "Paid"
                )
                invoiceDao.insertInvoice(newInvoice)
                userProfileDao.updateUserProfile(
                    prof.copy(creditsRemaining = prof.creditsRemaining + creditsCount)
                )
            }
        }
    }

    // Helper to deduct credits and sync to Firestore
    private suspend fun deductCredits(amount: Int) {
        userProfile.value?.let { prof ->
            val remaining = (prof.creditsRemaining - amount).coerceAtLeast(0)
            userProfileDao.updateUserProfile(prof.copy(creditsRemaining = remaining))

            // Sync with Firebase Firestore if user is authenticated
            val currentFbUser = firebaseManager.currentUser
            if (currentFbUser != null) {
                try {
                    val breakdown = TokenBreakdown(
                        scriptClaudeTokens = (amount * 0.35).toInt(),
                        renderComputeTokens = (amount * 0.65).toInt(),
                        totalTokens = amount,
                        durationSeconds = wizardDurationSeconds.value,
                        quality = wizardQualityTier.value
                    )
                    val rec = GenerationRecord(
                        id = UUID.randomUUID().toString(),
                        videoTitle = "GEE AI Video Generation",
                        durationSeconds = wizardDurationSeconds.value,
                        quality = wizardQualityTier.value,
                        tokensDeducted = amount,
                        platforms = wizardTargetPlatforms.value.toList()
                    )
                    firebaseManager.deductTokensAndRecordGeneration(
                        userId = currentFbUser.uid,
                        currentCredits = prof.creditsRemaining,
                        tokenBreakdown = breakdown,
                        record = rec
                    )
                } catch (e: Exception) {
                    android.util.Log.e("FirebaseDeduct", "Failed to deduct in Firestore: ${e.message}")
                }
            }
        }
    }

    // Admin commands
    fun adminAdjustCredits(email: String, newAmount: Int) {
        viewModelScope.launch {
            if (email == userProfile.value?.email) {
                userProfile.value?.let { prof ->
                    userProfileDao.updateUserProfile(prof.copy(creditsRemaining = newAmount))
                }
            }
            adminMockUsers.value = adminMockUsers.value.map {
                if (it.email == email) it.copy(creditsRemaining = newAmount) else it
            }
        }
    }

    fun adminSuspendAccount(email: String) {
        adminMockUsers.value = adminMockUsers.value.map {
            if (it.email == email) it.copy(subscriptionTier = "Suspended") else it
        }
    }

    fun adminAdjustSubscription(email: String, tier: String) {
        viewModelScope.launch {
            if (email == userProfile.value?.email) {
                userProfile.value?.let { prof ->
                    userProfileDao.updateUserProfile(prof.copy(subscriptionTier = tier))
                }
            }
            adminMockUsers.value = adminMockUsers.value.map {
                if (it.email == email) it.copy(subscriptionTier = tier) else it
            }
        }
    }
}
