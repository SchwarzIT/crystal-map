package com.kaufland.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sbra0902 on 22.06.17.
 */

public final class ConversionUtil {

    public static String convertCamelToUnderscore(String words){
        Matcher m = Pattern.compile("(?<=[a-z])[A-Z]").matcher(words);

        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, "_"+m.group().toLowerCase());
        }
        m.appendTail(sb);

        return sb.toString();
    }
}
