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
    }

    private lateinit var binding: ActivityQuizBinding
    private lateinit var questions: List<Question>
    private var currentIndex = 0
    private var score = 0
    private var answered = false

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
        questions = QuestionsRepository.getShuffled(this, count)

        showQuestion()

        binding.btnNext.setOnClickListener {
            if (!answered) return@setOnClickListener
            currentIndex++
            if (currentIndex >= questions.size) showResult() else showQuestion()
        }
    }

    private fun showQuestion() {
        answered = false
        val q = questions[currentIndex]

        binding.tvProgress.text = "Pregunta ${currentIndex + 1} de ${questions.size}"
        binding.progressBar.progress = (currentIndex + 1) * 100 / questions.size
        binding.tvQuestion.text = q.question
        binding.btnNext.isEnabled = false
        binding.btnNext.text =
            if (currentIndex + 1 >= questions.size) "Ver resultado" else "Siguiente"

        optionButtons.forEachIndexed { i, btn ->
            if (i < q.options.size) {
                btn.visibility = View.VISIBLE
                btn.isEnabled = true
                btn.isClickable = true
                btn.text = "${'A' + i}. ${q.options[i]}"
                btn.backgroundTintList = csl(Color.parseColor("#E3F2FD"))
                btn.setTextColor(Color.parseColor("#212121"))
                btn.setStrokeColor(csl(Color.parseColor("#1565C0")))
                btn.setOnClickListener { onOptionSelected(i) }
            } else {
                btn.visibility = View.GONE
            }
        }
    }

    private fun onOptionSelected(selected: Int) {
        if (answered) return
        answered = true
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
            }
        }

        if (selected == correct) score++
        binding.btnNext.isEnabled = true
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
