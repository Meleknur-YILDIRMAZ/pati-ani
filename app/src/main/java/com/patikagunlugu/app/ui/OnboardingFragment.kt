package com.patikagunlugu.app.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.patikagunlugu.app.R
import com.patikagunlugu.app.databinding.FragmentOnboardingBinding
import com.patikagunlugu.app.databinding.ItemOnboardingBinding

class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val prefs = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("onboarding_done", false)) {
            findNavController().navigate(R.id.action_onboardingFragment_to_homeFragment)
        }
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViewPager()
        
        binding.btnNext.setOnClickListener {
            if (binding.viewPager.currentItem < 2) {
                binding.viewPager.currentItem += 1
            } else {
                finishOnboarding()
            }
        }
    }

    private fun setupViewPager() {
        val adapter = OnboardingAdapter()
        binding.viewPager.adapter = adapter
        // Connect TabLayout if needed, but for now we have custom indicators logic or just simple one
    }

    private fun finishOnboarding() {
        val prefs = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("onboarding_done", true).apply()
        findNavController().navigate(R.id.action_onboardingFragment_to_homeFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class OnboardingAdapter : RecyclerView.Adapter<OnboardingAdapter.ViewHolder>() {
        private val titles = listOf(
            R.string.onboarding_title_1,
            R.string.onboarding_title_2,
            R.string.onboarding_title_3
        )
        private val descs = listOf(
            R.string.onboarding_desc_1,
            R.string.onboarding_desc_2,
            R.string.onboarding_desc_3
        )
        // Images would be system icons for now
        private val images = listOf(
            android.R.drawable.ic_menu_camera,
            android.R.drawable.ic_menu_recent_history,
            android.R.drawable.btn_star_big_on
        )

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemOnboardingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.binding.tvTitle.setText(titles[position])
            holder.binding.tvDescription.setText(descs[position])
            holder.binding.ivOnboarding.setImageResource(images[position])
            holder.binding.ivOnboarding.setColorFilter(resources.getColor(R.color.colorPrimary, null))
        }

        override fun getItemCount(): Int = 3

        inner class ViewHolder(val binding: ItemOnboardingBinding) : RecyclerView.ViewHolder(binding.root)
    }
}
