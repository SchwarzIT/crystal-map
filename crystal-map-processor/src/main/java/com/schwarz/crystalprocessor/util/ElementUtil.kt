package com.schwarz.crystalprocessor.util

import java.util.ArrayList
import java.util.Arrays

object ElementUtil {
    fun splitGenericIfNeeded(name: String): List<String> {
        val result = ArrayList<String>()
        if (name.contains("<")) {
            result.add(name.substring(0, name.indexOf("<")).trim { it <= ' ' })

            for (item in Arrays.asList(
                *name
                    .substring(
                        name.indexOf("<") + 1,
                        name.indexOf(">"),
                    ).split(",".toRegex())
                    .dropLastWhile {
                        it.isEmpty()
                    }.toTypedArray(),
            )) {
                result.add(item.trim { it <= ' ' })
            }
        } else {
            result.add(name)
        }

        return result
    }
}
