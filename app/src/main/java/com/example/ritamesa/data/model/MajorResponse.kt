package com.example.ritamesa.data.model

import com.google.gson.annotations.SerializedName

data class MajorResponse(
    @SerializedName("data")
    val data: List<MajorItem>,
    @SerializedName("current_page")
    val currentPage: Int? = null,
    @SerializedName("last_page")
    val lastPage: Int? = null
)
