package com.schwarz.crystalcore.model.id

import com.schwarz.crystalcore.model.source.SourceMemberFunction

class DocIdSegmentHolder(private val memberFunction: SourceMemberFunction) {
    val name = memberFunction.name

    val type = memberFunction.returnTypeName
}
