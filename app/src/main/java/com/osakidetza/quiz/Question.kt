package com.osakidetza.quiz

import com.google.gson.annotations.SerializedName

data class Question(
    @SerializedName("id") val id: Int,
    @SerializedName("question") val question: String,
    @SerializedName("options") val options: List<String>,
    @SerializedName("correctAnswer") val correctAnswer: Int,
    @SerializedName("page") val page: Int
)
