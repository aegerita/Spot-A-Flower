package com.example.spot_a_flower

import java.text.SimpleDateFormat
import java.util.*

class Flower {
    var name: String
    var detail: String
    var int: Int

    constructor(name: String, time: Long) {
        this.name = name
        val sdf = SimpleDateFormat("HH:mm:ss MM/dd", Locale.CANADA)
        this.detail = sdf.format(Date(time))
        this.int = 0
    }

    constructor(name: String?, detail: String?) {
        this.name = name ?: "Hello?"
        this.detail = detail ?: "Sorry anyone here?"
        this.int = 0
    }

    constructor(name: String, probability: Int) {
        this.name = name
        this.detail = "$probability% Probability"
        this.int = probability
    }
}