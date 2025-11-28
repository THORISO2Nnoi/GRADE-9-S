package com.example.edupath.data


import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Subject(
    val name: String,
    val score: Int,
    val isCore: Boolean = false
) : Parcelable