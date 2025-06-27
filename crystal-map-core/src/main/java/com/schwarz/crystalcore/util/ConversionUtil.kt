package com.schwarz.crystalcore.util

import com.squareup.kotlinpoet.TypeName
import java.util.regex.Pattern

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
}
