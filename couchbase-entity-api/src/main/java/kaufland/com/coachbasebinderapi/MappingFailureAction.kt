package kaufland.com.coachbasebinderapi

import kotlin.reflect.KClass

interface TypeConversionErrorCallback {
    fun invokeOnError(ex: Exception, value: Any?, `class`: KClass<*>)
}