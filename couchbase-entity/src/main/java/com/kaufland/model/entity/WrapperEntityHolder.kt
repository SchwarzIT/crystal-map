package com.kaufland.model.entity

import com.kaufland.model.source.ISourceModel

class WrapperEntityHolder(val modifierOpen: Boolean, sourceModel: ISourceModel) : BaseEntityHolder(sourceModel) {

    override val entitySimpleName: String
        get() = sourceClazzSimpleName + "Wrapper"
}
