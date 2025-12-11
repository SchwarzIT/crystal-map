package com.schwarz.crystalapi.converterexport

@Retention(AnnotationRetention.RUNTIME)
annotation class TargetDefinition(
    val pkg: String,
    val name: String,
)
