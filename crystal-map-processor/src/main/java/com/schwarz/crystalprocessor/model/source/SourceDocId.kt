package com.schwarz.crystalprocessor.model.source

import com.schwarz.crystalapi.DocId
import com.schwarz.crystalcore.model.source.ISourceDocId

class SourceDocId(docIdAnnotation: DocId) : ISourceDocId {
    override val value: String = docIdAnnotation.value
}