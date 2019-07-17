package kaufland.com.coachbasebinderapi

import com.sun.org.apache.xpath.internal.operations.Bool
import java.lang.annotation.RetentionPolicy


import kotlin.reflect.KClass

@Retention(AnnotationRetention.BINARY)
annotation class Field(val name: String = "", val type: KClass<out Any>, val list: Boolean = false, val defaultValue: String = "", val readonly: Boolean = false)
