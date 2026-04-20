package com.osakidetza.quiz

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.osakidetza.quiz.databinding.ActivityStudyBinding

class StudyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Modo Estudio (${QuestionsRepository.getAll(this).size} preguntas)"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val questions = QuestionsRepository.getAll(this)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = StudyAdapter(questions)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}

class StudyAdapter(private val questions: List<Question>) :
    RecyclerView.Adapter<StudyAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNumber: TextView = view.findViewById(R.id.tvNumber)
        val tvQuestion: TextView = view.findViewById(R.id.tvQuestion)
        val options = listOf<TextView>(
            view.findViewById(R.id.tvOpt0),
            view.findViewById(R.id.tvOpt1),
            view.findViewById(R.id.tvOpt2),
            view.findViewById(R.id.tvOpt3)
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_question_study, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val q = questions[position]
        holder.tvNumber.text = "Pregunta ${q.id}"
        holder.tvQuestion.text = q.question

        holder.options.forEachIndexed { i, tv ->
            if (i < q.options.size) {
                tv.visibility = View.VISIBLE
                tv.text = "${'A' + i}. ${q.options[i]}"
                if (i == q.correctAnswer) {
                    tv.setBackgroundColor(Color.parseColor("#C8E6C9"))
                    tv.setTextColor(Color.parseColor("#1B5E20"))
                    tv.setTypeface(null, android.graphics.Typeface.BOLD)
                } else {
                    tv.setBackgroundColor(Color.TRANSPARENT)
                    tv.setTextColor(Color.parseColor("#424242"))
                    tv.setTypeface(null, android.graphics.Typeface.NORMAL)
                }
            } else {
                tv.visibility = View.GONE
            }
        }
    }

    override fun getItemCount() = questions.size
}
