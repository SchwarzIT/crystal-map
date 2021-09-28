package kaufland.com.coachbasebinderapi

import kotlin.reflect.KClass

@Retention(AnnotationRetention.BINARY)
annotation class Field(val name: String = "", val type: KClass<out Any>, val list: Boolean = false, val defaultValue: String = "", val readonly: Boolean = false, val comment: Array<String> = [])
