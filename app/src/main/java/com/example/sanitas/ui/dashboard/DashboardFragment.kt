package com.example.sanitas.ui.dashboard
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.sanitas.databinding.FragmentDashboardBinding
import com.example.sanitas.dataprocessing.StepMonitor

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private var stepMonitor = StepMonitor.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        binding.StepEditText.text = "Steps: ${StepMonitor.stepCounter}"
        stepMonitor.setOnStepDetectedCallback {
            if (_binding != null) {
                binding.StepEditText.text = "Steps: ${StepMonitor.stepCounter}"
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}