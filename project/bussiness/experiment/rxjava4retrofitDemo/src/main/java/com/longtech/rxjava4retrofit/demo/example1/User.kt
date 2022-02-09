package com.longtech.rxjava4retrofit.demo.example1

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class User (
    @SerializedName("userId")
    var userId: String? = null,
    @SerializedName("userName")
    var userName: String? = null
) : Serializable