package com.kaufland.model.id

import com.kaufland.javaToKotlinType
import com.squareup.kotlinpoet.*
import kaufland.com.coachbasebinderapi.DocIdSegment
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement

class DocIdSegmentHolder(docIdSegment: DocIdSegment, element: Element) {

    val name = element.simpleName.toString()

    val type = (element as? ExecutableElement)?.returnType?.asTypeName()?.javaToKotlinType()

    val paramsElements = (element as? ExecutableElement)?.parameters
}
