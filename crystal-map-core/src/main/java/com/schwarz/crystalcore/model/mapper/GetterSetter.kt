package com.schwarz.crystalcore.model.mapper

import com.schwarz.crystalcore.model.source.IClassModel
import com.schwarz.crystalcore.model.source.ISourceMapify

class GetterSetter<T> {
    var getterElement: IClassModel<T>? = null
    var setterElement: IClassModel<T>? = null
    var mapify: ISourceMapify? = null

    fun getterName(): String = getterElement!!.sourceClazzSimpleName

    fun getterInternalAccessor(): String = "a${getterName().capitalize()}"

    fun setterName(): String = setterElement!!.sourceClazzSimpleName

    fun setterInternalAccessor(): String = "a${setterName().capitalize()}"
}
