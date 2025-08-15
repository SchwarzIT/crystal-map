package com.schwarz.crystalapi.converterexport

import com.schwarz.crystalapi.ClassNameDefinition
import com.schwarz.crystalapi.ITypeConverter
import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
annotation class TargetDefinition(val pkg: String,
                                  val name: String)
