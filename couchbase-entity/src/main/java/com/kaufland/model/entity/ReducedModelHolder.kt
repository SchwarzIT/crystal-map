package com.kaufland.model.entity

data class ReducedModelHolder(
    val namePrefix: String,
    val includedElements: List<String>,
    val includeQueries: Boolean,
    val includeAccessor: Boolean,
    val includeDocId: Boolean,
    val includeBasedOn: Boolean,
    private val parentModel: BaseEntityHolder
)
