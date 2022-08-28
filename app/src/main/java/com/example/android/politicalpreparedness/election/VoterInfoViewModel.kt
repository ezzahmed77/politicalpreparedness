package com.example.android.politicalpreparedness.election

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse
import kotlinx.coroutines.launch

class VoterInfoViewModel(private val dataSource: ElectionDao) : ViewModel() {

    // for VoterInfoResponse
    private val _voterInfoResponse = MutableLiveData<VoterInfoResponse>()
    val voterInfoResponse : LiveData<VoterInfoResponse>
    get() = _voterInfoResponse


    // For the location
    private val _locationString = MutableLiveData<String>()
    val locationString : LiveData<String>
        get() = _locationString
    // for Election Id
    private var electionId = 0

    // For saved Election
    private val _savedElection = MutableLiveData<Election>()
    val savedElection : LiveData<Election>
        get() = _savedElection


    fun setLocationAndElectionIdAndGetVoterResponse(location: String, Id : Int){
        _locationString.value = location
        electionId = Id
        getVoterInfoResponse()
    }



    private fun getVoterInfoResponse(){
        try{
            viewModelScope.launch {
                val response = CivicsApi.retrofitService.getVoterInfo(_locationString.value!!,electionId)
                _voterInfoResponse.value = response
            }
        }catch (e: Exception){
            Log.i("Error", "Getting Data From Retrofit")
        }

    }

    fun checkElectionByIdFromDatabase(electionId: Int) {
        viewModelScope.launch {
            _savedElection.value = dataSource.getElectionById(electionId)
        }
    }

    // methods to save and delete election from database
    fun saveElection(election : Election){
        viewModelScope.launch {
            dataSource.insertElectionItem(election)
            checkElectionByIdFromDatabase(election.id)
        }
    }
    fun deleteElection(election: Election){
        viewModelScope.launch {
            dataSource.deleteElection(election)
            checkElectionByIdFromDatabase(election.id)
        }
    }


}