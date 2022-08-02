package com.github.pagehelper.config;

import com.github.pagehelper.util.StringUtil;
import io.mybatis.config.ConfigHelper;
import io.mybatis.config.defaults.UserConfig;

import java.util.Properties;

/**
 * 配置类
 *
 * @author majiang
 * @version 6.0.0
 */
public class PageHelperConfig extends UserConfig {

    @Override
    protected String getConfigName() {
        return "pagehelper";
    }

    @Override
    protected String getConfigKey() {
        return "pagehelper.properties";
    }

    public static void fillPropertiesByConfig(Properties properties) {
        for (PageHelperConfigEnum configEnum : PageHelperConfigEnum.values()) {
            String configKey = configEnum.name();
            String configValue = ConfigHelper.getStr(configKey);

            // properties当中配置优先级更高，如果没有通过starter和mybatis的方式进行配置。再从ConfigHelper当中获取配置
            if (StringUtil.isEmpty(properties.getProperty(configKey))
                    && StringUtil.isNotEmpty(configValue)) {
                properties.setProperty(configKey, configValue);
            }
        }
    }
}
