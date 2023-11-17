package com.example.sanitas.ui.heartmonitor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.sanitas.R
import com.example.sanitas.databinding.FragmentHeartmonitor1Binding
import com.example.sanitas.databinding.FragmentHeartmonitor2Binding
import com.example.sanitas.databinding.FragmentHeartmonitor3Binding

class HeartMonitorFragment1: Fragment() {
    private lateinit var binding: FragmentHeartmonitor1Binding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentHeartmonitor1Binding.inflate(inflater, container, false)


        binding.nextButton.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_heartonitor1_to_navigation_heartonitor2)
        }

        return binding.root
    }

}


class HeartMonitorFragment2: Fragment() {
    private lateinit var binding: FragmentHeartmonitor2Binding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentHeartmonitor2Binding.inflate(inflater, container, false)


        binding.nextButton.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_heartonitor2_to_navigation_heartonitor3)

        }

        return binding.root
    }

}


class HeartMonitorFragment3: Fragment() {
    private lateinit var binding: FragmentHeartmonitor3Binding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentHeartmonitor3Binding.inflate(inflater, container, false)


        binding.nextButton.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_heartonitor3_to_navigation_heartmonitor)

        }

        return binding.root
    }

}


