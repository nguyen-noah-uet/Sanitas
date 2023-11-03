package com.example.sanitas.ui.login

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.sanitas.R
import com.example.sanitas.databinding.FragmentLoginBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class LoginFragment : Fragment() {

    companion object {
        fun newInstance() = LoginFragment()
    }
    private lateinit var binding: FragmentLoginBinding;
    private var viewModel: LoginViewModel? = null
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentLoginBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        navController = findNavController()

        binding.loginButton.setOnClickListener {
            Toast.makeText(activity, "hilu", Toast.LENGTH_SHORT).show()
            navController.navigate(R.id.action_navigation_login_to_navigation_dashboard)
        }
        binding.registerButton.setOnClickListener {
            navController.navigate(R.id.action_navigation_login_to_navigation_register)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        when (context){
//            is MainActivity -> {
//                val navView = context.navView
//                if (navView != null)
//                    navView.visibility = View.GONE
//            }
//        }
//    }
    override fun onDestroyView() {
        super.onDestroyView()
        viewModel =null
        val navView = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
        if (navView != null)
            navView.visibility = View.VISIBLE
    }
}