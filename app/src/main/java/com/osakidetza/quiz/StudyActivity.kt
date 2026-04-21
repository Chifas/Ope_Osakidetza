package com.osakidetza.quiz

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
    private lateinit var allQuestions: List<Question>
    private lateinit var adapter: StudyAdapter
    private var currentFilter = "Todos"
    private var currentSearch = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // Ocultar título por defecto para usar el centrado
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        allQuestions = QuestionsRepository.getAll(this)
        adapter = StudyAdapter(allQuestions)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        setupFilters()
    }

    private fun setupFilters() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearch = s.toString().lowercase()
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.chipGroupFilter.setOnCheckedStateChangeListener { group, checkedIds ->
            currentFilter = when (checkedIds.firstOrNull()) {
                R.id.chipComun -> "Común"
                R.id.chipEspec -> "Específico"
                else -> "Todos"
            }
            applyFilters()
        }
    }

    private fun applyFilters() {
        val filteredList = allQuestions.filter { q ->
            val matchesSearch = q.question.lowercase().contains(currentSearch) || 
                               q.options.any { it.lowercase().contains(currentSearch) }
            
            // Corrección: La categoría en el JSON es "comun" o "especifico" (con o sin tilde según el JSON)
            // Usamos equals con ignoreCase y normalizamos para evitar problemas de tildes si las hubiera
            val matchesCategory = when (currentFilter) {
                "Común" -> q.category.equals("comun", ignoreCase = true)
                "Específico" -> !q.category.equals("comun", ignoreCase = true)
                else -> true
            }
            
            matchesSearch && matchesCategory
        }
        adapter.updateList(filteredList)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}

class StudyAdapter(private var questions: List<Question>) :
    RecyclerView.Adapter<StudyAdapter.ViewHolder>() {

    fun updateList(newList: List<Question>) {
        questions = newList
        notifyDataSetChanged()
    }

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
