package com.schwarz.testdata

import com.tschuchort.compiletesting.SourceFile

object TestDataHelper {
    private const val PACKAGE_DECLARE = "package com.kaufland.testModels\n"

    fun clazzAsJavaFileObjects(clazz: String): SourceFile {
        val className = "$clazz.kt"
        val content =
            String(
                this::class.java.classLoader
                    .getResourceAsStream(className)
                    ?.readAllBytes()!!,
            )
        return SourceFile.kotlin(className, "$PACKAGE_DECLARE$content")
    }
}
