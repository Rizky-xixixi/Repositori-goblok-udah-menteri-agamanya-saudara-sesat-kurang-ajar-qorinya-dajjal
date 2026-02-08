package com.example.ritamesa.data.model

import com.example.ritamesa.Jurusan
import com.google.gson.annotations.SerializedName

data class MajorResponse(
    @SerializedName("data")
    val data: List<Jurusan>,
    @SerializedName("current_page")
    val currentPage: Int,
    @SerializedName("last_page")
    val lastPage: Int
)
