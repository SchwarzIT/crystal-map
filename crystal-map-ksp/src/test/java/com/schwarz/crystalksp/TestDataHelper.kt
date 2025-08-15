package com.schwarz.crystalksp

import com.tschuchort.compiletesting.SourceFile
import java.io.File

object TestDataHelper {

    fun clazzAsJavaFileObjects(clazz: String): SourceFile {
        val className = "$clazz.kt"
        val file = javaClass.classLoader.getResource(className).file
        return SourceFile.kotlin(className, File(file).readText())
    }
}