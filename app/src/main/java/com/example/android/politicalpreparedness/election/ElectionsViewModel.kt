package com.example.android.politicalpreparedness.election

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide.init
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.CivicsHttpClient.Companion.API_KEY
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.ElectionResponse
import kotlinx.coroutines.launch

class ElectionsViewModel(private val dataSource : ElectionDao): ViewModel() {

    // For Upcoming Elections
    private var _upcomingElections = MutableLiveData<List<Election>>()
    val upcomingElections : LiveData<List<Election>>
        get() = _upcomingElections

    // For saved Elections
    private var _savedElections = MutableLiveData<List<Election>>()
    val savedElections : LiveData<List<Election>>
        get() = _savedElections


    init {
        // Get the upcoming elections from network
        getUpcomingElectionsFromNetwork()
        // Get Saved Elections
        getSavedElectionsFromDatabase()
    }

    private fun getUpcomingElectionsFromNetwork(){
        viewModelScope.launch {
            val electionResponse = CivicsApi.retrofitService.getElectionResponse()
            _upcomingElections.value = electionResponse.elections
        }

    }

    fun getSavedElectionsFromDatabase(){
        viewModelScope.launch {
            _savedElections.value = dataSource.getAllSavedElections() }
    }


}