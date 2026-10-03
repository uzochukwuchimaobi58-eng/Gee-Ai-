package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey val id: String,
    val name: String,
    val topic: String,
    val prompt: String,
    val targetAudience: String,
    val lengthType: String, // "6s-1m", "1m-5m", "5m-10m", "10m-30m"
    val language: String,
    val tone: String,
    val style: String,
    val presenter: String,
    val voice: String,
    val accent: String,
    val backgroundMusic: String,
    val videoFormat: String, // "16:9", "9:16", "1:1"
    val resolution: String, // "720p", "1080p", "4K"
    val script: String,
    val createdAt: Long,
    val status: String // "Draft", "Generating", "Completed"
)

@Entity(tableName = "scenes")
data class Scene(
    @PrimaryKey val id: String,
    val projectId: String,
    val sceneIndex: Int,
    val narration: String,
    val visualPrompt: String,
    val durationSeconds: Int,
    val transition: String,
    val cameraDirection: String,
    val captionText: String,
    val imageUrl: String = "",
    val status: String = "Pending" // "Pending", "Generating", "Ready"
)

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val email: String,
    val name: String,
    val role: String, // "Admin", "User"
    val creditsRemaining: Int,
    val storageUsedMb: Double,
    val subscriptionTier: String, // "Free", "Starter", "Pro", "Business"
    val videosGeneratedCount: Int
)

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey val id: String,
    val email: String,
    val dateString: String,
    val amount: String,
    val creditsAdded: Int,
    val planName: String,
    val status: String // "Paid", "Pending"
)

data class ThumbnailIdea(
    val id: String,
    val imageUrl: String,
    val description: String,
    val isSelected: Boolean = false
)

data class SeoMetadata(
    val youtubeTitle: String,
    val description: String,
    val tags: String,
    val chapters: String,
    val hashtags: String
)
