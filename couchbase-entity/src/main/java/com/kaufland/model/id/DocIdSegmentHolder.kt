package com.kaufland.model.id

import com.kaufland.model.source.SourceMemberFunction

class DocIdSegmentHolder(private val memberFunction: SourceMemberFunction) {

    val name = memberFunction.name

    val type = memberFunction.returnTypeName
}
