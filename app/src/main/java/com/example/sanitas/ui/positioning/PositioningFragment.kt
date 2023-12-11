package com.example.sanitas.ui.positioning

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.sanitas.R
import com.example.sanitas.databinding.FragmentPositioningBinding
import com.example.sanitas.services.LocationService
import com.here.sdk.core.Color
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.GeoPolyline
import com.here.sdk.core.Location
import com.here.sdk.core.errors.InstantiationErrorException
import com.here.sdk.mapview.MapImageFactory
import com.here.sdk.mapview.MapMarker
import com.here.sdk.mapview.MapMeasure
import com.here.sdk.mapview.MapPolyline
import com.here.sdk.mapview.MapScheme
import com.here.sdk.mapview.MapView


@Suppress("DEPRECATION")
class PositioningFragment : Fragment() {
    private val TAG = "PositioningFragment"
    private var _binding: FragmentPositioningBinding? = null
    private val binding get() = _binding!!

    // UI related properties
    private lateinit var mapView: MapView
    private var currentLocationMarker: MapMarker? = null
    private var displayPolyline: MapPolyline? = null
    private var isTracking = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentPositioningBinding.inflate(inflater, container, false)
        val root: View = binding.root


        // ViewModel operations
        val positioningViewModel =
            ViewModelProvider(this)[PositioningViewModel::class.java]

//        positioningViewModel.setupProvider(requireContext())
        positioningViewModel.location.observe(viewLifecycleOwner) {
            updateLocationMarker(it)
        }
        positioningViewModel.tracked.observe(viewLifecycleOwner) { coordinates ->
            updateMapPolyline(coordinates)
        }

        // MapView init
        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)


        // Handle track button click
        val trackBtn = binding.trackButton
        if (LocationService.isTracking) {
            trackBtn.text = "Stop"
        } else {
            trackBtn.text = "Track"
        }
        trackBtn.setOnClickListener {
//            if (trackBtn.text.equals("Track")) {
//                trackBtn.text = getString(R.string.stop_button)
//                trackBtn.setBackgroundColor(android.graphics.Color.YELLOW)
//            } else {
//                trackBtn.text = getString(R.string.track_button)
//                trackBtn.setBackgroundColor(android.graphics.Color.BLUE)
//            }

            if (!LocationService.isTracking) {
                trackBtn.text = "Stop"
                ActivityCompat.requestPermissions(requireActivity(),
                    arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
                requireActivity().startService(Intent(context, LocationService::class.java).apply {
                    action = LocationService.ACTION_START
                })
            }
            else{
                trackBtn.text = "Track"
                requireActivity().startService(Intent(context, LocationService::class.java).apply {
                    action = LocationService.ACTION_STOP
                })
            }
            LocationService.isTracking = !LocationService.isTracking
//            positioningViewModel.switchTracking()
        }

        loadMapScene()

        return root
    }


    private fun loadMapScene() {
        // Load a scene from the HERE SDK to render the map with a map scheme.
        mapView.mapScene.loadScene(
            MapScheme.NORMAL_DAY
        ) { mapError ->
            if (mapError == null) {
                val distanceInMeters = (1000 * 10).toDouble()
                val mapMeasureZoom = MapMeasure(MapMeasure.Kind.DISTANCE, distanceInMeters)
                mapView.camera.lookAt(
                    GeoCoordinates(21.0378124, 105.7638597), mapMeasureZoom
                )
            } else {
                Log.d("loadMapScene()", "Loading map failed: mapError: " + mapError.name)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        mapView.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        mapView.onDestroy()
        super.onDestroyView()
        _binding = null
    }


    private fun updateLocationMarker(location: Location?) {
        if (currentLocationMarker == null) {
            if (location != null) {
                currentLocationMarker = MapMarker(
                    location.coordinates,
                    MapImageFactory.fromResource(requireContext().resources, R.drawable.pinpoint)
                )
                mapView.mapScene.addMapMarker(currentLocationMarker!!)
            }
        }
        else if (location != null) {
            currentLocationMarker!!.coordinates = location.coordinates
            mapView.camera.lookAt(
                location.coordinates,
                MapMeasure(MapMeasure.Kind.DISTANCE, (1000 * 3).toDouble())
            )
        }
    }


    private fun updateMapPolyline(coordinates: ArrayList<GeoCoordinates>) {
        try {
            val geoPolyline = GeoPolyline(coordinates)
            Log.i(TAG, "-> init geoPolyline success")
            val widthInPixel = 20.0
            val lineColor = Color.valueOf(0f, 0.56f, 0.54f, 0.63f) //RGBA

            displayPolyline?.let { mapView.mapScene.removeMapPolyline(it) }
            displayPolyline = MapPolyline(geoPolyline, widthInPixel, lineColor)
            mapView.mapScene.addMapPolyline(displayPolyline!!)
        } catch (exception: InstantiationErrorException) {
            exception.printStackTrace()
            return
        }

    }
}