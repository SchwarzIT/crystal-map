package com.kaufland.model.id

import com.kaufland.generation.TypeConversionMethodsGeneration
import com.kaufland.javaToKotlinType
import com.kaufland.model.deprecated.DeprecatedModel
import com.kaufland.model.entity.BaseEntityHolder
import com.kaufland.util.ConversionUtil
import com.kaufland.util.FieldExtractionUtil
import com.kaufland.util.TypeUtil
import com.squareup.kotlinpoet.*
import kaufland.com.coachbasebinderapi.DocId
import kaufland.com.coachbasebinderapi.DocIdSegment
import kaufland.com.coachbasebinderapi.Field
import org.apache.commons.lang3.text.WordUtils
import java.lang.StringBuilder
import java.util.regex.Pattern
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.type.TypeMirror


class DocIdSegmentHolder(docIdSegment: DocIdSegment, element: Element) {

    val name = element.simpleName.toString()

    val type = (element as? ExecutableElement)?.returnType?.asTypeName()?.javaToKotlinType()

    val paramsElements = (element as? ExecutableElement)?.parameters

}
