package com.example.spor

import MyPagerAdapter2
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout

class AntrenmanFragment : Fragment() {

    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_antrenman, container, false)

        viewPager = view.findViewById(R.id.viewPager)
        tabLayout = view.findViewById(R.id.tabLayout)

        val adapter = MyPagerAdapter2(childFragmentManager)
        adapter.addFragment(FirstFragment(), "Ön Kol Antrenmanı")
        adapter.addFragment(SecondFragment(), "Arka Kol Antrenmanı")
        adapter.addFragment(ThreeFragment(), "Omuz Antrenmanı")
        adapter.addFragment(SevenFragment(), "Göğüs Antrenmanı")
        adapter.addFragment(FourFragment(), "Karın Antrenmanı")
        adapter.addFragment(FiveFragment(), "Bacak Antrenmanı")
        adapter.addFragment(SixFragment(), "Tüm Vücut Antrenmanı")

        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Geri tuşuna basıldığında hiçbir şey yapma
            }
        })
    }
}
