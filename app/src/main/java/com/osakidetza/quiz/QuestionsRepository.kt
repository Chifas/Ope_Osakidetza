package com.osakidetza.quiz

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object QuestionsRepository {

    private var questions: List<Question>? = null

    fun getAll(context: Context): List<Question> {
        if (questions == null) {
            val json = context.assets.open("bateria.json").bufferedReader().readText()
            val type = object : TypeToken<List<Question>>() {}.type
            questions = Gson().fromJson<List<Question>>(json, type)
        }
        return questions!!
    }

    fun getQuestions(context: Context, category: String?, count: Int): List<Question> {
        var filtered = getAll(context)
        if (category != null) {
            filtered = filtered.filter { it.category.equals(category, ignoreCase = true) }
        }
        val shuffled = filtered.shuffled()
        return if (count > 0 && count < shuffled.size) shuffled.take(count) else shuffled
    }
}
