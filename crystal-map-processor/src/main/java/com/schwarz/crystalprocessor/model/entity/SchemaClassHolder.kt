package com.schwarz.crystalprocessor.model.entity

import com.schwarz.crystalprocessor.model.source.ISourceModel

class SchemaClassHolder(sourceModel: ISourceModel) : BaseEntityHolder(sourceModel) {

    override val entitySimpleName: String
        get() = sourceClazzSimpleName + "Schema"
}
