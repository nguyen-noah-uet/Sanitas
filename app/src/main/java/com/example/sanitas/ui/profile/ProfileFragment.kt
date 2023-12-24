package com.example.sanitas.ui.profile

import android.graphics.BitmapFactory
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.findNavController
import com.example.sanitas.R
import com.example.sanitas.SanitasApp
import com.example.sanitas.databinding.FragmentProfileBinding
import java.net.URL

class ProfileFragment : Fragment() {

    private lateinit var viewModel: ProfileViewModel
    private lateinit var binding: FragmentProfileBinding

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        if(SanitasApp.userDisplayName != null) {
            binding.userDisplayName.text = SanitasApp.userDisplayName
        }
        if (SanitasApp.userEmail != null) {
            if (SanitasApp.userEmail == "local") {
                binding.userEmail.text = getString(R.string.guest)
            } else {
                binding.userEmail.text = SanitasApp.userEmail
            }
        }
        if (SanitasApp.userPhotoUrl != null){
            try{
                val url = URL(SanitasApp.userPhotoUrl)
                val bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                binding.userAvatar.setImageBitmap(bmp)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.logoutButton.setOnClickListener {
            SanitasApp.userDisplayName = null
            SanitasApp.userEmail = null
            SanitasApp.userPhotoUrl = null
            SanitasApp.currentSteps = 0
            SanitasApp.measuredHeartBeat = 0.0

            Toast.makeText(activity, getString(R.string.logged_out_text), Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_navigation_profile_to_navigation_login)
        }
        return binding.root
    }



    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        // TODO: Use the ViewModel
    }

}