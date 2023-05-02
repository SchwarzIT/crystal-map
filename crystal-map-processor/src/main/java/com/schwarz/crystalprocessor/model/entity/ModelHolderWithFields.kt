package com.schwarz.crystalprocessor.model.entity

interface ModelHolderWithFields {
    fun hasFieldWithName(name: String): Boolean
    fun hasFieldConstantWithName(name: String): Boolean
}
