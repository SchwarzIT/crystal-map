package com.schwarz.crystalcore.model.entity

import com.schwarz.crystalcore.model.source.ISourceModel

class WrapperEntityHolder<T>(val modifierOpen: Boolean, sourceModel: ISourceModel<T>) : BaseEntityHolder<T>(sourceModel) {
    override val entitySimpleName: String
        get() = sourceClazzSimpleName + "Wrapper"
}
