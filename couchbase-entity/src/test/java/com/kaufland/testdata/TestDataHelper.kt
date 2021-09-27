package com.kaufland.testdata

import com.tschuchort.compiletesting.SourceFile
import java.io.File

object TestDataHelper {

    private const val PACKAGE_DECLARE = "package com.kaufland.testModels\n"

    fun clazzAsJavaFileObjects(clazz: String): SourceFile {
        val className = "$clazz.kt"
        val file = javaClass.classLoader.getResource(className).file
        return SourceFile.kotlin(className, "$PACKAGE_DECLARE${File(file).readText()}")
    }
}
