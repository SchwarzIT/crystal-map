package com.schwarz.crystalapi.deprecated

@Retention(AnnotationRetention.BINARY)
annotation class DeprecatedField(
    val field: String,
    val replacedBy: String = "",
    val inUse: Boolean = true,
)
