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


@RequiresApi(Build.VERSION_CODES.O)
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!


    private val dashboardViewModel by viewModels<DashboardViewModel> {
        DashboardViewModelFactory((activity?.application as SanitasApp).stepsRepository)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

        val stepTextView = binding.StepEditText
        dashboardViewModel.steps.observe(viewLifecycleOwner) {
            stepTextView.text = it.toString()
        }

        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}