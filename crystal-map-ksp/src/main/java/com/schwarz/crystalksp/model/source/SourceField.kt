package com.schwarz.crystalksp.model.source

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.schwarz.crystalcore.model.source.ISourceField
import com.schwarz.crystalcore.javaToKotlinType
import com.schwarz.crystalcore.util.TypeUtil.list
import com.schwarz.crystalcore.util.TypeUtil.map
import com.schwarz.crystalcore.util.TypeUtil.mapStringAnyNullable
import com.schwarz.crystalksp.util.getArgument
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.toClassName

class SourceField(val fieldAnnotation: KSAnnotation) : ISourceField {

    private val classArgument = fieldAnnotation.getArgument<KSType>("type")!!

    override val simpleName: String
        get() = classArgument.declaration.simpleName.asString()
    override val packageName: String
        get() = classArgument.declaration.packageName.asString()
    override val readonly: Boolean=fieldAnnotation.getArgument<Boolean>("readonly") ?: false
    override val name: String = fieldAnnotation.getArgument<String>("name") ?: ""
    override val list: Boolean = fieldAnnotation.getArgument<Boolean>("list") ?: false
    override val defaultValue: String = fieldAnnotation.getArgument<String>("defaultValue") ?: ""
    override val mandatory: Boolean = fieldAnnotation.getArgument<Boolean>("mandatory") ?: false
    override val comment: Array<String> = fieldAnnotation.getArgument<List<String>>("comment")?.toTypedArray() ?: arrayOf()

    override val fullQualifiedName: String
        get() = classArgument.declaration.qualifiedName!!.asString()

    override val javaToKotlinType = classArgument.toClassName().javaToKotlinType()
    override val baseType: TypeName = classArgument.toClassName()

    override fun parseMetaType(list: Boolean, subEntity: String?): TypeName {
        return parseMetaType(list, true, subEntity)
    }

    private fun parseMetaType(list: Boolean, convertMap: Boolean, subEntity: String?): TypeName {
        val simpleName = if (subEntity != null && subEntity.contains(simpleName)) subEntity else simpleName

        var baseType: TypeName?

        try {
            baseType = ClassName(packageName, simpleName)
        } catch (e: IllegalArgumentException) {
            baseType = this.baseType
        }

        if (convertMap && baseType!!.javaToKotlinType() == map()) {
            baseType = mapStringAnyNullable()
        }

        return if (list) {
            list(baseType!!.javaToKotlinType())
        } else {
            baseType!!.javaToKotlinType()
        }
    }
}
