package com.kaufland.model.entity

class WrapperEntityHolder(val modifierOpen: Boolean) : BaseEntityHolder() {

    override val entitySimpleName: String
        get() = sourceClazzSimpleName + "Wrapper"
}
