package com.kaufland.model.entity

class WrapperEntityHolder : BaseEntityHolder() {

    override val entitySimpleName: String
        get() = sourceClazzSimpleName + "Wrapper"
}
