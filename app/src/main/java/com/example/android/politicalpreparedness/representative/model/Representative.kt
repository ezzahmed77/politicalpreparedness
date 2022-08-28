package com.example.android.politicalpreparedness.representative.model

import android.os.Parcelable
import androidx.room.Entity
import com.example.android.politicalpreparedness.network.models.Office
import com.example.android.politicalpreparedness.network.models.Official
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Parcelize
data class Representative (
        val official:  Official,
        val office:  Office
): Parcelable