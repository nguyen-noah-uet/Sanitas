package com.example.sanitas.ui.dashboard
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment

import androidx.fragment.app.viewModels
import com.example.sanitas.SanitasApp
import com.example.sanitas.databinding.FragmentDashboardBinding
import androidx.lifecycle.ViewModelProvider
import com.example.sanitas.dataprocessing.StepMonitor
import kotlin.math.round



@RequiresApi(Build.VERSION_CODES.O)
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val dashboardViewModel by viewModels<DashboardViewModel> {
        DashboardViewModelFactory((activity?.application as SanitasApp).stepsRepository)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

        val stepTextView = binding.StepEditText
        dashboardViewModel.steps.observe(viewLifecycleOwner) {
            stepTextView.text = "Steps: $it"
        }
        dashboardViewModel.heartBeat.observe(viewLifecycleOwner) {
            binding.heartBeatTextView.text = String.format("%.2f BPM", it)
        }
        dashboardViewModel.calories.observe(viewLifecycleOwner) {
            binding.caloriesTextView.text = String.format("%.2f CAL", it)
        }

        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}