package com.example.spor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

class DayDetailFragment3 : Fragment() {

    private var selectedDay: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            selectedDay = it.getString(ARG_SELECTED_DAY)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_day_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selectedDay?.let { day ->
            // Günün adını gösteren TextView'e günü ayarla
            view.findViewById<TextView>(R.id.textViewSelectedDay).text = day
            // Günün egzersizlerini gösteren TextView'e ilgili metni ayarla
            val exercisesTextView: TextView = view.findViewById(R.id.textViewExercises)
            exercisesTextView.text = when (day) {
                "1. Gün" -> getString(R.string.three_day1_exercises)
                "2. Gün" -> getString(R.string.three_day2_exercises)
                "3. Gün" -> getString(R.string.three_day3_exercises)
                "4. Gün" -> getString(R.string.three_day4_exercises)
                "5. Gün" -> getString(R.string.three_day5_exercises)
                "6. Gün" -> getString(R.string.three_day6_exercises)
                "7. Gün" -> getString(R.string.three_day7_exercises)
                "8. Gün" -> getString(R.string.three_day8_exercises)
                "9. Gün" -> getString(R.string.three_day9_exercises)
                "10. Gün" -> getString(R.string.three_day10_exercises)
                "11. Gün" -> getString(R.string.three_day11_exercises)
                "12. Gün" -> getString(R.string.three_day12_exercises)
                "13. Gün" -> getString(R.string.three_day13_exercises)
                "14. Gün" -> getString(R.string.three_day14_exercises)
                "15. Gün" -> getString(R.string.three_day15_exercises)
                "16. Gün" -> getString(R.string.three_day16_exercises)
                "17. Gün" -> getString(R.string.three_day17_exercises)
                "18. Gün" -> getString(R.string.three_day18_exercises)
                "19. Gün" -> getString(R.string.three_day19_exercises)
                "20. Gün" -> getString(R.string.three_day20_exercises)
                "21. Gün" -> getString(R.string.three_day21_exercises)
                "22. Gün" -> getString(R.string.three_day22_exercises)
                "23. Gün" -> getString(R.string.three_day23_exercises)
                "24. Gün" -> getString(R.string.three_day24_exercises)
                "25. Gün" -> getString(R.string.three_day25_exercises)
                "26. Gün" -> getString(R.string.three_day26_exercises)
                "27. Gün" -> getString(R.string.three_day27_exercises)
                "28. Gün" -> getString(R.string.three_day28_exercises)
                "29. Gün" -> getString(R.string.three_day29_exercises)
                "30. Gün" -> getString(R.string.three_day30_exercises)
                else -> "" // Diğer günler için metin belirtilmemişse boş bir metin göster
            }
        }
        // Geri butonunu bul
        val backButton: Button = view.findViewById(R.id.buttonBack)

        // Geri butonuna tıklama dinleyicisi ekle
        backButton.setOnClickListener {
            // İlk fragmenta geçiş yap
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.fragment_container, ThreeFragment()) // YourFirstFragment yerine kendi ilk fragmentınızı geçirin
                addToBackStack(null) // Geri tuşuna basıldığında geri dönülebilmesini sağlar
                commit()
            }
        }
        val buttonBack: Button = view.findViewById(R.id.buttonBack)

        // Geri butonuna tıklama dinleyicisi ekle
        backButton.setOnClickListener {
            // İlk fragmenta geçiş yap
            parentFragmentManager.popBackStack()
        }
    }

    companion object {
        private const val ARG_SELECTED_DAY = "selected_day"

        fun newInstance(selectedDay: String): DayDetailFragment3 {
            val fragment = DayDetailFragment3()
            val args = Bundle()
            args.putString(ARG_SELECTED_DAY, selectedDay)
            fragment.arguments = args
            return fragment
        }
    }
}
