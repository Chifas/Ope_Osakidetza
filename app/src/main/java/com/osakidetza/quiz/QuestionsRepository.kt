package com.osakidetza.quiz

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object QuestionsRepository {

    private var questions: List<Question>? = null
    private const val FAILED_QUESTIONS_FILE = "failed_questions.json"

    fun getAll(context: Context): List<Question> {
        if (questions == null) {
            val json = context.assets.open("bateria.json").bufferedReader().readText()
            val type = object : TypeToken<List<Question>>() {}.type
            questions = Gson().fromJson<List<Question>>(json, type)
        }
        return questions!!
    }

    fun getQuestions(context: Context, category: String?, count: Int, onlyFailed: Boolean = false): List<Question> {
        var filtered = if (onlyFailed) getFailedQuestions(context) else getAll(context)
        
        if (category != null) {
            filtered = filtered.filter { it.category.equals(category, ignoreCase = true) }
        }
        val shuffled = filtered.shuffled()
        return if (count > 0 && count < shuffled.size) shuffled.take(count) else shuffled
    }

    fun getFailedQuestions(context: Context): List<Question> {
        val file = context.getFileStreamPath(FAILED_QUESTIONS_FILE)
        if (!file.exists()) return emptyList()
        
        return try {
            val json = file.readText()
            val type = object : TypeToken<List<Question>>() {}.type
            Gson().fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addFailedQuestion(context: Context, question: Question) {
        val failed = getFailedQuestions(context).toMutableList()
        if (failed.none { it.id == question.id }) {
            failed.add(question)
            saveFailedQuestions(context, failed)
        }
    }

    fun removeFailedQuestion(context: Context, questionId: Int) {
        val failed = getFailedQuestions(context).toMutableList()
        if (failed.removeIf { it.id == questionId }) {
            saveFailedQuestions(context, failed)
        }
    }

    private fun saveFailedQuestions(context: Context, list: List<Question>) {
        val json = Gson().toJson(list)
        context.openFileOutput(FAILED_QUESTIONS_FILE, Context.MODE_PRIVATE).use {
            it.write(json.toByteArray())
        }
    }
}
