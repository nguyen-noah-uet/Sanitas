package com.example.sanitas.ui.positioning

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.sanitas.R
import com.example.sanitas.SanitasApp
import com.example.sanitas.databinding.FragmentPositioningBinding
import com.example.sanitas.services.LocationService
import com.example.sanitas.ui.pickers.DatePickerFragment
import com.here.sdk.core.Color
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.GeoPolyline
import com.here.sdk.core.Location
import com.here.sdk.mapview.MapImageFactory
import com.here.sdk.mapview.MapMarker
import com.here.sdk.mapview.MapMeasure
import com.here.sdk.mapview.MapPolyline
import com.here.sdk.mapview.MapScheme
import com.here.sdk.mapview.MapView
import java.time.LocalDateTime


@RequiresApi(Build.VERSION_CODES.O)
@Suppress("DEPRECATION")
class PositioningFragment : Fragment() {

    private var _binding: FragmentPositioningBinding? = null
    private val binding get() = _binding!!

    private val positioningViewModel: PositioningViewModel by viewModels {
        PositioningViewModelFactory((activity?.application as SanitasApp).travelRouteRepository)
    }

    // Handle multiple fragment states
    enum class State {
        TRACKING, STOPPING, HISTORY
    }

    private var state = State.STOPPING


    // UI related properties
    private lateinit var mapView: MapView
    private var currentLocationMarker: MapMarker? = null
    private var displayPolylines = mutableListOf<MapPolyline>()

    private lateinit var trackBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentPositioningBinding.inflate(inflater, container, false)
        val root: View = binding.root

        if (!(ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 100
            )
        }else{
            Log.d("Permission", "Permission Granted")
            positioningViewModel.startLocation(requireContext(), this.requireActivity())
        }

        positioningViewModel.location.observe(viewLifecycleOwner) {
            updateLocationMarker(it)
        }

        positioningViewModel.tracked.observe(viewLifecycleOwner) { coordinates ->
            updateMapPolyline(coordinates)
        }

        // MapView init
        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)


        val historyBtn = binding.historyButton
        historyBtn.setOnClickListener {
            // DatePickerFragment -> onDateSet -> set datePicked -> positioningViewModel.loadHistoryTravelRouteById
            // -> update _tracked -> update historyTravelPolyline -> add historyTravelPolyline to mapView
            val datePickerFragment = DatePickerFragment { y: Int, m: Int, d: Int ->
                datePicked(y, m, d)
            }
            datePickerFragment.show(requireActivity().supportFragmentManager, "datePicker")
        }

        // Handle track button click
        trackBtn = binding.trackButton
        trackBtn.setOnClickListener {
            mapView.mapScene.removeMapPolylines(displayPolylines)
            when (state) {
                State.TRACKING -> {
                    trackBtn.text = getString(R.string.track_button)
                    trackBtn.setBackgroundColor(android.graphics.Color.BLUE)
                    historyBtn.isEnabled = true
                    state = State.STOPPING
                    positioningViewModel.switchTracking()
                }

                State.STOPPING -> {
                    trackBtn.text = getString(R.string.stop_button)
                    trackBtn.setBackgroundColor(android.graphics.Color.YELLOW)
                    historyBtn.isEnabled = false
                    state = State.TRACKING
                    positioningViewModel.switchTracking()
                }

                State.HISTORY -> {
                    trackBtn.text = getString(R.string.track_button)
                    trackBtn.setBackgroundColor(android.graphics.Color.BLUE)
                    historyBtn.isEnabled = true
                    state = State.STOPPING
                }
            }
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
        requireActivity().startService(Intent(context, LocationService::class.java).apply {
            action = LocationService.ACTION_STOP
        })
        mapView.onDestroy()
        super.onDestroyView()
        _binding = null
    }


    private fun datePicked(year: Int, month: Int, day: Int) {
        state = State.HISTORY
        val date = LocalDateTime.of(year, month, day, 0, 1, 0)
        positioningViewModel.loadHistoryTravelRouteByDate(date)

        // disable tracking
        trackBtn.text = getString(R.string.trackBtnBack)
        trackBtn.setBackgroundColor(android.graphics.Color.RED)
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
        } else if (location != null) {
            currentLocationMarker!!.coordinates = location.coordinates
        }
    }


    private fun updateMapPolyline(line: ArrayList<GeoCoordinates>) {
        val widthInPixels = 20.0
        val lineColor = Color.valueOf(0f, 0.56f, 0.54f, 0.63f)

        if (state == State.HISTORY) {
            val mapPolyline = MapPolyline(GeoPolyline(line), widthInPixels, lineColor)
            displayPolylines.add(mapPolyline)
            mapView.mapScene.addMapPolylines(displayPolylines)
        } else {
            mapView.mapScene.removeMapPolylines(displayPolylines)
            if (line.size >= 2) {
                val mapPolyline = MapPolyline(GeoPolyline(line), widthInPixels, lineColor)
                displayPolylines.clear()
                displayPolylines.add(mapPolyline)
                mapView.mapScene.addMapPolylines(displayPolylines)
            }
        }
    }
}