package com.github.pagehelper.test.basic.config;

import com.github.pagehelper.config.PageHelperConfig;
import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

public class ConfigTest {

    @Test
    public void configTest() {
        // System.setProperty("dialectAlias", "hsqldb=com.github.pagehelper.dialect.helper.HsqldbDialect");
        // System.setProperty("helperDialect", "hsqldb");
        Properties properties = new Properties();
        PageHelperConfig.fillPropertiesByConfig(properties);
        Assert.assertEquals(properties.getProperty("offsetAsPageNum"), "false");
        Assert.assertEquals(properties.getProperty("rowBoundsWithCount"), "false");
        Assert.assertEquals(properties.getProperty("pageSizeZero"), "false");
        Assert.assertEquals(properties.getProperty("reasonable"), "true");
        Assert.assertEquals(properties.getProperty("supportMethodsArguments"), "false");
        Assert.assertEquals(properties.getProperty("autoRuntimeDialect"), "true");
        // Assert.assertEquals(properties.getProperty("helperDialect"), "hsqldb");
        // Assert.assertEquals(properties.getProperty("dialectAlias"), "hsqldb=com.github.pagehelper.dialect.helper.HsqldbDialect");
    }
}
