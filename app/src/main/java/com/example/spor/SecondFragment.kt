package com.example.spor

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager

class SecondFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var nestedScrollView: NestedScrollView
    private var viewPager: ViewPager? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_second, container, false)

        // ImageView'e ilk resmi atama
        val imageView: ImageView = view.findViewById(R.id.imageView)
        imageView.setImageResource(R.drawable.triceps)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Burada tıklama işleyicisini ayarlayarak MyRecyclerViewAdapter'ı oluşturun
        recyclerView.adapter = MyRecyclerViewAdapter(generateData()) { position ->
            val selectedDay = generateData()[position]
            val fragment = DayDetailFragment2.newInstance(selectedDay)
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        // Divider eklemek için bir DividerItemDecoration nesnesi oluşturun
        val divider = DividerItemDecoration(
            recyclerView.context,
            LinearLayoutManager.VERTICAL
        )
        // Divider'ın drawable'ını belirleyin (opsiyonel)
        val drawable: Drawable? = ContextCompat.getDrawable(requireContext(), R.drawable.horizontal_divider)
        drawable?.let {
            divider.setDrawable(it)
        }
        // RecyclerView'e Divider ekleyin
        recyclerView.addItemDecoration(divider)

        // NestedScrollView'i bulun
        nestedScrollView = view.findViewById(R.id.nestedScrollView)
        // RecyclerView'un yuvarlak kenarlarına sahip olduğundan emin olun
        nestedScrollView.isNestedScrollingEnabled = true

        // ViewPager'ı alın
        viewPager = requireActivity().findViewById<ViewPager>(R.id.viewPager)

        return view
    }

    override fun onResume() {
        super.onResume()
        viewPager?.addOnPageChangeListener(onPageChangeListener)
    }

    override fun onPause() {
        super.onPause()
        viewPager?.removeOnPageChangeListener(onPageChangeListener)
    }

    private val onPageChangeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) {}

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

        override fun onPageSelected(position: Int) {
            recyclerView.scrollToPosition(0)
            nestedScrollView.post { nestedScrollView.scrollTo(0, 0) }
        }
    }

    private fun generateData(): List<String> {
        val data = mutableListOf<String>()
        for (i in 1..31) {
            data.add("${i}. Gün")
        }
        return data
    }
}
