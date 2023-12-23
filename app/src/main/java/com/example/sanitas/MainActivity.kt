package com.example.sanitas

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.sanitas.databinding.ActivityMainBinding
import com.example.sanitas.workers.StepCounterWorker
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.here.sdk.core.engine.SDKNativeEngine
import com.here.sdk.core.engine.SDKOptions
import com.here.sdk.core.errors.InstantiationErrorException


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navView: BottomNavigationView
    private lateinit var permissionsRequester: PermissionsRequester

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
        initializeHERESDK()
        val workRequest = OneTimeWorkRequestBuilder<StepCounterWorker>().build()
        WorkManager.getInstance(this).enqueue(workRequest)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handleAndroidPermissions()

        navView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        //hello
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_dashboard,
                R.id.navigation_notifications,
                R.id.navigation_settings,
                R.id.navigation_positioning,
                R.id.navigation_heartmonitor
            )
        )
//        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, nd: NavDestination, _ ->
            if (nd.id == R.id.navigation_login || nd.id == R.id.navigation_register) {
                navView.visibility = View.GONE
            } else {
                navView.visibility = View.VISIBLE
            }
            Log.i("MainActivity", navView.visibility.toString())
        }

    }


    private fun handleAndroidPermissions() {
        permissionsRequester = PermissionsRequester(this)
        permissionsRequester.request(object : PermissionsRequester.ResultListener {
            override fun permissionsGranted() {
                // Do something
            }

            override fun permissionsDenied() {
                Log.e("permission", "Permissions denied by user.")
            }
        })
    }


    private fun initializeHERESDK() {
        // Set your credentials for the HERE SDK.
        val accessKeyID = "XVeUR0xrLiV4PtM8S9_Jlg"
        val accessKeySecret = "iDlaYLfxEAMBw61_U-BRKP35vaFVaHrDwi_vTQ-J9EJVyQIdYxo7gVtf2L9EaGnHDNjnKG5Hm4heCKG9wbQLJQ"
        val options = SDKOptions(accessKeyID, accessKeySecret)
        try {
            val context: Context = this
            SDKNativeEngine.makeSharedInstance(context, options)
        } catch (e: InstantiationErrorException) {
            throw RuntimeException("Initialization of HERE SDK failed: " + e.error.name)
        }
    }


    private fun disposeHERESDK() {
        // Free HERE SDK resources before the application shuts down.
        // Usually, this should be called only on application termination.
        // Afterwards, the HERE SDK is no longer usable unless it is initialized again.
        val sdkNativeEngine = SDKNativeEngine.getSharedInstance()
        if (sdkNativeEngine != null) {
            sdkNativeEngine.dispose()
            // For safety reasons, we explicitly set the shared instance to null to avoid situations,
            // where a disposed instance is accidentally reused.
            SDKNativeEngine.setSharedInstance(null)
        }
    }


    override fun onDestroy() {
        disposeHERESDK()
        super.onDestroy()
    }

}