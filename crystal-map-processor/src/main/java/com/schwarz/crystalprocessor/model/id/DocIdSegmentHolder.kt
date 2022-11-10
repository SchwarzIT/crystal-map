package com.schwarz.crystalprocessor.model.id

import com.schwarz.crystalprocessor.model.source.SourceMemberFunction

class DocIdSegmentHolder(private val memberFunction: SourceMemberFunction) {

    val name = memberFunction.name

    val type = memberFunction.returnTypeName
}
