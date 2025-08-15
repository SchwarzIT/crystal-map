package com.schwarz.crystalksp.model.source

import com.google.devtools.ksp.symbol.KSAnnotation
import com.schwarz.crystalapi.ClassNameDefinition
import com.schwarz.crystalapi.ITypeConverter
import com.schwarz.crystalcore.model.source.ISourceMetaData
import com.squareup.kotlinpoet.ClassName
import kotlin.metadata.KmClassifier
import kotlin.metadata.KmType
import kotlin.metadata.KmTypeProjection
import kotlin.metadata.isNullable
import kotlin.metadata.jvm.KotlinClassMetadata

class SourceMetaData(metadata: KSAnnotation) : ISourceMetaData {

}