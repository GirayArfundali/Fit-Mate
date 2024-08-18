package com.example.spor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class BmıFragment : Fragment() {
    private lateinit var heightEditText: EditText
    private lateinit var weightEditText: EditText
    private lateinit var resultTextView: TextView
    private lateinit var calculateButton: Button

    // Vücut Yağ Oranı Girişleri
    private lateinit var heightEditText2: EditText
    private lateinit var neckCircumferenceEditText: EditText
    private lateinit var waistCircumferenceEditText: EditText
    private lateinit var hipCircumferenceEditText: EditText
    private lateinit var genderRadioGroup: RadioGroup
    private lateinit var calculateBodyFatButton: Button
    private lateinit var bodyFatResultTextView: TextView

    // İdeal Kilo ve Cinsiyet Bilgileri
    private lateinit var heightEditText3: EditText
    private lateinit var idealWeightEditText: EditText
    private lateinit var genderRadioGroup2: RadioGroup
    private lateinit var calculateIdealWeightButton: Button
    private lateinit var idealWeightResultTextView: TextView

    // Günlük Su İhtiyacı Girişleri
    private lateinit var weightEditText2: EditText
    private lateinit var calculateWaterNeedButton: Button
    private lateinit var waterNeedResultTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bmi, container, false)

        heightEditText = view.findViewById(R.id.editTextHeight)
        weightEditText = view.findViewById(R.id.editTextWeight)
        resultTextView = view.findViewById(R.id.resultTextView)
        calculateButton = view.findViewById(R.id.btnCalculateBMI)

        // Vücut Yağ Oranı Girişleri
        heightEditText2 = view.findViewById(R.id.editTextHeight2)
        neckCircumferenceEditText = view.findViewById(R.id.editTextNeckCircumference)
        waistCircumferenceEditText = view.findViewById(R.id.editTextWaistCircumference)
        hipCircumferenceEditText = view.findViewById(R.id.editTextHipCircumference)
        genderRadioGroup = view.findViewById(R.id.radioGroupGender)
        calculateBodyFatButton = view.findViewById(R.id.btnCalculateBodyFat)
        bodyFatResultTextView = view.findViewById(R.id.bodyFatResultTextView)

        // İdeal Kilo ve Cinsiyet Bilgileri
        heightEditText3 = view.findViewById(R.id.editTextHeight3)
        idealWeightEditText = view.findViewById(R.id.editTextIdealWeight)
        genderRadioGroup2 = view.findViewById(R.id.radioGroupGender2)
        calculateIdealWeightButton = view.findViewById(R.id.btnCalculateIdealWeight3)
        idealWeightResultTextView = view.findViewById(R.id.idealWeightResultTextView)

        // Günlük Su İhtiyacı Girişleri
        weightEditText2 = view.findViewById(R.id.editTextWeight2)
        calculateWaterNeedButton = view.findViewById(R.id.btnCalculateWaterNeed)
        waterNeedResultTextView = view.findViewById(R.id.waterNeedResultTextView)

        calculateButton.setOnClickListener {
            onCalculateButtonClicked()
        }

        calculateBodyFatButton.setOnClickListener {
            onCalculateBodyFatButtonClicked()
        }

        calculateIdealWeightButton.setOnClickListener {
            onCalculateIdealWeightButtonClicked()
        }

        calculateWaterNeedButton.setOnClickListener {
            onCalculateWaterNeedButtonClicked()
        }

        return view
    }

    private fun onCalculateButtonClicked() {
        val heightStr = heightEditText.text.toString()
        val weightStr = weightEditText.text.toString()

        if (heightStr.isNotEmpty() && weightStr.isNotEmpty()) {
            val height = heightStr.toDouble() / 100 // cm to meters
            val weight = weightStr.toDouble()

            val bmi = calculateBMI(height, weight)
            val bmiStatus = getBMIStatus(bmi)

            val bmiMessage = "BMI: %.2f (%s)".format(bmi, bmiStatus)
            resultTextView.text = bmiMessage
        } else {
            showToast("Lütfen boy ve kilonuzu giriniz.")
        }
    }

    private fun calculateBMI(height: Double, weight: Double): Double {
        return weight / (height * height)
    }

    private fun getBMIStatus(bmi: Double): String {
        return when {
            bmi < 18.5 -> "Zayıf"
            bmi < 24.9 -> "Normal"
            bmi < 29.9 -> "Fazla Kilolu"
            else -> "Obez"
        }
    }

    private fun onCalculateBodyFatButtonClicked() {
        val heightStr = heightEditText2.text.toString()
        val waistStr = waistCircumferenceEditText.text.toString()
        val neckStr = neckCircumferenceEditText.text.toString()
        val hipStr = hipCircumferenceEditText.text.toString()

        if (heightStr.isNotEmpty() && waistStr.isNotEmpty() && neckStr.isNotEmpty() && hipStr.isNotEmpty()) {
            if (genderRadioGroup.checkedRadioButtonId != -1) { // Check if a gender is selected
                val height = heightStr.toDouble()
                val waist = waistStr.toDouble()
                val neck = neckStr.toDouble()
                val hip = hipStr.toDouble()
                val gender = when (genderRadioGroup.checkedRadioButtonId) {
                    R.id.radioButtonMale -> "Erkek"
                    else -> "Kadın"
                }

                val bodyFatPercentage = calculateBodyFatPercentage(height, waist, neck, hip, gender)
                val bodyFatStatus = getBodyFatStatus(bodyFatPercentage)
                val bodyFatMessage = "Vücut Yağ Oranı: %.2f%% (%s)".format(bodyFatPercentage, bodyFatStatus)
                bodyFatResultTextView.text = bodyFatMessage
            } else {
                showToast("Lütfen cinsiyetinizi seçiniz.")
            }
        } else {
            showToast("Lütfen tüm bilgileri giriniz.")
        }
    }

    private fun getBodyFatStatus(bodyFatPercentage: Double): String {
        return when {
            bodyFatPercentage < 18.5 -> "Zayıf"
            bodyFatPercentage < 24.9 -> "Ortalama"
            else -> "Obez"
        }
    }

    private fun calculateBodyFatPercentage(height: Double, waist: Double, neck: Double, hip: Double, gender: String): Double {
        return if (gender == "Erkek") {
            495 / (1.0324 - 0.19077 * Math.log10(waist - neck) + 0.15456 * Math.log10(height)) - 450
        } else {
            495 / (1.29579 - 0.35004 * Math.log10(waist + hip - neck) + 0.22100 * Math.log10(height)) - 450
        }
    }

    private fun onCalculateIdealWeightButtonClicked() {
        val heightStr = heightEditText3.text.toString()
        val idealWeightStr = idealWeightEditText.text.toString()

        if (heightStr.isNotEmpty() && idealWeightStr.isNotEmpty()) {
            if (genderRadioGroup2.checkedRadioButtonId != -1) { // Check if a gender is selected
                val height = heightStr.toDouble()
                val idealWeight = idealWeightStr.toDouble()

                val gender = when (genderRadioGroup2.checkedRadioButtonId) {
                    R.id.radioButtonMale2 -> "Erkek"
                    else -> "Kadın"
                }

                val idealWeightCalculation = calculateIdealWeight(height, idealWeight, gender)
                val idealWeightMessage = "İdeal Kilonuz: %.2f".format(idealWeightCalculation)
                idealWeightResultTextView.text = idealWeightMessage
            } else {
                showToast("Lütfen cinsiyetinizi seçiniz.")
            }
        } else {
            showToast("Lütfen boy ve kilonuzu giriniz.")
        }
    }


    private fun calculateIdealWeight(height: Double, idealWeight: Double, gender: String): Double {
        return if (gender == "Erkek") {
            50 + 2.3 * (height - 152.4) / 2.54
        } else {
            45.5 + 2.3 * (height - 152.4) / 2.54
        }
    }

    private fun calculateWaterNeed(weight: Double): Double {
        return weight * 0.033 // Günlük su ihtiyacı formülüne göre
    }

    private fun onCalculateWaterNeedButtonClicked() {
        val weightStr = weightEditText2.text.toString()

        if (weightStr.isNotEmpty()) {
            val weight = weightStr.toDouble()

            val waterNeed = calculateWaterNeed(weight)
            val waterNeedMessage = "Günlük Su İhtiyacınız: %.2f litre".format(waterNeed)
            waterNeedResultTextView.text = waterNeedMessage
        } else {
            showToast("Lütfen kilonuzu giriniz.")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
