package com.example.spor

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment(), SensorEventListener {
    private var previousStepValues: MutableList<Float> = mutableListOf()
    private var previousCalorieValues: MutableList<Float> = mutableListOf()
    private val sdfMonthYear = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val sdfDayOfWeek = SimpleDateFormat("EEE", Locale.getDefault())
    private val sdfDayOfMonth = SimpleDateFormat("dd", Locale.getDefault())
    private lateinit var monthYearTextView: TextView
    private lateinit var dayTextView: TextView
    private lateinit var recyclerViewDates: RecyclerView
    private lateinit var dateAdapter: DateAdapter
    private var currentDate = Calendar.getInstance()
    private var sensorManager: SensorManager? = null
    private var running = false
    private var totalSteps = 0f
    private var previousTotalSteps = 0f
    var isViewCreated = false
    // Constants for calorie calculation
    private val caloriesBurnedPerStep = 0.05f // Each step burns 0.05 calories
    private lateinit var stepCountViewModel: StepCountViewModel
    private lateinit var waterCircularProgressBar: CircularProgressBar
    private lateinit var waterIntakeTextView: TextView
    private lateinit var waterIntakeButton: Button
    private val SHARED_PREFS_NAME = "MyAppSharedPreferences"
    private val KEY_WATER_AMOUNT = "waterAmount"
    private val KEY_WATER_PROGRESS = "waterProgress"
    private val KEY_LAST_RESET_DATE = "lastResetDate"
    private var currentWaterAmount = 0
    private var lastResetDate: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        previousStepValues = MutableList(7) { 0f } // Listeyi başlat
        previousCalorieValues = MutableList(7) { 0f } // Listeyi başlat
        loadData() // loadData() fonksiyonunu çağır
        waterCircularProgressBar = view.findViewById(R.id.waterCircularProgressBar)
        waterIntakeTextView = view.findViewById(R.id.waterIntakeTextView)
        waterIntakeButton = view.findViewById(R.id.waterIntakeButton)

        // Set initial values
        waterIntakeTextView.text = "$currentWaterAmount ml"

        // Button click listener
        waterIntakeButton.setOnClickListener {
            addWater(250) // 250 ml ekle
        }
        // view değişkenine erişerek bileşenlere erişim sağla
        monthYearTextView = view.findViewById(R.id.monthYearTextView)
        dayTextView = view.findViewById(R.id.dayTextView)
        recyclerViewDates = view.findViewById(R.id.recyclerViewDates)
        val calendarIcon = view.findViewById<View>(R.id.calendarIcon)
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager

        recyclerViewDates.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        dateAdapter = DateAdapter(generateDates(currentDate))
        recyclerViewDates.adapter = dateAdapter

        // Tarih görünümlerini güncelle
        updateDateViews()

        calendarIcon.setOnClickListener {
            showDatePicker()
        }

        return view
    }

    private fun addWater(amount: Int) {
        // Yeni su miktarını hesapla
        val newWaterAmount = currentWaterAmount + amount

        // Günlük sınırı aşsa bile arttırmaya devam et
        currentWaterAmount = newWaterAmount
        waterIntakeTextView.text = "$currentWaterAmount ml"

        // ProgressBar'ı güncelle
        val newProgress = (currentWaterAmount.toFloat() / 2000f) * 100 // 2000 ml'lik maksimum değer için
        waterCircularProgressBar.setProgressWithAnimation(newProgress, 1000) // İleriye doğru animasyon ile ilerleyin

        // Verileri kaydet
        saveWaterData()
    }

    private fun saveWaterData() {
        val sharedPreferences = requireContext().getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt(KEY_WATER_AMOUNT, currentWaterAmount)
        editor.putFloat(KEY_WATER_PROGRESS, waterCircularProgressBar.progress)
        editor.putLong(KEY_LAST_RESET_DATE, lastResetDate)
        editor.apply()
    }

    private fun loadWaterData() {
        val sharedPreferences = requireContext().getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        currentWaterAmount = sharedPreferences.getInt(KEY_WATER_AMOUNT, 0)
        val savedProgress = sharedPreferences.getFloat(KEY_WATER_PROGRESS, 0f)
        waterIntakeTextView.text = "$currentWaterAmount ml"
        waterCircularProgressBar.setProgressWithAnimation(savedProgress, 1000)
        lastResetDate = sharedPreferences.getLong(KEY_LAST_RESET_DATE, 0)

        // Kontrol et ve eğer tarih değiştiyse verileri sıfırla
        val today = Calendar.getInstance()
        val lastReset = Calendar.getInstance().apply { timeInMillis = lastResetDate }

        if (today.get(Calendar.YEAR) != lastReset.get(Calendar.YEAR) ||
            today.get(Calendar.DAY_OF_YEAR) != lastReset.get(Calendar.DAY_OF_YEAR)
        ) {
            resetData()
            lastResetDate = today.timeInMillis
            saveWaterData()
        }
    }

    private fun resetData() {
        currentWaterAmount = 0
        waterIntakeTextView.text = "0 ml"
        waterCircularProgressBar.setProgressWithAnimation(0f, 1000)
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        resetSteps()
        isViewCreated = true
        stepCountViewModel = ViewModelProvider(requireActivity()).get(StepCountViewModel::class.java)
        stepCountViewModel.stepCount.observe(viewLifecycleOwner, Observer { count ->
            // Do something with the updated step count, such as updating UI
        })
        Log.d("HomeFragment", "View created")

        // Gerçek zamanlı adım ve kalori değerlerini al
        val weeklyStepValues = getWeeklyStepValues()
        val weeklyCalorieValues = getWeeklyCalorieValues()

        // Bar chart'ı ayarla ve gerçek zamanlı verilerle güncelle
        val barChart: BarChart = view.findViewById(R.id.bar_chart_steps)
        setupBarChart(barChart, weeklyStepValues, weeklyCalorieValues)

        // Adım ilerleme çubuğunun arka planını doldur
        val stepCircularProgressBar = view.findViewById<CircularProgressBar>(R.id.stepCircularProgressBar)
        stepCircularProgressBar.backgroundProgressBarColor = ContextCompat.getColor(requireContext(), R.color.orange)
        stepCircularProgressBar.setProgressWithAnimation(100f, 1000)

        // Kalori ilerleme çubuğunun arka planını doldur
        val kaloriCircularProgressBar = view.findViewById<CircularProgressBar>(R.id.kaloriCircularProgressBar)
        kaloriCircularProgressBar.backgroundProgressBarColor = ContextCompat.getColor(requireContext(), R.color.white)
        kaloriCircularProgressBar.setProgressWithAnimation(100f, 1000)


        // İlerleme çubuklarının arka planlarını doldur
        fillCircularProgressBarBackgrounds()
    }

    private fun setupBarChart(barChart: BarChart, stepValues: List<Float>, calorieValues: List<Float>) {
        // Veri girişlerini hazırla
        val stepEntries = mutableListOf<BarEntry>()
        val calorieEntries = mutableListOf<BarEntry>()

        stepValues.forEachIndexed { index, value ->
            stepEntries.add(BarEntry(index.toFloat(), value))
        }

        calorieValues.forEachIndexed { index, value ->
            calorieEntries.add(BarEntry(index.toFloat(), value))
        }

        val stepDataSet = BarDataSet(stepEntries, "Adım Sayısı")
        stepDataSet.color = ContextCompat.getColor(requireContext(), R.color.custom_blue)
        stepDataSet.setDrawValues(true)
        stepDataSet.setValueTextColor(Color.BLACK)

        val calorieDataSet = BarDataSet(calorieEntries, "Kalori Tüketimi")
        calorieDataSet.color = resources.getColor(android.R.color.white)
        calorieDataSet.setDrawValues(true)
        calorieDataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return String.format(Locale.getDefault(), "%.2f", value)
            }
        }
        calorieDataSet.setValueTextColor(Color.BLACK)

        val data = BarData(stepDataSet, calorieDataSet)
        data.barWidth = 0.3f

        // BarData null olmadığından emin olun
        barChart.data = data

        // Bar genişliğini ve aralıkları ayarla
        val groupSpace = 0.3f
        val barSpace = 0.05f
        val barWidth = 0.3f

        // Gün isimlerini ayarla
        val daysOfWeek = arrayOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")
        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(daysOfWeek)
        xAxis.textColor = Color.BLACK
        xAxis.textSize = 8f
        xAxis.position = XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)

        // Taban çizgisinden başla
        xAxis.axisMinimum = -0.6f // Yatay eksende başlangıç noktası ayarı

        // Bar aralıklarını ve genişliğini ayarla
        data.barWidth = barWidth
        barChart.groupBars(-0.6f, groupSpace, barSpace)

        // Diğer grafik özelliklerini ayarla
        val leftAxis: YAxis = barChart.axisLeft
        leftAxis.axisMinimum = 0f // Sol eksende başlangıç noktası ayarı
        leftAxis.isEnabled = false // Sol eksen kapatıldı

        val rightAxis: YAxis = barChart.axisRight
        rightAxis.isEnabled = false

        barChart.description.isEnabled = false
        barChart.animateY(1000, Easing.Linear)

        // Legendi kaldır
        barChart.legend.isEnabled = false

        // Grafik güncelleme
        barChart.notifyDataSetChanged()
        barChart.invalidate()
    }




    fun getStepCount(): Int {
        val stepCountTextView = requireView().findViewById<TextView>(R.id.stepCountTextView)
        val stepCount = stepCountTextView.text.toString().toIntOrNull() ?: 0
        Log.d("HomeFragment", "Adım sayısı alındı: $stepCount")
        return stepCount
    }

    private fun getWeeklyStepValues(): List<Float> {
        return previousStepValues
    }

    private fun getWeeklyCalorieValues(): List<Float> {
        return previousCalorieValues
    }


    private fun getWeekDays(): List<String> {
        val calendar = Calendar.getInstance(Locale("tr", "TR"))
        calendar.firstDayOfWeek = Calendar.MONDAY // Pazartesi gününü haftanın başı olarak ayarla
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)

        val weekDays = mutableListOf<String>()
        val dateFormat = SimpleDateFormat("EEEE", Locale("tr", "TR"))
        for (i in 0 until 7) {
            weekDays.add(dateFormat.format(calendar.time))
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return weekDays
    }


    private fun startClockUpdater() {
        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                if (view != null) { // onViewCreated içinde oluşturulan view null değilse

                }
                handler.postDelayed(this, 1000) // Her saniyede bir güncelle
            }
        })
    }

    override fun onResume() {
        super.onResume()
        running = true
        loadWaterData()
        val stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepSensor == null) {
            Toast.makeText(
                requireContext(),
                "No sensor detected on this device",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        }
        val serviceIntent = Intent(requireContext(), StepCounterService::class.java)
        requireContext().startService(serviceIntent)

        // Güncel günü al
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_WEEK)
        val sharedPreferences = requireActivity().getSharedPreferences("myPrefs", Context.MODE_PRIVATE)

        // Önceki günle karşılaştır
        val lastSavedDay = sharedPreferences.getInt("key_last_saved_day", -1)

        if (lastSavedDay != currentDay) { // Yeni gün
            // Adım ve kalori değerlerini sıfırla
            totalSteps = 0f
            previousTotalSteps = 0f

            // Pazartesi gününü kontrol et
            if (currentDay == Calendar.MONDAY) {
                // Tüm günlerin değerlerini sıfırla
                for (i in previousStepValues.indices) {
                    previousStepValues[i] = 0f
                    previousCalorieValues[i] = 0f
                }
            }

            // Eğer güncel gün Pazar değilse, güncel günün değerlerini sıfırla
            if (currentDay != Calendar.SUNDAY) {
                previousStepValues[calendar.get(Calendar.DAY_OF_WEEK) - 1] = 0f
                previousCalorieValues[calendar.get(Calendar.DAY_OF_WEEK) - 1] = 0f
            }

            saveData() // Verileri kaydet

            // TextView'leri sıfırla
            val stepCountTextView = requireView().findViewById<TextView>(R.id.stepCountTextView)
            stepCountTextView.text = "0"



            // Son kaydedilen günü güncelle
            val editor = sharedPreferences.edit()
            editor.putInt("key_last_saved_day", currentDay)
            editor.apply()
        }


        startClockUpdater() // onResume içinde zamanlayıcıyı başlat
        loadData()
    }

    override fun onPause() {
        super.onPause()
        running = false
        saveWaterData()
        sensorManager?.unregisterListener(this)
        val serviceIntent = Intent(requireContext(), StepCounterService::class.java)
        requireContext().stopService(serviceIntent)
        saveData()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // We do not have to write anything in this function for this app
    }
    // onSensorChanged fonksiyonunu güncelle
    override fun onSensorChanged(event: SensorEvent?) {
        if (running && event != null) {
            // Mevcut adımları güncelle
            val tvStepsTaken = requireView().findViewById<TextView>(R.id.stepCountTextView)
            val tvCaloriesBurned = requireView().findViewById<TextView>(R.id.kaloriCountTextView)

            if (previousTotalSteps == 0f) {
                previousTotalSteps = event.values[0]
            }

            totalSteps = event.values[0]
            val currentSteps = (totalSteps - previousTotalSteps).toInt()
            tvStepsTaken.text = currentSteps.toString()

            // Kalori hesapla
            val caloriesBurned = calculateCalories(currentSteps.toInt())
            tvCaloriesBurned.text = String.format("%.2f", caloriesBurned)

            // Önceki günlerin değerlerini güncelle
            updatePreviousValues(currentSteps)

            // Grafikleri yeniden oluştur
            val barChart = requireView().findViewById<BarChart>(R.id.bar_chart_steps)
            setupBarChart(barChart, previousStepValues, previousCalorieValues)

            // Kullanıcı e-posta adresini al
            val sharedPreferences = requireContext().getSharedPreferences("mySharedPreferences", Context.MODE_PRIVATE)
            val userEmail = sharedPreferences.getString("email", null)

            if (userEmail != null) {
                // Bugünün tarihini al
                val currentDate = java.sql.Date(Calendar.getInstance().timeInMillis)

                // Save current steps and calories
                DatabaseConnection().saveStepAndCalories(userEmail, currentSteps, caloriesBurned, currentDate) { success ->
                    if (success) {
                        println("Steps and calories saved successfully.")
                    } else {
                        println("Failed to save steps and calories.")
                    }
                }
            } else {
                println("User email not found in SharedPreferences.")
            }

            // StepCountViewModel'i güncelle
            stepCountViewModel?.setStepCount(currentSteps) ?: Log.e("HomeFragment", "StepCountViewModel is null")

            // Verileri kullanarak bar chart'ı güncelle
            val updatedStepValues = mutableListOf<Float>()
            val updatedCalorieValues = mutableListOf<Float>()

            for (i in 0 until 7) {
                // Bugünün indeksi (0-6 arası)
                val todayIndex = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 6) % 7 // Haftanın başlangıcı Pazartesi olarak alınıyor

                // İlgili günün indeksi
                val dayIndex = (i + 1) % 7 // Hafta başlangıcına göre bir sonraki günün indeksi

                if (dayIndex == todayIndex) {
                    // Bugünün değerini güncelle
                    previousStepValues[i] = currentSteps.toFloat()
                    previousCalorieValues[i] = caloriesBurned
                }

                updatedStepValues.add(previousStepValues[i])
                updatedCalorieValues.add(previousCalorieValues[i])
            }

            setupBarChart(barChart, updatedStepValues, updatedCalorieValues)
        }
    }


    private fun calculateCalories(steps: Int): Float {
        // 1 adım başına yaklaşık 0.05 kalori harcanır
        val caloriesPerStep = 0.05f
        return steps * caloriesPerStep
    }


    private fun updatePreviousValues(currentSteps: Int) {
        // Önceki günlerin değerlerini güncelle
        for (i in 0 until 7) {
            val todayIndex = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 6) % 7
            val dayIndex = (i + 1) % 7
            if (dayIndex == todayIndex) {
                previousStepValues[i] = currentSteps.toFloat()
                previousCalorieValues[i] = currentSteps * caloriesBurnedPerStep
            }
        }
    }


    private fun resetSteps() {
        val tvStepsTaken = requireView().findViewById<TextView>(R.id.stepCountTextView)
        val circularProgressBar = requireView().findViewById<CircularProgressBar>(R.id.stepCircularProgressBar)
        tvStepsTaken.setOnClickListener {
            Toast.makeText(requireContext(), "Long tap to reset steps", Toast.LENGTH_SHORT).show()
        }
        tvStepsTaken.setOnLongClickListener {
            // Tüm değerler sıfırlansın
            totalSteps = 0f
            previousTotalSteps = 0f
            previousStepValues.fill(0f)
            previousCalorieValues.fill(0f)

            tvStepsTaken.text = "0"
            circularProgressBar.setProgressWithAnimation(0f) // Adım ilerleme çubuğunu sıfırla

            // Kalori sayısını ve ilerleme çubuğunu sıfırla


            saveData() // Verileri kaydet
            true
        }
    }

    private fun saveData() {
        val sharedPreferences = requireActivity().getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putFloat("key_total_steps", totalSteps)
        editor.putFloat("key_previous_total_steps", previousTotalSteps)

        // Save previous step values
        for (i in 0 until 7) {
            editor.putFloat("key_previous_step_$i", previousStepValues[i])
            editor.putFloat("key_previous_calorie_$i", previousCalorieValues[i])
        }

        editor.apply()
    }


    private fun loadData() {
        val sharedPreferences = requireActivity().getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        totalSteps = sharedPreferences.getFloat("key_total_steps", 0f)
        previousTotalSteps = sharedPreferences.getFloat("key_previous_total_steps", 0f)

        // Load previous step values
        for (i in 0 until 7) {
            previousStepValues[i] = sharedPreferences.getFloat("key_previous_step_$i", 0f)
            previousCalorieValues[i] = sharedPreferences.getFloat("key_previous_calorie_$i", 0f)
        }
    }


    private fun fillCircularProgressBarBackgrounds() {
        // Adım ilerleme çubuğunun arka planını doldur
        val circularProgressBar = requireView().findViewById<CircularProgressBar>(R.id.stepCircularProgressBar)
        circularProgressBar.backgroundProgressBarColor = ContextCompat.getColor(requireContext(), R.color.white)

        // Kalori ilerleme çubuğunun arka planını doldur

    }
    private fun updateDateViews() {
        // Tarih bilgisini güncelle
        val currentDayOfMonth = currentDate.get(Calendar.DAY_OF_MONTH)
        val currentMonth = currentDate.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
        monthYearTextView.text = "$currentDayOfMonth $currentMonth"
        val currentDayOfWeek = currentDate.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())
        dayTextView.text = currentDayOfWeek
        dateAdapter.updateDates(generateDates(currentDate))
        scrollToCurrentDay()
    }

    private fun showDatePicker() {
        val calendarForDialog = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            requireContext(), // Context sağlanıyor
            { _, year, month, dayOfMonth ->
                currentDate.set(year, month, dayOfMonth)
                val formattedDate = if (currentDate.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH)) {
                    // Display only day and month for current month
                    "${currentDate.get(Calendar.DAY_OF_MONTH)} " +
                            "${currentDate.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())}"
                } else {
                    // Display day, month, and year for other months
                    "${currentDate.get(Calendar.DAY_OF_MONTH)} " +
                            "${currentDate.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())} " +
                            "${currentDate.get(Calendar.YEAR)}"
                }
                monthYearTextView.text = formattedDate
                val currentDayOfWeek = currentDate.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())
                dayTextView.text = currentDayOfWeek // dayTextView güncelle
                dateAdapter.updateDates(generateDates(currentDate))
                scrollToCurrentDay()
            },
            calendarForDialog.get(Calendar.YEAR),
            calendarForDialog.get(Calendar.MONTH),
            calendarForDialog.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }



    private fun scrollToCurrentDay() {
        val currentDayOfMonth = currentDate.get(Calendar.DAY_OF_MONTH)
        val currentMonth = currentDate.get(Calendar.MONTH)

        val position = dateAdapter.dates.indexOfFirst {
            val calendar = Calendar.getInstance()
            calendar.time = it
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH)
            dayOfMonth == currentDayOfMonth && month == currentMonth
        }

        if (position != -1) {
            recyclerViewDates.scrollToPosition(position)
        }
    }

    private fun generateDates(calendar: Calendar): List<Date> {
        val dates = mutableListOf<Date>()
        val clonedCalendar = calendar.clone() as Calendar
        clonedCalendar.set(Calendar.DAY_OF_MONTH, 1)
        val maxDayOfMonth = clonedCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (i in 1..maxDayOfMonth) {
            clonedCalendar.set(Calendar.DAY_OF_MONTH, i)
            dates.add(clonedCalendar.time)
        }
        return dates
    }

    class DateAdapter(var dates: List<Date>) :
        RecyclerView.Adapter<DateAdapter.DateViewHolder>() {
        private val sdfDayOfWeek = SimpleDateFormat("EEE", Locale.getDefault())
        private val sdfDayOfMonth = SimpleDateFormat("dd", Locale.getDefault())

        fun updateDates(newDates: List<Date>) {
            dates = newDates
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_date, parent, false)
            return DateViewHolder(view)
        }

        override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
            val date = dates[position]
            val calendar = Calendar.getInstance()
            calendar.time = date
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            val currentMonthOfItem = calendar.get(Calendar.MONTH)

            holder.textViewDayOfWeek.text = sdfDayOfWeek.format(date)
            holder.textViewDayOfMonth.text = sdfDayOfMonth.format(date)

            val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            val currentMonth = Calendar.getInstance().get(Calendar.MONTH)

            // Set text color to white for the selected day
            if (currentMonthOfItem == currentMonth && dayOfMonth == currentDay) {
                holder.textViewDayOfWeek.setTextColor(Color.WHITE)
                holder.textViewDayOfMonth.setTextColor(Color.WHITE)
            } else {
                // For other days, set text color to black
                holder.textViewDayOfWeek.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.gri3))
                holder.textViewDayOfMonth.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.black2))
            }

            // Set background color based on selected or unselected state
            if (currentMonthOfItem == currentMonth && dayOfMonth == currentDay) {
                // If it's the current day, change background color
                holder.itemView.background = getRoundedBackground(holder.itemView.context, R.color.custom_blue)
            } else {
                // For other days, set default background color
                holder.itemView.background = getRoundedBackground(holder.itemView.context, R.color.white3)
            }
        }



        private fun getRoundedBackground(context: Context, colorResId: Int): Drawable {
            val shapeDrawable = ShapeDrawable()
            val radius = context.resources.getDimension(R.dimen.corner_radius)
            shapeDrawable.shape = RoundRectShape(
                floatArrayOf(radius, radius, radius, radius, radius, radius, radius, radius),
                null,
                null
            )
            shapeDrawable.paint.color = ContextCompat.getColor(context, colorResId)
            val width = context.resources.getDimensionPixelSize(R.dimen.item_width)
            val height = context.resources.getDimensionPixelSize(R.dimen.item_height)
            shapeDrawable.setBounds(35, 35, width, height)
            return shapeDrawable
        }

        override fun getItemCount(): Int {
            return dates.size
        }

        inner class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textViewDayOfWeek: TextView = itemView.findViewById(R.id.textViewDayOfWeek)
            val textViewDayOfMonth: TextView = itemView.findViewById(R.id.textViewDayOfMonth)
        }
    }
}

