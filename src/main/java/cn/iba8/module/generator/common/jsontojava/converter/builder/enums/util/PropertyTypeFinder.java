package cn.iba8.module.generator.common.jsontojava.converter.builder.enums.util;

import cn.iba8.module.generator.common.jsontojava.converter.builder.enums.ComplexPropertyType;
import cn.iba8.module.generator.common.jsontojava.converter.builder.enums.PropertyType;
import cn.iba8.module.generator.common.jsontojava.converter.builder.enums.SinglePropertyType;
import cn.iba8.module.generator.common.jsontojava.validator.JsonTypeChecker;

public final class PropertyTypeFinder {

    private PropertyTypeFinder() {}

    public static PropertyType getPropertyType(Object object, JsonTypeChecker jsonTypeChecker) {
        PropertyType propertyType = SinglePropertyType.NEW;

        if(!jsonTypeChecker.isArray(object.toString())) {
            if(object instanceof String) {
                propertyType = SinglePropertyType.STRING;
            }else if(object instanceof Integer) {
                propertyType = SinglePropertyType.INTEGER;
            }else if(object instanceof Double) {
                propertyType = SinglePropertyType.DOUBLE;
            } else if (object instanceof Boolean){
                propertyType = SinglePropertyType.BOOLEAN;
            }
        } else {
            propertyType = ComplexPropertyType.LIST;
        }

        return propertyType;
    }
}
