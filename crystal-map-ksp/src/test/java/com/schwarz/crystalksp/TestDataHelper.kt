package com.schwarz.crystalksp

import com.tschuchort.compiletesting.SourceFile

object TestDataHelper {
    fun clazzAsJavaFileObjects(clazz: String): SourceFile {
        val className = "$clazz.kt"
        val content = String(javaClass.classLoader.getResourceAsStream(className).readAllBytes())
        return SourceFile.kotlin(className, content)
    }
}
