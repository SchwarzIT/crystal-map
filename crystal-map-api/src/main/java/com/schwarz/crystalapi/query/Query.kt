package com.schwarz.crystalapi.query

@Retention(AnnotationRetention.BINARY)
annotation class Query(
    val fields: Array<String>,
)
