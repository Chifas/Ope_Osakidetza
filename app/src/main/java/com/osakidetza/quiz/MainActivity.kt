package com.osakidetza.quiz

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.osakidetza.quiz.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Común
        binding.btnComunQuick.setOnClickListener { startQuiz("comun", 20) }
        binding.btnComunFull.setOnClickListener { startQuiz("comun", 100) }

        // Específico
        binding.btnEspecQuick.setOnClickListener { startQuiz("especifico", 20) }
        binding.btnEspecFull.setOnClickListener { startQuiz("especifico", 100) }

        // Todo
        binding.btnAllQuick.setOnClickListener { startQuiz(null, 20) }
        binding.btnAllFull.setOnClickListener { startQuiz(null, 100) }

        binding.btnStudy.setOnClickListener {
            startActivity(Intent(this, StudyActivity::class.java))
        }
    }

    private fun startQuiz(category: String?, count: Int) {
        val intent = Intent(this, QuizActivity::class.java).apply {
            putExtra(QuizActivity.EXTRA_COUNT, count)
            putExtra(QuizActivity.EXTRA_CATEGORY, category)
        }
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences("quiz_prefs", MODE_PRIVATE)
        val last = prefs.getInt("last_score", -1)
        val total = prefs.getInt("last_total", 0)
        val best = prefs.getInt("best_score", -1)
        
        binding.tvStats.text = when {
            last >= 0 && total > 0 -> "📊 Último test: $last/$total  |  Récord personal: $best/$total"
            else -> "¡Bienvenida! Selecciona un tipo de examen para empezar."
        }
    }
}
