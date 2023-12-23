package com.example.sanitas.ui.login

import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.sanitas.R
import com.example.sanitas.SanitasApp
import com.example.sanitas.databinding.FragmentLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase

@Suppress("DEPRECATION")
class LoginFragment : Fragment() {

    companion object {
        private const val TAG = "LoginFragment"
        private const val RC_SIGN_IN = 20
    }

    private lateinit var binding: FragmentLoginBinding;
    private var viewModel: LoginViewModel? = null
    private lateinit var navController: NavController
    private lateinit var loginBtn: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentLoginBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        navController = findNavController()
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail().build()
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)


        binding.googleLoginButton.setOnClickListener {
            googleSignIn();
        }
        binding.guestLoginButton.setOnClickListener {
            Toast.makeText(activity, "Chào mừng!", Toast.LENGTH_SHORT).show()
            navController.navigate(R.id.action_navigation_login_to_navigation_dashboard)
        }
        return binding.root
    }

    private fun googleSignIn() {
        val intent = mGoogleSignInClient.signInIntent
        startActivityForResult(intent, RC_SIGN_IN)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: android.content.Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account =
                    task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                firebaseAuth(account.idToken)
            } catch (e: com.google.android.gms.common.api.ApiException) {
                Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun firebaseAuth(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener() { task: Task<AuthResult?> ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val map = HashMap<String, Any?>()
                    val email = user!!.email;
                    val name = user.displayName;
                    val photo = user.photoUrl.toString();

                    map["email"] = email
                    map["name"] = name
                    map["photo"] = photo
                    database.reference.child("Users").child(user.uid).setValue(map)
                    SanitasApp.userDisplayName = name
                    SanitasApp.userEmail = email
                    SanitasApp.userPhotoUrl = photo
                    Toast.makeText(activity, "Chào mừng $name!", Toast.LENGTH_SHORT).show()
                    navController.navigate(R.id.action_navigation_login_to_navigation_dashboard)
                } else {
                    Toast.makeText(requireContext(), "Authentication failed.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        viewModel = null
        val navView = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
        if (navView != null)
            navView.visibility = View.VISIBLE
    }
}