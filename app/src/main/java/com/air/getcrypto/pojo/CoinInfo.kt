package com.air.getcrypto.pojo

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class CoinInfo(
        //название криптовалюты
        @SerializedName("Name")
        @Expose
        val name: String? = null,

        )