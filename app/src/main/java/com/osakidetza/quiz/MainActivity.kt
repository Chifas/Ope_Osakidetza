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

        binding.btnQuick.setOnClickListener {
            startActivity(Intent(this, QuizActivity::class.java).apply {
                putExtra(QuizActivity.EXTRA_COUNT, 20)
            })
        }

        binding.btnFull.setOnClickListener {
            startActivity(Intent(this, QuizActivity::class.java).apply {
                putExtra(QuizActivity.EXTRA_COUNT, -1)
            })
        }

        binding.btnStudy.setOnClickListener {
            startActivity(Intent(this, StudyActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences("quiz_prefs", MODE_PRIVATE)
        val last = prefs.getInt("last_score", -1)
        val total = prefs.getInt("last_total", 0)
        val best = prefs.getInt("best_score", -1)
        binding.tvStats.text = when {
            last >= 0 && total > 0 -> "Último: $last/$total  |  Récord: $best/$total"
            else -> "¡Empieza a practicar! (299 preguntas)"
        }
    }
}
