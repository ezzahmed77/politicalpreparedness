package com.example.android.politicalpreparedness.election

import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.databinding.FragmentElectionBinding
import com.example.android.politicalpreparedness.election.adapter.ElectionListAdapter
import com.example.android.politicalpreparedness.election.adapter.ElectionListener
import com.example.android.politicalpreparedness.network.models.Address
import com.google.android.gms.maps.model.LatLng
import java.util.*

class ElectionsFragment: Fragment() {

    private lateinit var viewModel: ElectionsViewModel
    private lateinit var viewModelFactory: ElectionsViewModelFactory

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = FragmentElectionBinding.inflate(inflater)
        binding.lifecycleOwner = this

        // Getting viewModel
        val dataSource = ElectionDatabase.getInstance(requireActivity().applicationContext).electionDao
        viewModelFactory = ElectionsViewModelFactory(dataSource)
        viewModel = ViewModelProvider(this, viewModelFactory).get(ElectionsViewModel::class.java)

        // Get saved elections to refresh data from database
        viewModel.getSavedElectionsFromDatabase()
        binding.electionsViewModel = viewModel

        // Getting adapter
        val electionsAdapter = ElectionListAdapter(ElectionListener {election->
            val action = ElectionsFragmentDirections.actionElectionsFragmentToVoterInfoFragment(election.id,election.division, election)
            findNavController().navigate(action)
        })

        val electionsAdapterSaved = ElectionListAdapter(ElectionListener {election->
            val action = ElectionsFragmentDirections.actionElectionsFragmentToVoterInfoFragment(election.id,election.division, election)
            findNavController().navigate(action)
        })

        // Setting adapters for recyclerViews
        binding.upcomingElectionsRecyclerView.adapter = electionsAdapter
        binding.savedElectionsRecyclerView.adapter = electionsAdapterSaved


        return binding.root
    }
}