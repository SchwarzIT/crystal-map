package kaufland.com.coachbasebinderapi

interface TypeConversionErrorCallback {
    fun invokeOnError(errorWrapper: TypeConversionErrorWrapper)
}
