package com.schwarz.crystalprocessor.util

import com.schwarz.crystalprocessor.javaToKotlinType
import com.squareup.kotlinpoet.asTypeName
import java.util.regex.Pattern
import javax.lang.model.type.TypeMirror

/**
 * Created by sbra0902 on 22.06.17.
 */

object ConversionUtil {

    fun convertCamelToUnderscore(words: String): String {
        val m = Pattern.compile("(?<=[a-z])[A-Z]").matcher(words)

        val sb = StringBuffer()
        while (m.find()) {
            m.appendReplacement(sb, "_" + m.group().lowercase())
        }
        m.appendTail(sb)

        return sb.toString()
    }

    fun convertStringToDesiredFormat(clazz: TypeMirror, value: String): String {
        return if (clazz.asTypeName().javaToKotlinType() == TypeUtil.string()) {
            "\"" + value + "\""
        } else {
            value
        }
    }
}
