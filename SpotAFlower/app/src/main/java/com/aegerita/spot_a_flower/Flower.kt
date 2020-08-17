package com.aegerita.spot_a_flower

import java.text.SimpleDateFormat
import java.util.*

class Flower {
    var name: String
    var detail: String
    var int: Int = 0

    // for save and history
    constructor(name: String, time: Long) {
        this.name = name
        this.detail = SimpleDateFormat("MMM.d HH:mm", Locale.getDefault()).format(Date(time))
    }

    // for encyclopedia and database testings
    constructor(name: String?, detail: String?) {
        this.name = name ?: "Hello?"
        this.detail = detail ?: "Sorry anyone here?"
    }

    // for search
    constructor(name: String, probability: Int) {
        this.name = name
        this.detail = "$probability% Probability"
        this.int = probability
    }
}