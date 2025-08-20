package com.schwarz.crystalcore

import kotlin.Exception

class PostValidationException : Exception {

    var causingElements: Any? = null

//    constructor(message: String, vararg causingElement: Any) : super(message) {
//        this.causingElements = causingElement.toList()
//    }
//
//    constructor(throwable: Throwable, vararg causingElement: Any) : super(throwable) {
//        this.causingElements = causingElement.toList()
//    }

    constructor(throwable: Throwable, causingElement: Any) : super(throwable) {
        this.causingElements = causingElement
    }
}
