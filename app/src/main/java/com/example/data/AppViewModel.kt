package com.example.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.util.UUID

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
    val wizardLength = MutableStateFlow("1m-5m") // "6s-1m", "1m-5m", "5m-10m", "10m-30m"
    val wizardLanguage = MutableStateFlow("English")
    val wizardTone = MutableStateFlow("Professional")
    val wizardStyle = MutableStateFlow("Cinematic")

    val wizardPresenter = MutableStateFlow("Alex (AI Avatar)")
    val wizardVoice = MutableStateFlow("Bella (Warm/Friendly)")
    val wizardAccent = MutableStateFlow("US American")
    val wizardMusic = MutableStateFlow("Ambient Cinematic")
    val wizardFormat = MutableStateFlow("16:9")
    val wizardResolution = MutableStateFlow("1080p")

    val wizardScript = MutableStateFlow("")
    val isGeneratingScript = MutableStateFlow(false)

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
    val avatarName = MutableStateFlow("Elena (Executive AI)")
    val avatarFraming = MutableStateFlow("Studio Close-up")
    val avatarEmotion = MutableStateFlow("Warm & Authoritative")
    val avatarBackdrop = MutableStateFlow("Modern Minimalist Office")
    val avatarScript = MutableStateFlow("Welcome to our executive brief. Today, we're demonstrating the next generation of digital avatars powered by photorealistic neural rendering and natural cadence.")
    val avatarVoice = MutableStateFlow("Bella - Warm Professional (US)")
    val avatarGestures = MutableStateFlow("Subtle Natural Hand Gestures")
    val isGeneratingAvatar = MutableStateFlow(false)
    val avatarRenderedVideoUrl = MutableStateFlow<String?>(null)

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
                creditsRemaining = existing?.creditsRemaining ?: 100,
                storageUsedMb = existing?.storageUsedMb ?: 120.0,
                subscriptionTier = existing?.subscriptionTier ?: "Pro",
                videosGeneratedCount = existing?.videosGeneratedCount ?: 5
            )
            userProfileDao.insertUserProfile(newProfile)
            _currentScreen.value = Screen.DASHBOARD
        }
    }

    fun logout() {
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
                length = wizardLength.value,
                language = wizardLanguage.value,
                tone = wizardTone.value,
                style = wizardStyle.value
            )
            wizardScript.value = script
            isGeneratingScript.value = false
        }
    }

    // Scene builder conversion
    fun buildScenesAndProject() {
        viewModelScope.launch {
            val projId = UUID.randomUUID().toString()
            val scriptText = wizardScript.value
            val project = Project(
                id = projId,
                name = wizardTopic.value.ifBlank { "Untitled AI Project" },
                topic = wizardTopic.value,
                prompt = wizardPrompt.value,
                targetAudience = wizardAudience.value,
                lengthType = wizardLength.value,
                language = wizardLanguage.value,
                tone = wizardTone.value,
                style = wizardStyle.value,
                presenter = wizardPresenter.value,
                voice = wizardVoice.value,
                accent = wizardAccent.value,
                backgroundMusic = wizardMusic.value,
                videoFormat = wizardFormat.value,
                resolution = wizardResolution.value,
                script = scriptText,
                createdAt = System.currentTimeMillis(),
                status = "Draft"
            )

            // Auto split script text into scenes
            val paragraphs = scriptText.split(Regex("\n\n+")).filter { it.isNotBlank() }
            val sentences = if (paragraphs.size <= 1) {
                scriptText.split(Regex("(?<=[.!?])\\s+")).filter { it.isNotBlank() }
            } else paragraphs

            val sceneList = sentences.mapIndexed { idx, sentence ->
                val duration = when (project.lengthType) {
                    "1s-7s (Short Clip)" -> 6
                    "6s-1m" -> 10
                    "1m-5m" -> 15
                    "5m-10m" -> 25
                    else -> 35
                }
                val visualDesc = when (idx) {
                    0 -> "Cinematic dynamic shot showcasing ${project.topic} as the primary theme, ultra detailed style ${project.style} format"
                    1 -> "High-contrast action shot building on ${project.topic}, camera slowly panning sideways, gorgeous lighting"
                    2 -> "Informative infographics elements or deep detailed close up focusing on topic elements, elegant rendering"
                    else -> "Epic resolution zoom in of subject, professional lighting, modern atmospheric background"
                }
                Scene(
                    id = "$projId-scene-$idx",
                    projectId = projId,
                    sceneIndex = idx,
                    narration = sentence.trim(),
                    visualPrompt = visualDesc,
                    durationSeconds = duration,
                    transition = if (idx == 0) "None" else "Cross Dissolve",
                    cameraDirection = when (idx % 3) {
                        0 -> "Slow Zoom In"
                        1 -> "Pan Right"
                        else -> "Static Focus"
                    },
                    captionText = sentence.trim().take(60) + "...",
                    imageUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?q=80&w=600&auto=format&fit=crop",
                    status = "Pending"
                )
            }

            val totalDuration = sceneList.sumOf { it.durationSeconds }
            val hasBackgroundMusic = project.backgroundMusic != "None" && project.backgroundMusic.isNotBlank()
            val baseCredits = if (totalDuration in 1..7 || project.lengthType == "1s-7s (Short Clip)") 60 else 15
            val musicCredits = if (hasBackgroundMusic) 10 else 0
            val totalCreditsDeduction = baseCredits + musicCredits

            projectDao.insertProject(project)
            sceneDao.insertScenes(sceneList)

            _activeProject.value = project
            _activeScenes.value = sceneList

            // Deduct calculated credits
            deductCredits(totalCreditsDeduction)

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
    fun selectSubscriptionTier(tier: String) {
        viewModelScope.launch {
            userProfile.value?.let { prof ->
                val invoice = paymentService.processSubscription(prof.email, tier)
                invoiceDao.insertInvoice(invoice)

                userProfileDao.updateUserProfile(
                    prof.copy(
                        subscriptionTier = tier,
                        creditsRemaining = prof.creditsRemaining + invoice.creditsAdded
                    )
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

    // Helper to deduct credits
    private suspend fun deductCredits(amount: Int) {
        userProfile.value?.let { prof ->
            val remaining = (prof.creditsRemaining - amount).coerceAtLeast(0)
            userProfileDao.updateUserProfile(prof.copy(creditsRemaining = remaining))
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
