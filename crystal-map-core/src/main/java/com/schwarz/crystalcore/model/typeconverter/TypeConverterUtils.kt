package com.schwarz.crystalcore.model.typeconverter

import com.schwarz.crystalapi.ITypeConverter
import kotlinx.metadata.KmClassifier
import kotlinx.metadata.KmType
import kotlinx.metadata.jvm.KotlinClassMetadata

fun Metadata.getTypeConverterInterface(): KmType? {
    val kmClass = this.toKmClass()
    return kmClass.supertypes.find {
        val classifier = it.classifier
        classifier is KmClassifier.Class && typeConverterKmClass.name == classifier.name
    }
}

private val typeConverterKmClass = ITypeConverter::class.java.getAnnotation(Metadata::class.java).toKmClass()

private fun Metadata.toKmClass() =
    (KotlinClassMetadata.readStrict(this) as KotlinClassMetadata.Class).kmClass
