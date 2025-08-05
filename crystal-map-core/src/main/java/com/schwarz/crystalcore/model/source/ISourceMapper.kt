package com.schwarz.crystalcore.model.source

import com.schwarz.crystalcore.model.mapper.Field
import com.schwarz.crystalcore.model.mapper.GetterSetter

interface ISourceMapper<T> : IClassModel<T> {

    val typeParams: List<ISourceDeclaringName>

    val declaringName: ISourceDeclaringName

    val fields: Map<String, Field<T>>

    val getterSetters: Map<String, GetterSetter<T>>
}
