package com.schwarz.crystalcore.model.entity

data class ReducedModelHolder<T>(
    val namePrefix: String,
    val includedElements: List<String>,
    val includeQueries: Boolean,
    val includeAccessor: Boolean,
    val includeDocId: Boolean,
    val includeBasedOn: Boolean,
    private val parentModel: BaseEntityHolder<T>
) : ModelHolderWithFields {
    override fun hasFieldWithName(name: String): Boolean = includedElements.contains(name)

    override fun hasFieldConstantWithName(name: String): Boolean = includedElements.contains(name)
}
