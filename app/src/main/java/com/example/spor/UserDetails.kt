package com.example.spor

import android.os.Parcel
import android.os.Parcelable

data class UserDetails(
    val name: String,
    val age: Int,
    val height: Float,
    val weight: Float,
    val email: String,
    val phoneNumber: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readString() ?: "",
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeInt(age)
        parcel.writeFloat(height)
        parcel.writeFloat(weight)
        parcel.writeString(email)
        parcel.writeString(phoneNumber)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UserDetails> {
        override fun createFromParcel(parcel: Parcel): UserDetails {
            return UserDetails(parcel)
        }

        override fun newArray(size: Int): Array<UserDetails?> {
            return arrayOfNulls(size)
        }
    }
}
