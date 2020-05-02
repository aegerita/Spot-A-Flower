package com.example.spot_a_flower

import java.text.SimpleDateFormat
import java.util.*

class Flower {
    var name: String
    var detail: String

    constructor(name: String, time: Long) {
        this.name = name
        val sdf = SimpleDateFormat("hh:mm:ss MM/dd", Locale.CANADA)
        this.detail = sdf.format(Date(time))
    }

    constructor(name: String, probability: Int) {
        this.name = name
        this.detail = "$probability% Probability"
    }
}