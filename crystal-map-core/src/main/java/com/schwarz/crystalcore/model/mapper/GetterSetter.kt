package com.schwarz.crystalcore.model.mapper

import com.schwarz.crystalapi.mapify.Mapify
import com.schwarz.crystalcore.model.source.IClassModel

class GetterSetter<T>() {
    var getterElement: IClassModel<T>? = null
    var setterElement: IClassModel<T>? = null
    var mapify: Mapify? = null

    fun getterName(): String {
        return getterElement!!.sourceClazzSimpleName
    }

    fun getterInternalAccessor(): String {
        return "a${getterName().capitalize()}"
    }

    fun setterName(): String {
        return setterElement!!.sourceClazzSimpleName
    }

    fun setterInternalAccessor(): String {
        return "a${setterName().capitalize()}"
    }
}
