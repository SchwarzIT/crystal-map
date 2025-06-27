package com.schwarz.crystalcore

interface ILogger<T> {

    fun info(msg: String)

    fun info(msg: String, element: T?)

    fun warn(msg: String, element: T?)

    fun error(msg: String, element: T?)

    fun abortWithError(msg: String, element: T?, ex: Throwable?)

    fun hasErrors(): Boolean

    fun abortWithError(msg: String?, elements: List<T>)
}