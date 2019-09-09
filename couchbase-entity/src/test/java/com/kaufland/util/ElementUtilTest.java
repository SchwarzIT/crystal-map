package com.kaufland.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class ElementUtilTest {

    @Test
    public void testWithOneGeneric() {

        String variable = "ArrayList<String>";

        List<String> results = ElementUtil.INSTANCE.splitGenericIfNeeded(variable);

        Assert.assertEquals(results.size(), 2);
        Assert.assertEquals(results.get(0), "ArrayList");
        Assert.assertEquals(results.get(1), "String");
    }

    @Test
    public void testWithTwoGeneric() {

        String variable = "Map<String, Object>";

        List<String> results = ElementUtil.INSTANCE.splitGenericIfNeeded(variable);

        Assert.assertEquals(results.size(), 3);
        Assert.assertEquals(results.get(0), "Map");
        Assert.assertEquals(results.get(1), "String");
        Assert.assertEquals(results.get(2), "Object");
    }

    @Test
    public void testWithNoGeneric() {

        String variable = "List";

        List<String> results = ElementUtil.INSTANCE.splitGenericIfNeeded(variable);

        Assert.assertEquals(results.size(), 1);
        Assert.assertEquals(results.get(0), "List");
    }

}
