package com.example.sanitas.ui.register

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.sanitas.R
import com.example.sanitas.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {

    companion object {
        fun newInstance() = RegisterFragment()
    }

    private  var viewModel: RegisterViewModel? = null
    private lateinit var binding: FragmentRegisterBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterBinding.inflate(layoutInflater, container, false)
        viewModel = ViewModelProvider(this).get(RegisterViewModel::class.java)
        binding.loginButton.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_register_to_navigation_login)
        }
        binding.registerButton.setOnClickListener {
            Toast.makeText(activity, "Register successfully, go to login", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_navigation_register_to_navigation_login)
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel = null
    }


}