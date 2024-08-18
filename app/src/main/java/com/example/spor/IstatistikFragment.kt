package com.example.spor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider

class IstatistikFragment : Fragment() {
    private lateinit var progressBar1: ProgressBar
    private lateinit var progressBar2: ProgressBar
    private lateinit var progressBar3: ProgressBar
    private lateinit var progressBar4: ProgressBar
    private lateinit var progressBar5: ProgressBar
    private lateinit var progressBars: List<ProgressBar>
    private var stepCountViewModel: StepCountViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_istatistik, container, false)
        progressBar1 = view.findViewById(R.id.progressBar1)
        progressBar2 = view.findViewById(R.id.progressBar2)
        progressBar3 = view.findViewById(R.id.progressBar3)
        progressBar4 = view.findViewById(R.id.progressBar4)
        progressBar5 = view.findViewById(R.id.progressBar5)
        progressBars = listOf(progressBar1, progressBar2, progressBar3, progressBar4, progressBar5)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        stepCountViewModel =
            ViewModelProvider(requireActivity()).get(StepCountViewModel::class.java)
        observeStepCount()
    }

    override fun onResume() {
        super.onResume()
        updateProgressBarsFromHomeFragment()
    }

    private fun observeStepCount() {
        stepCountViewModel?.stepCount?.observe(viewLifecycleOwner, Observer { count ->
            // Step count değiştiğinde progress barları güncelle
            updateProgressBars(count)
        })
    }

    private fun updateProgressBarsFromHomeFragment() {
        val homeFragment =
            requireActivity().supportFragmentManager.findFragmentByTag("HomeFragment") as? HomeFragment
        homeFragment?.let { fragment ->
            val stepCount = fragment.getStepCount()
            updateProgressBars(stepCount)
        }
    }

    private fun updateProgressBars(stepCount: Int) {
        val dailyGoals = listOf(5000, 10000, 15000, 20000, 25000)
        val goalIncrements = listOf(50, 100, 150, 200, 250)

        for (i in dailyGoals.indices) {
            val progress = ((stepCount.toFloat() / dailyGoals[i]) * 100).toInt()
            setProgressBar(progressBars[i], progress, 100)
        }
    }

    private fun setProgressBar(progressBar: ProgressBar, progress: Int, maxProgress: Int) {
        progressBar.max = maxProgress
        progressBar.progress = progress
        progressBar.animate().cancel()
        progressBar.animate().setDuration(1000).start()
    }
}
