package kaufland.com.coachbasebinderapi

import kotlin.reflect.KClass

data class TypeConversionErrorWrapper(
    val exception: Exception,
    val fieldName: String,
    val value: Any?,
    val `class`: KClass<*>
)
