package com.example.edupath.data


import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StudentProfile(
    val grade: Int,
    val subjects: List<Subject>,
    val selectedSkills: List<String> = emptyList(),
    val selectedInterests: List<String> = emptyList(),
    val apsScore: Int = 0
) : Parcelable