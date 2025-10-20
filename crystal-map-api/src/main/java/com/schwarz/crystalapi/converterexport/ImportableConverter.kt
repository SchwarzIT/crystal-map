package com.schwarz.crystalapi.converterexport

@Retention(AnnotationRetention.RUNTIME)
annotation class ImportableConverter(
    val typeConverterInstanceTargetDefinition: TargetDefinition,
    val domainTargetDefinition: TargetDefinition,
    val mapTargetDefinition: TargetDefinition,
    val genericsTargetDefinitions: Array<TargetDefinition>
)
