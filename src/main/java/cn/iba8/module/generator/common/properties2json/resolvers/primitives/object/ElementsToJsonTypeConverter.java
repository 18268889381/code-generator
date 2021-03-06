package cn.iba8.module.generator.common.properties2json.resolvers.primitives.object;

import cn.iba8.module.generator.common.properties2json.object.AbstractJsonType;
import cn.iba8.module.generator.common.properties2json.object.ArrayJsonType;
import cn.iba8.module.generator.common.properties2json.resolvers.PrimitiveJsonTypesResolver;
import cn.iba8.module.generator.common.properties2json.object.AbstractJsonType;
import cn.iba8.module.generator.common.properties2json.object.ArrayJsonType;
import cn.iba8.module.generator.common.properties2json.resolvers.PrimitiveJsonTypesResolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;

public class ElementsToJsonTypeConverter extends AbstractObjectToJsonTypeConverter<Collection<?>> {

    @Override
    public Optional<AbstractJsonType> convertToJsonTypeOrEmpty(PrimitiveJsonTypesResolver primitiveJsonTypesResolver, Collection<?> elements, String propertyKey) {
        return Optional.of(new ArrayJsonType(primitiveJsonTypesResolver, elements, null, propertyKey));
    }

    @Override
    public Optional<AbstractJsonType> returnOptionalJsonType(PrimitiveJsonTypesResolver primitiveJsonTypesResolver, Object propertyValue, String propertyKey) {
        if(hasElements(propertyValue.getClass())) {
            Collection<?> collection;
            if (propertyValue.getClass().isArray()) {
                Object[] rawArray = (Object[]) propertyValue;
                collection  = new ArrayList<>(Arrays.asList(rawArray));
            } else {
                collection = (Collection<?>) propertyValue;
            }
            return convertToJsonTypeOrEmpty(primitiveJsonTypesResolver, collection, propertyKey);
        }
        return convertToJsonTypeOrEmpty(primitiveJsonTypesResolver, singletonList(propertyValue), propertyKey);
    }

    public boolean hasElements(Class<?> classToTest) {
        return canResolveClass.isAssignableFrom(classToTest) || classToTest.isArray();
    }

    @Override
    public Class<?> resolveTypeOfResolver() {
        return Collection.class;
    }

    @Override
    public List<Class<?>> getClassesWhichCanResolve() {
        return Arrays.asList(Collection.class, Object[].class);
    }
}
