package com.schwarz.crystalapi

interface TypeConversionErrorCallback {
    fun invokeOnError(errorWrapper: TypeConversionErrorWrapper)
}
