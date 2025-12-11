package com.schwarz.crystalprocessor.model.source

import com.schwarz.crystalapi.query.Query
import com.schwarz.crystalcore.model.source.ISourceQuery

class SourceQuery(
    private val queryAnnotations: Query,
) : ISourceQuery {
    override val fields: Array<String> = queryAnnotations.fields
}
