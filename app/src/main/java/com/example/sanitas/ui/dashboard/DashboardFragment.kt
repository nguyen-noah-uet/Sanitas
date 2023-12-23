package com.example.sanitas.ui.dashboard
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.sanitas.SanitasApp
import com.example.sanitas.databinding.FragmentDashboardBinding
import com.example.sanitas.dataprocessing.StepMonitor
import kotlin.math.round

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private var stepMonitor = StepMonitor.getInstance()
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        binding.StepEditText.text = "Steps: ${SanitasApp.currentSteps}"
        binding.caloriesTextView.text = "${String.format("%.2f",SanitasApp.currentSteps * 0.37)} CAL"
        if (SanitasApp.measuredHeartBeat != 0.0)
            binding.heartBeatTextView.text = "${String.format("%.2f",SanitasApp.measuredHeartBeat)} BPM"

        stepMonitor.setOnStepDetectedCallback {
            if (_binding != null) {
                binding.StepEditText.text = "Steps: ${SanitasApp.currentSteps}"
                binding.caloriesTextView.text = "${String.format("%.2f",SanitasApp.currentSteps * 0.37)} CAL"
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}