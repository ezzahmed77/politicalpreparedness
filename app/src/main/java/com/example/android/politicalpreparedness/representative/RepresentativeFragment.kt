package com.example.android.politicalpreparedness.representative

import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentRepresentativeBinding
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.adapter.RepresentativeListAdapter
import com.example.android.politicalpreparedness.representative.model.Representative
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import java.io.Serializable
import java.util.Locale

class DetailFragment : Fragment() {

    companion object {
       private const val REQUEST_LOCATION_CODE = 101
    }
    // For using current location of user
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var locationOfUser  = LatLng(0.0, 0.0)

    private lateinit var binding: FragmentRepresentativeBinding
    private lateinit var arrayAdapterForSpinner: ArrayAdapter<CharSequence>


    private lateinit var viewModel : RepresentativeViewModel

    // List of representatives to be saved with onSavedInstanceState
    private var listOfRepresentatives : List<Representative> = listOf()
    private var listOfRepresentativesGet : List<Representative> = listOf()
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentRepresentativeBinding.inflate(inflater)
        viewModel = ViewModelProvider(this).get(RepresentativeViewModel::class.java)

        // Connecting viewModel and lifecycle
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        // For spinner
        val spinner = binding.state
        arrayAdapterForSpinner = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.states_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }

        // Check if there is data in bundle of savedState
        if(savedInstanceState != null){
            binding.addressLine1.setText(savedInstanceState.getString(ADDRESS_LINE_1))
            binding.addressLine2.setText(savedInstanceState.getString(ADDRESS_LINE_2))
            binding.city.setText(savedInstanceState.getString(CITY))
            binding.zip.setText(savedInstanceState.getString(ZIP))
            binding.state.setSelection(arrayAdapterForSpinner.getPosition(savedInstanceState.getString(STATE)))

            listOfRepresentativesGet = savedInstanceState.getSerializable(LIST_OF_REPRESENTATIVES) as List<Representative>
            // Setting representatives in viewModel
            viewModel.setRepresentatives(listOfRepresentativesGet)

            binding.motionLayout.transitionToState(savedInstanceState.getInt(MOTION_LAYOUT_STATE))
            binding.executePendingBindings()
        }

        // Clicking on UseMyLocationButton
        binding.buttonLocation.setOnClickListener { checkLocationPermissions() }

        // setting onclickListener for find my representative button
        binding.buttonSearch.setOnClickListener {
            if(getAddressFromUser() != null){
                viewModel.setAddressOfUser(getAddressFromUser()!!)
                viewModel.getRepresentativeResponse()
                hideKeyboard()

            }
            else{
                Toast.makeText(requireContext(), "Fill Out Fields!!", Toast.LENGTH_LONG).show()
            }
        }

        // Getting Representative adapter and set it to recyclerView
        val representativeListAdapter = RepresentativeListAdapter()
        binding.representativesRecycleView.adapter = representativeListAdapter

        // Adding observe for representatives
        viewModel.representatives.observe(viewLifecycleOwner, Observer {
            representativeListAdapter.submitList(it)
            listOfRepresentatives = it
        })

        binding.representativesRecycleView.setOnTouchListener { _, event ->
            binding.motionLayout.onTouchEvent(event)
            return@setOnTouchListener false
        }

        // For getting current location of user
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations){
                    getLocation()
                }
            }
        }

        return binding.root

    }


    // Methods related to find my representative button
    private fun getAddressFromUser(): Address?{
        val line1 = binding.addressLine1.text.toString()
        val line2 = binding.addressLine2.text.toString()
        val city = binding.city.text.toString()
        val state = binding.state.selectedItem
        val zip = binding.zip.text.toString()

        return if(line1.isNotEmpty() && city.isNotEmpty() && state!= null && zip.isNotEmpty()){
            Address(line1, line2, city, state.toString(), zip)
        }else{
            null
        }
    }

    // Methods Related to getting user location!
    private fun startLocationUpdates() {
        if(isPermissionGranted()){
            fusedLocationClient.requestLocationUpdates(
                LocationRequest(),
                locationCallback,
                Looper.getMainLooper())
        }
    }

    private fun checkLocationPermissions() {
        if(isPermissionGranted()){
            getLocation()
        }
        else{
            Toast.makeText(requireContext(), "Please Enable Location", Toast.LENGTH_LONG).show()
            requestPermissions( arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Log.i("ResultPermission", "onRequestPermissionsResult was called")
        if(requestCode == REQUEST_LOCATION_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.i("ResultPermission", "Show CurrentLocation")
                Toast.makeText(requireContext(), "Result Permission granted", Toast.LENGTH_LONG).show()
                getLocation()
            }
        }
        else{
            Toast.makeText(requireContext(), "Please Enable Location For better usage of the app", Toast.LENGTH_LONG).show()
        }
    }

    private fun isPermissionGranted() : Boolean {
        if(ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        return true
    }

    private fun getLocation() {
        val task = fusedLocationClient.lastLocation
        task.addOnSuccessListener { location: Location?->
            if(location != null){
                // Get Location successfully
                locationOfUser = LatLng(location.latitude, location.longitude)
                // convert it to locationReadable
                val locationAddress = geoCodeLocation(locationOfUser)
                // Set location details with binding
                viewModel.setAddressOfUser(locationAddress)
                // Set the spinner value to state
                binding.state.setSelection(arrayAdapterForSpinner.getPosition(viewModel.addressOfUser.value!!.state))
                // Get representative Response
                viewModel.getRepresentativeResponse()


            }
            else{
                Toast.makeText(requireContext(), "Error in Getting Location ", Toast.LENGTH_SHORT).show()
                startLocationUpdates()
            }

        }

    }

    private fun geoCodeLocation(location: LatLng): Address {
        val geocoder = Geocoder(context, Locale.getDefault())
        return geocoder.getFromLocation(location.latitude, location.longitude, 1)
            .map { address ->
                Address(address.thoroughfare, address.subThoroughfare, address.locality, address.adminArea, address.postalCode)
            }
            .first()
    }

    // Method to Hide Keyboard
    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view!!.windowToken, 0)
    }


    // Overriding onSavedInstance to store the user input
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(ADDRESS_LINE_1, binding.addressLine1.text.toString())
        outState.putString(ADDRESS_LINE_2, binding.addressLine2.text.toString())
        outState.putString(CITY, binding.city.text.toString())
        outState.putString(ZIP, binding.zip.text.toString())
        outState.putString(STATE, binding.state.selectedItem.toString())
        // For State of motionLayout
        outState.putInt(MOTION_LAYOUT_STATE, binding.motionLayout.currentState)

        outState.putSerializable(LIST_OF_REPRESENTATIVES, listOfRepresentatives as Serializable)

    }

}

private var ADDRESS_LINE_1 = "addressLine1"
private var ADDRESS_LINE_2 = "addressLine2"
private var CITY = "city"
private var ZIP = "zip"
private var STATE = "state"
private var LIST_OF_REPRESENTATIVES = "list_of_representatives"
private var MOTION_LAYOUT_STATE = "motion_layout_state"






