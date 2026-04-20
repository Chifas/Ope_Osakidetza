package com.osakidetza.quiz

import com.google.gson.annotations.SerializedName

data class Question(
    @SerializedName("id") val id: Int,
    @SerializedName("categoria") val category: String,
    @SerializedName("pregunta") val question: String,
    @SerializedName("opciones") val options: List<String>,
    @SerializedName("respuesta_correcta") val correctAnswer: Int
)
