package com.example.android.politicalpreparedness.representative

import android.app.Application
import android.location.Geocoder
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide.init

import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.network.models.RepresentativeResponse
import com.example.android.politicalpreparedness.representative.model.Representative
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch


class RepresentativeViewModel : ViewModel() {



    private var _representativesResponse = MutableLiveData<RepresentativeResponse>()
    val representativeResponse : LiveData<RepresentativeResponse>
        get() = _representativesResponse

    private var _representatives = MutableLiveData<List<Representative>>()
    val representatives : LiveData<List<Representative>>
        get() = _representatives

    // For address
    private var _addressOfUser = MutableLiveData<Address>()
    val addressOfUser : LiveData<Address>
        get() = _addressOfUser


    fun setAddressOfUser(address: Address){
        _addressOfUser.value = address
    }

    // function to set the list of representatives to show in recyclerView
    // This method will be used for rotation or terminating the application
    fun setRepresentatives(list : List<Representative>){
        _representatives.value = list
    }

     fun getRepresentativeResponse() {
        viewModelScope.launch {
            val response = CivicsApi.retrofitService.getRepresentatives(_addressOfUser.value!!.toFormattedString())
            _representativesResponse.value = response

            val offices = _representativesResponse.value!!.offices
            val officials = _representativesResponse.value!!.officials
            _representatives.value = offices.flatMap { office -> office.getRepresentatives(officials) }

        }
     }

}

