package com.osakidetza.quiz

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.osakidetza.quiz.databinding.ActivityQuizBinding

class QuizActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_COUNT = "question_count"
        const val EXTRA_CATEGORY = "question_category"
        const val EXTRA_ONLY_FAILED = "only_failed"
    }

    private lateinit var binding: ActivityQuizBinding
    private lateinit var questions: List<Question>
    private var currentIndex = 0
    private var score = 0
    private var answered = false
    private var userAnswers = mutableMapOf<Int, Int>() // Mapa de respuestas: Índice -> Opción seleccionada

    private val optionButtons
        get() = listOf(
            binding.btnOption0,
            binding.btnOption1,
            binding.btnOption2,
            binding.btnOption3
        )

    // ColorStateList.of() requires API 23 but has Kotlin interop issues; use constructor instead
    private fun csl(color: Int) = ColorStateList(arrayOf(intArrayOf()), intArrayOf(color))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val count = intent.getIntExtra(EXTRA_COUNT, 20)
        val category = intent.getStringExtra(EXTRA_CATEGORY)
        val onlyFailed = intent.getBooleanExtra(EXTRA_ONLY_FAILED, false)

        val prefs = getSharedPreferences("quiz_session", MODE_PRIVATE)
        val savedIds = prefs.getString("question_ids", null)

        if (savedIds != null) {
            // Mostrar diálogo para continuar
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Continuar test")
                .setMessage("Hemos encontrado un test sin terminar. ¿Quieres continuarlo?")
                .setPositiveButton("Continuar") { _, _ ->
                    loadSession(savedIds)
                    showQuestion()
                }
                .setNegativeButton("Nuevo test") { _, _ ->
                    clearSession()
                    startNewQuiz(category, count, onlyFailed)
                }
                .setCancelable(false)
                .show()
        } else {
            startNewQuiz(category, count, onlyFailed)
        }

        setupListeners()
    }

    private fun startNewQuiz(category: String?, count: Int, onlyFailed: Boolean) {
        questions = QuestionsRepository.getQuestions(this, category, count, onlyFailed)
        if (questions.isEmpty()) {
            finish()
            return
        }
        currentIndex = 0
        score = 0
        userAnswers.clear()
        showQuestion()
    }

    private fun loadSession(savedIds: String) {
        val prefs = getSharedPreferences("quiz_session", MODE_PRIVATE)
        val ids = savedIds.split(",").map { it.toInt() }
        val allQuestions = QuestionsRepository.getAll(this)
        questions = ids.mapNotNull { id -> allQuestions.find { it.id == id } }
        
        currentIndex = prefs.getInt("current_index", 0)
        score = prefs.getInt("score", 0)
        
        val answersStr = prefs.getString("user_answers", "") ?: ""
        userAnswers.clear()
        if (answersStr.isNotEmpty()) {
            answersStr.split(",").forEach {
                val parts = it.split(":")
                if (parts.size == 2) {
                    userAnswers[parts[0].toInt()] = parts[1].toInt()
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnNext.setOnClickListener {
            if (!answered) return@setOnClickListener
            currentIndex++
            if (currentIndex >= questions.size) {
                clearSession()
                showResult()
            } else {
                saveSession()
                showQuestion()
            }
        }

        binding.btnPrevious.setOnClickListener {
            if (currentIndex > 0) {
                currentIndex--
                showQuestion()
            }
        }

        binding.btnBack.setOnClickListener {
            saveSession()
            finish()
        }
    }

    private fun saveSession() {
        val prefs = getSharedPreferences("quiz_session", MODE_PRIVATE)
        prefs.edit().apply {
            putInt("current_index", currentIndex)
            putInt("score", score)
            // Guardamos los IDs de las preguntas para reconstruir la lista
            putString("question_ids", questions.joinToString(",") { it.id.toString() })
            // Guardamos las respuestas del usuario: "indice:opcion,indice:opcion"
            putString("user_answers", userAnswers.map { "${it.key}:${it.value}" }.joinToString(","))
            apply()
        }
    }

    private fun clearSession() {
        getSharedPreferences("quiz_session", MODE_PRIVATE).edit().clear().apply()
    }

    private fun showQuestion() {
        val q = questions[currentIndex]
        val previousAnswer = userAnswers[currentIndex]
        answered = previousAnswer != null

        binding.tvProgress.text = "Pregunta ${currentIndex + 1} de ${questions.size}"
        binding.progressBar.progress = (currentIndex + 1) * 100 / questions.size
        binding.tvQuestion.text = q.question
        binding.btnNext.isEnabled = answered
        binding.btnNext.text =
            if (currentIndex + 1 >= questions.size) "Ver resultado" else "Siguiente"

        binding.btnPrevious.isEnabled = currentIndex > 0
        binding.btnPrevious.alpha = if (currentIndex > 0) 1.0f else 0.5f

        optionButtons.forEachIndexed { i, btn ->
            if (i < q.options.size) {
                btn.visibility = View.VISIBLE
                btn.text = "${'A' + i}. ${q.options[i]}"
                
                // Resetear estado visual
                btn.isEnabled = true
                btn.isClickable = !answered
                
                if (answered) {
                    val selected = previousAnswer!!
                    val correct = q.correctAnswer
                    when {
                        i == correct -> {
                            btn.backgroundTintList = csl(Color.parseColor("#4CAF50"))
                            btn.setTextColor(Color.WHITE)
                            btn.setStrokeColor(csl(Color.parseColor("#4CAF50")))
                        }
                        i == selected && selected != correct -> {
                            btn.backgroundTintList = csl(Color.parseColor("#F44336"))
                            btn.setTextColor(Color.WHITE)
                            btn.setStrokeColor(csl(Color.parseColor("#F44336")))
                        }
                        else -> {
                            btn.backgroundTintList = csl(Color.parseColor("#F5F5F5"))
                            btn.setTextColor(Color.parseColor("#BDBDBD"))
                            btn.setStrokeColor(csl(Color.parseColor("#E0E0E0")))
                        }
                    }
                } else {
                    btn.backgroundTintList = csl(Color.parseColor("#FFFFFF"))
                    btn.setTextColor(Color.parseColor("#374151"))
                    btn.setStrokeColor(csl(Color.parseColor("#E5E7EB")))
                    btn.setOnClickListener { onOptionSelected(i) }
                }
            } else {
                btn.visibility = View.GONE
            }
        }
    }

    private fun onOptionSelected(selected: Int) {
        if (answered) return
        answered = true
        userAnswers[currentIndex] = selected
        val correct = questions[currentIndex].correctAnswer

        optionButtons.forEachIndexed { i, btn ->
            btn.isClickable = false
            btn.setOnClickListener(null)
            when {
                i == correct -> {
                    btn.backgroundTintList = csl(Color.parseColor("#4CAF50"))
                    btn.setTextColor(Color.WHITE)
                    btn.setStrokeColor(csl(Color.parseColor("#4CAF50")))
                }
                i == selected && selected != correct -> {
                    btn.backgroundTintList = csl(Color.parseColor("#F44336"))
                    btn.setTextColor(Color.WHITE)
                    btn.setStrokeColor(csl(Color.parseColor("#F44336")))
                }
                else -> {
                    btn.backgroundTintList = csl(Color.parseColor("#F5F5F5"))
                    btn.setTextColor(Color.parseColor("#BDBDBD"))
                    btn.setStrokeColor(csl(Color.parseColor("#E0E0E0")))
                }
            }
        }

        if (selected == correct) {
            score++
            QuestionsRepository.removeFailedQuestion(this, questions[currentIndex].id)
        } else {
            QuestionsRepository.addFailedQuestion(this, questions[currentIndex])
        }
        binding.btnNext.isEnabled = true
        saveSession()
    }

    private fun showResult() {
        val prefs = getSharedPreferences("quiz_prefs", MODE_PRIVATE)
        val prevBest = prefs.getInt("best_score", -1)
        prefs.edit().apply {
            putInt("last_score", score)
            putInt("last_total", questions.size)
            if (prevBest < 0 || score > prevBest) putInt("best_score", score)
            apply()
        }

        startActivity(Intent(this, ResultActivity::class.java).apply {
            putExtra("score", score)
            putExtra("total", questions.size)
        })
        finish()
    }
}
