package com.example.sanitas.ui.heartmonitor

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.sanitas.databinding.FragmentHeartmonitorBinding

class HeartMonitorFragment : Fragment() {

    companion object {
        fun newInstance() = HeartMonitorFragment()
    }

    private lateinit var viewModel: HeartMonitorViewModel
    private lateinit var binding: FragmentHeartmonitorBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHeartmonitorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(HeartMonitorViewModel::class.java)
        // TODO: Use the ViewModel
        viewModel.text.observe(viewLifecycleOwner){
            binding.textView.text = it
        }
    }

}