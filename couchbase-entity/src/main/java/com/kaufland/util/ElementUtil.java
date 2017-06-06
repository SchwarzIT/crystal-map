package com.kaufland.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ElementUtil {

    public static List<String> splitGenericIfNeeded(String name) {


        List<String> result = new ArrayList<>();
        if (name.contains("<")) {

            result.add(name.substring(0, name.indexOf("<")).trim());

            for (String item : Arrays.asList(name.substring(name.indexOf("<") + 1, name.indexOf(">")).split(","))) {
                result.add(item.trim());
            }
        } else {
            result.add(name);
        }

        return result;

    }

}
