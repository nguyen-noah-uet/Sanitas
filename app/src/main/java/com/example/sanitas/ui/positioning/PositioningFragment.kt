package com.example.sanitas.ui.positioning

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.sanitas.R
import com.example.sanitas.databinding.FragmentPositioningBinding

class PositioningFragment : Fragment() {

    companion object {
        fun newInstance() = PositioningFragment()
    }

    private lateinit var viewModel: PositioningViewModel
    private lateinit var binding: FragmentPositioningBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPositioningBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(PositioningViewModel::class.java)
        // TODO: Use the ViewModel
        viewModel.text.observe(viewLifecycleOwner){
            binding.textView.text = it
        }
    }

}