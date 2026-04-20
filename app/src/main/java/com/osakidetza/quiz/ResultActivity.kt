package com.osakidetza.quiz

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.osakidetza.quiz.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val score = intent.getIntExtra("score", 0)
        val total = intent.getIntExtra("total", 1)
        val pct = score * 100 / total

        binding.tvScore.text = "$score / $total"
        binding.tvPercentage.text = "$pct%"
        binding.tvMessage.text = when {
            pct >= 90 -> "¡Excelente! Estás listo/a para el examen."
            pct >= 70 -> "¡Muy bien! Sigue practicando."
            pct >= 50 -> "Aprobado. Puedes mejorar con más práctica."
            else -> "Necesitas repasar más. ¡Tú puedes!"
        }

        binding.btnRepeat.setOnClickListener {
            startActivity(Intent(this, QuizActivity::class.java).apply {
                putExtra(QuizActivity.EXTRA_COUNT, total)
            })
            finish()
        }

        binding.btnHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            })
            finish()
        }
    }
}
