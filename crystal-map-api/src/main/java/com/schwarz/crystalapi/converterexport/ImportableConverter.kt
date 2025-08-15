package com.schwarz.crystalapi.converterexport

import com.schwarz.crystalapi.ITypeConverter
import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
annotation class ImportableConverter(val typeConverterInstanceTargetDefinition: TargetDefinition,
                                     val domainTargetDefinition: TargetDefinition,
                                     val mapTargetDefinition: TargetDefinition,
                                     val genericsTargetDefinitions: Array<TargetDefinition>)
