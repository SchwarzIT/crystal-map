package com.schwarz.crystalprocessor.model.typeconverter

import com.schwarz.crystalapi.ITypeConverter
import kotlinx.metadata.KmClassifier
import kotlinx.metadata.KmType
import kotlinx.metadata.jvm.KotlinClassMetadata
import javax.lang.model.element.Element

fun Element.getTypeConverterInterface(): KmType? {
    val kmClass = getAnnotation(Metadata::class.java).toKmClass()
    return kmClass.supertypes.find {
        val classifier = it.classifier
        classifier is KmClassifier.Class && typeConverterKmClass.name == classifier.name
    }
}

private val typeConverterKmClass = ITypeConverter::class.java.getAnnotation(Metadata::class.java).toKmClass()

private fun Metadata.toKmClass() =
    (KotlinClassMetadata.read(this) as KotlinClassMetadata.Class).kmClass
