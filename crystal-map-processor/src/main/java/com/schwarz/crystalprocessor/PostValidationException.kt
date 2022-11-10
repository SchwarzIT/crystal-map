package com.schwarz.crystalprocessor

import java.lang.Exception
import javax.lang.model.element.Element

class PostValidationException : Exception {

    var causingElements: List<Element> = emptyList()

    constructor(message: String, vararg causingElement: Element) : super(message) {
        this.causingElements = causingElement.toList()
    }

    constructor(throwable: Throwable, vararg causingElement: Element) : super(throwable) {
        this.causingElements = causingElement.toList()
    }
}
