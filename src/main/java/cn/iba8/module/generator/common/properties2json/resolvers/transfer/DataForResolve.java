package cn.iba8.module.generator.common.properties2json.resolvers.transfer;

import cn.iba8.module.generator.common.properties2json.object.ObjectJsonType;
import cn.iba8.module.generator.common.properties2json.path.PathMetadata;
import cn.iba8.module.generator.common.properties2json.object.ObjectJsonType;
import cn.iba8.module.generator.common.properties2json.path.PathMetadata;

import java.util.Map;

public class DataForResolve {

    private final Map<String, Object> properties;
    private final String propertyKey;
    private final ObjectJsonType currentObjectJsonType;
    private final PathMetadata currentPathMetaData;

    public DataForResolve(Map<String, Object> properties, String propertyKey, ObjectJsonType currentObjectJsonType, PathMetadata currentPathMetaData) {
        this.properties = properties;
        this.propertyKey = propertyKey;
        this.currentObjectJsonType = currentObjectJsonType;
        this.currentPathMetaData = currentPathMetaData;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public String getPropertiesKey() {
        return propertyKey;
    }

    public ObjectJsonType getCurrentObjectJsonType() {
        return currentObjectJsonType;
    }

    public PathMetadata getCurrentPathMetaData() {
        return currentPathMetaData;
    }
}
