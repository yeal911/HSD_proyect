package com.hsd.contest.data.response

import com.google.gson.annotations.SerializedName

data class Temperaturas (

	@SerializedName("max") val max : Int,
	@SerializedName("min") val min : Int
)