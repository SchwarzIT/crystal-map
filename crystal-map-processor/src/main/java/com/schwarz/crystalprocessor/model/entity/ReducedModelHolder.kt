package com.schwarz.crystalprocessor.model.entity

data class ReducedModelHolder(
    val namePrefix: String,
    val includedElements: List<String>,
    val includeQueries: Boolean,
    val includeAccessor: Boolean,
    val includeDocId: Boolean,
    val includeBasedOn: Boolean,
    private val parentModel: BaseEntityHolder
): ModelHolderWithFields {
    override fun hasFieldWithName(name: String): Boolean = includedElements.contains(name)
    override fun hasFieldConstantWithName(name: String): Boolean = includedElements.contains(name)
}
