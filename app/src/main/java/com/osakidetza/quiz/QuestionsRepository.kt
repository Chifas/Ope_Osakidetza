package com.osakidetza.quiz

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object QuestionsRepository {

    private var questions: List<Question>? = null

    fun getAll(context: Context): List<Question> {
        if (questions == null) {
            val json = context.assets.open("questions.json").bufferedReader().readText()
            val type = object : TypeToken<List<Question>>() {}.type
            questions = Gson().fromJson<List<Question>>(json, type)
                .filter { it.correctAnswer >= 0 }
        }
        return questions!!
    }

    fun getShuffled(context: Context, count: Int = -1): List<Question> {
        val all = getAll(context).shuffled()
        return if (count > 0 && count < all.size) all.take(count) else all
    }
}
