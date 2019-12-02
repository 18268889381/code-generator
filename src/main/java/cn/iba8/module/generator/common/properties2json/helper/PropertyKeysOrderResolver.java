package cn.iba8.module.generator.common.properties2json.helper;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;

public class PropertyKeysOrderResolver {
    public List<String> getKeysInExpectedOrder(Map<String, ?> properties) {
        return Lists.newArrayList(properties.keySet());
    }
}
