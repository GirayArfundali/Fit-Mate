import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spor.BilgilerimActivity
import com.example.spor.DatabaseConnection
import com.example.spor.MainActivity
import com.example.spor.R
import com.example.spor.UserDetails
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class AdminFragment : Fragment() {

    private lateinit var nameTextView: TextView
    private lateinit var ageTextView: TextView
    private lateinit var heightTextView: TextView
    private lateinit var weightTextView: TextView
    private lateinit var exitButton: Button
    private lateinit var bilgilerimButton: Button
    private lateinit var profileImage: ImageView
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_PICK = 2
    private var selectedImageBitmap: Bitmap? = null

    // Takvim için gerekli değişkenler
    private val sdfMonthYear = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val sdfDayOfWeek = SimpleDateFormat("EEE", Locale.getDefault())
    private val sdfDayOfMonth = SimpleDateFormat("dd", Locale.getDefault())
    private lateinit var monthYearTextView: TextView
    private lateinit var dayTextView: TextView
    private lateinit var recyclerViewDates: RecyclerView
    private lateinit var dateAdapter: DateAdapter
    private var currentDate = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin, container, false)

        // Kullanıcı detaylarını al
        val userDetails: UserDetails? = arguments?.getParcelable("userDetails")

        // TextView'leri bağla
        nameTextView = view.findViewById(R.id.nameTextView)
        ageTextView = view.findViewById(R.id.ageTextView)
        heightTextView = view.findViewById(R.id.heightTextView)
        weightTextView = view.findViewById(R.id.weightTextView)
        exitButton = view.findViewById(R.id.exitButton)
        bilgilerimButton = view.findViewById(R.id.buton3)
        profileImage = view.findViewById(R.id.profileImage)

        // Takvim için View'leri bağla
        monthYearTextView = view.findViewById(R.id.monthYearTextView)
        dayTextView = view.findViewById(R.id.dayTextView)
        recyclerViewDates = view.findViewById(R.id.recyclerViewDates)
        val calendarIcon = view.findViewById<View>(R.id.calendarIcon)

        recyclerViewDates.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        dateAdapter = DateAdapter(generateDates(currentDate))
        recyclerViewDates.adapter = dateAdapter

        // Update date views
        updateDateViews()

        calendarIcon.setOnClickListener {
            showDatePicker()
        }

        // UserDetails nesnesinin boş olup olmadığını kontrol et
        userDetails?.let {
            // UserDetails nesnesi boş değilse, TextView'lere yazdır
            nameTextView.text = it.name
            ageTextView.text = it.age.toString()
            heightTextView.text = it.height.toString()
            weightTextView.text = it.weight.toString()

            // Profil resmini yükle
            loadProfileImage(userDetails.email)

            // Profil resmi seçme veya fotoğraf çekme işlemleri için izin kontrolü
            profileImage.setOnClickListener {
                showPictureDialog()
            }

            // Çıkış butonuna click dinleyici ekle
            exitButton.setOnClickListener {
                val intent = Intent(activity, MainActivity::class.java)
                startActivity(intent)
                activity?.finish()
            }

            // Bilgilerim butonuna click dinleyici ekle
            bilgilerimButton.setOnClickListener {
                // BilgilerimActivity'ye geçiş yap
                val intent = Intent(requireContext(), BilgilerimActivity::class.java)
                startActivity(intent)
            }
        }

        return view
    }

    // Takvim işlevselliği
    private fun updateDateViews() {
        val currentDayOfMonth = currentDate.get(Calendar.DAY_OF_MONTH)
        val currentMonth =
            currentDate.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
        monthYearTextView.text = "$currentDayOfMonth $currentMonth"
        val currentDayOfWeek =
            currentDate.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())
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
                val formattedDate = if (currentDate.get(Calendar.MONTH) == Calendar.getInstance()
                        .get(Calendar.MONTH)
                ) {
                    // Display only day and month for current month
                    "${currentDate.get(Calendar.DAY_OF_MONTH)} " +
                            "${
                                currentDate.getDisplayName(
                                    Calendar.MONTH,
                                    Calendar.LONG,
                                    Locale.getDefault()
                                )
                            }"
                } else {
                    // Display day, month, and year for other months
                    "${currentDate.get(Calendar.DAY_OF_MONTH)} " +
                            "${
                                currentDate.getDisplayName(
                                    Calendar.MONTH,
                                    Calendar.LONG,
                                    Locale.getDefault()
                                )
                            } " +
                            "${currentDate.get(Calendar.YEAR)}"
                }
                monthYearTextView.text = formattedDate
                val currentDayOfWeek = currentDate.getDisplayName(
                    Calendar.DAY_OF_WEEK,
                    Calendar.LONG,
                    Locale.getDefault()
                )
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
                holder.textViewDayOfWeek.setTextColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.gri3
                    )
                )
                holder.textViewDayOfMonth.setTextColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.black2
                    )
                )
            }

            // Set background color based on selected or unselected state
            if (currentMonthOfItem == currentMonth && dayOfMonth == currentDay) {
                // If it's the current day, change background color
                holder.itemView.background =
                    getRoundedBackground(holder.itemView.context, R.color.custom_blue)
            } else {
                // For other days, set default background color
                holder.itemView.background =
                    getRoundedBackground(holder.itemView.context, R.color.white3)
            }
        }

        override fun getItemCount(): Int {
            return dates.size
        }

        class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textViewDayOfWeek: TextView = itemView.findViewById(R.id.textViewDayOfWeek)
            val textViewDayOfMonth: TextView = itemView.findViewById(R.id.textViewDayOfMonth)
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
            return shapeDrawable
        }
    }



    // Fotoğraf seçme veya çekme için dialog gösterimi
    private fun showPictureDialog() {
        val items = arrayOf<CharSequence>(
            getString(R.string.take_photo),
            getString(R.string.choose_from_library),
            getString(R.string.cancel)
        )
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.add_photo))
        builder.setItems(items) { dialog, item ->
            when {
                items[item] == getString(R.string.take_photo) -> {
                    dispatchTakePictureIntent()
                }

                items[item] == getString(R.string.choose_from_library) -> {
                    dispatchPickPictureIntent()
                }

                items[item] == getString(R.string.cancel) -> {
                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }

    // Kamera açma işlemi
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.CAMERA
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.CAMERA),
                        REQUEST_IMAGE_CAPTURE
                    )
                } else {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    // Galeriyi açma işlemi
    private fun dispatchPickPictureIntent() {
        val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickIntent.type = "image/*"
        startActivityForResult(pickIntent, REQUEST_IMAGE_PICK)
    }

    // İzin sonuçları
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_IMAGE_CAPTURE || requestCode == REQUEST_IMAGE_PICK) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (requestCode == REQUEST_IMAGE_CAPTURE) {
                    dispatchTakePictureIntent()
                } else if (requestCode == REQUEST_IMAGE_PICK) {
                    dispatchPickPictureIntent()
                }
            }
        }
    }

    // Seçilen resmi imageview'e ekleme ve veritabanına kaydetme
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    selectedImageBitmap = getRoundedBitmap(imageBitmap)
                    profileImage.setImageBitmap(selectedImageBitmap)
                    saveImageToDatabase(imageBitmap)
                }

                REQUEST_IMAGE_PICK -> {
                    val selectedImage = data?.data
                    selectedImage?.let {
                        val bitmap = MediaStore.Images.Media.getBitmap(
                            requireActivity().contentResolver,
                            it
                        )
                        selectedImageBitmap = getRoundedBitmap(bitmap)
                        profileImage.setImageBitmap(selectedImageBitmap)
                        saveImageToDatabase(bitmap)
                    }
                }
            }
        }
    }


    // Bitmap'i yuvarlak yapma işlemi
    // Bitmap'i yuvarlak yapma işlemi
    private fun getRoundedBitmap(bitmap: Bitmap): Bitmap {
        // Galeriden seçilen fotoğrafın boyutlarına getir
        val selectedImageBitmapResized = Bitmap.createScaledBitmap(
            bitmap,
            selectedImageBitmap!!.width,
            selectedImageBitmap!!.height,
            true
        )
        val output = Bitmap.createBitmap(
            selectedImageBitmapResized.width,
            selectedImageBitmapResized.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(output)

        val color = Color.RED
        val paint = Paint()
        val rect = Rect(0, 0, selectedImageBitmapResized.width, selectedImageBitmapResized.height)

        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        canvas.drawCircle(
            selectedImageBitmapResized.width / 2f,
            selectedImageBitmapResized.height / 2f,
            selectedImageBitmapResized.width / 2f,
            paint
        )
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(selectedImageBitmapResized, rect, rect, paint)

        return output
    }


    // Fragment sonlandığında seçilen resmi saklama
    override fun onPause() {
        super.onPause()
        selectedImageBitmap?.let {
            saveImageToInternalStorage(it)
        }
    }

    // Seçilen resmi dahili depolamaya kaydetme
    private fun saveImageToInternalStorage(bitmap: Bitmap) {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()
        val sharedPreferences =
            requireContext().getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("image", byteArray.toString())
        editor.apply()
    }

    // Dahili depolamadan resmi yükleme
    private fun loadImageFromInternalStorage(): Bitmap? {
        val sharedPreferences =
            requireContext().getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val encodedImage = sharedPreferences.getString("image", "")
        if (encodedImage != null && encodedImage.isNotEmpty()) {
            val decodedString = encodedImage.toByteArray()
            return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        }
        return null
    }

    // Fragment yeniden yüklendiğinde resmi geri yükleme
    override fun onResume() {
        super.onResume()
        val bitmap = loadImageFromInternalStorage()
        bitmap?.let {
            profileImage.setImageBitmap(getRoundedBitmap(it))
            selectedImageBitmap = it
        }
    }

    // Seçilen resmi veritabanına kaydetme
    private fun saveImageToDatabase(bitmap: Bitmap) {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()

        // DatabaseConnection sınıfını kullanarak resmi kaydet
        val userDetails: UserDetails? = arguments?.getParcelable("userDetails")
        userDetails?.let {
            val databaseConnection = DatabaseConnection()
            databaseConnection.updateProfileImage(userDetails.email, byteArray) { success ->
                if (success) {
                    println("Profil resmi başarıyla güncellendi.")
                } else {
                    println("Profil resmi güncelleme başarısız.")
                }
            }
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



    inner class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewDayOfWeek: TextView = itemView.findViewById(R.id.textViewDayOfWeek)
        val textViewDayOfMonth: TextView = itemView.findViewById(R.id.textViewDayOfMonth)
    }


    private fun loadProfileImage(email: String) {
        val databaseConnection = DatabaseConnection()
        databaseConnection.getUserImage(email) { imageByteArray ->
            requireActivity().runOnUiThread {
                if (imageByteArray != null) {
                    val bitmap =
                        BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.size)
                    selectedImageBitmap = bitmap
                    profileImage.setImageBitmap(getRoundedBitmap(bitmap))
                }
            }
        }
    }
}


