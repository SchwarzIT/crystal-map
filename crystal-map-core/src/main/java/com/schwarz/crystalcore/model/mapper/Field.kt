package com.schwarz.crystalcore.model.mapper

import com.schwarz.crystalcore.model.source.IClassModel
import com.schwarz.crystalcore.model.source.ISourceMapify

data class Field<T>(var field: IClassModel<T>, var mapify: ISourceMapify)
