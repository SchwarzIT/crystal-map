package com.kaufland.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by sbra0902 on 22.06.17.
 */

public class ConversionUtilTest {


    @Test
    public void testCamel(){
        Assert.assertEquals("Ta_aa_ba", ConversionUtil.INSTANCE.convertCamelToUnderscore("TaAaBa"));
    }

    @Test
    public void testMultibleWordsCamel(){
        Assert.assertEquals("Ta_aa_ba Foo_bar", ConversionUtil.INSTANCE.convertCamelToUnderscore("TaAaBa FooBar"));
    }

}
