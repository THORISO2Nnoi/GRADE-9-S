package com.example.edupath.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

enum class RecommendationType {
    CAREER_GUIDANCE,
    UNIVERSITY_RECOMMENDATION,
    REQUIREMENTS,
    STREAM_RECOMMENDATION
}

enum class AdmissionStatus {
    EXCEEDS_REQUIREMENT,
    MEETS_REQUIREMENT,
    BELOW_REQUIREMENT
}

@Parcelize
data class University(
    val name: String,
    val program: String,
    val location: String,
    val apsRequirement: Int,
    val status: AdmissionStatus
) : Parcelable

@Parcelize
data class Career(
    val title: String,
    val description: String,
    val demand: String,
    val requirements: List<String>,
    val skillsNeeded: List<String>,
    val elevatorPitch: String
) : Parcelable

@Parcelize
data class School(
    val name: String,
    val location: String,
    val distance: String,
    val type: String,
    val streams: List<String>
) : Parcelable

@Parcelize
data class Recommendation(
    val title: String,
    val description: String,
    val type: RecommendationType,
    val requirements: List<String> = emptyList(),
    val universities: List<University> = emptyList(),
    val careers: List<Career> = emptyList(),
    val schools: List<School> = emptyList()
) : Parcelable