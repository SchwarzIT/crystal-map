package com.schwarz.crystalcore.model.mapper

import com.schwarz.crystalapi.mapify.Mapify
import com.schwarz.crystalcore.model.source.IClassModel

data class Field<T>(var field: IClassModel<T>, var mapify: Mapify)
