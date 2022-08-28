package com.example.android.politicalpreparedness.network.jsonadapter

import android.annotation.SuppressLint
import com.example.android.politicalpreparedness.database.Converters
import com.example.android.politicalpreparedness.network.models.Division
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

class ElectionAdapter {
    @FromJson
    fun divisionFromJson (ocdDivisionId: String): Division {
        val countryDelimiter = "country:"
        val stateDelimiter = "state:"
        val districtDelimiter = "district:"
        val country = ocdDivisionId.substringAfter(countryDelimiter,"")
                .substringBefore("/")

        var state = ocdDivisionId.substringAfter(stateDelimiter,"")
                .substringBefore("/")
        // So if we have found that state returns empty string then it may be district not state
        // then we will set the value of state from district
        if(state.isEmpty()){
            state = ocdDivisionId.substringAfter(districtDelimiter,"")
                .substringBefore("/")
        }
        return Division(ocdDivisionId, country, state)
    }

    @ToJson
    fun divisionToJson (division: Division): String {
        return division.id
    }


    @SuppressLint("SimpleDateFormat")
    @FromJson
    fun dateFromJson(dateString: String): Date? {
        return SimpleDateFormat("yyyy-MM-dd").parse(dateString)
    }

    @SuppressLint("SimpleDateFormat")
    @ToJson
    fun dateToJson (date: Date): String {
        val simpleDateFormatter = SimpleDateFormat("yyyy-MM-dd")
        return simpleDateFormatter.format(date)
    }
}