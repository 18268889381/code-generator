package cn.iba8.module.generator.common.properties2json.resolvers;

import cn.iba8.module.generator.common.properties2json.JsonObjectFieldsValidator;
import cn.iba8.module.generator.common.properties2json.object.*;
import cn.iba8.module.generator.common.properties2json.path.PathMetadata;
import cn.iba8.module.generator.common.properties2json.resolvers.hierarchy.JsonTypeResolversHierarchyResolver;
import cn.iba8.module.generator.common.properties2json.resolvers.primitives.object.NullToJsonTypeConverter;
import cn.iba8.module.generator.common.properties2json.resolvers.primitives.object.ObjectToJsonTypeConverter;
import cn.iba8.module.generator.common.properties2json.resolvers.primitives.string.TextToConcreteObjectResolver;
import cn.iba8.module.generator.common.properties2json.util.exception.CannotOverrideFieldException;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Optional;

public class PrimitiveJsonTypesResolver extends JsonTypeResolver {

    private final List<TextToConcreteObjectResolver> toObjectsResolvers;
    private final JsonTypeResolversHierarchyResolver resolversHierarchyResolver;
    private final Boolean skipNulls;
    private final NullToJsonTypeConverter nullToJsonTypeConverter;

    public PrimitiveJsonTypesResolver(List<TextToConcreteObjectResolver> toObjectsResolvers,
                                      List<ObjectToJsonTypeConverter> toJsonResolvers,
                                      Boolean skipNulls,
                                      NullToJsonTypeConverter nullToJsonTypeConverter) {
        this.toObjectsResolvers = ImmutableList.copyOf(toObjectsResolvers);
        this.resolversHierarchyResolver = new JsonTypeResolversHierarchyResolver(toJsonResolvers);
        this.skipNulls = skipNulls;
        this.nullToJsonTypeConverter = nullToJsonTypeConverter;
    }

    @Override
    public ObjectJsonType traverse(PathMetadata currentPathMetaData) {
        addPrimitiveFieldWhenIsValid(currentPathMetaData);
        return null;
    }

    private void addPrimitiveFieldWhenIsValid(PathMetadata currentPathMetaData) {
        JsonObjectFieldsValidator.checkThatFieldCanBeSet(currentObjectJsonType, currentPathMetaData, propertyKey);
        addPrimitiveFieldToCurrentJsonObject(currentPathMetaData);
    }

    private void addPrimitiveFieldToCurrentJsonObject(PathMetadata currentPathMetaData) {
        String field = currentPathMetaData.getFieldName();
        if(currentPathMetaData.isArrayField()) {
            addFieldToArray(currentPathMetaData);
        } else {
            if(currentObjectJsonType.containsField(field) && JsonObjectFieldsValidator.isArrayJson(currentObjectJsonType.getField(field))) {
                AbstractJsonType abstractJsonType = currentPathMetaData.getJsonValue();
                ArrayJsonType currentArrayInObject = currentObjectJsonType.getJsonArray(field);
                if(JsonObjectFieldsValidator.isArrayJson(abstractJsonType)) {
                    ArrayJsonType newArray = (ArrayJsonType) abstractJsonType;
                    List<AbstractJsonType> abstractJsonTypes = newArray.convertToListWithoutRealNull();
                    for(int i = 0; i < abstractJsonTypes.size(); i++) {
                        currentArrayInObject.addElement(i, abstractJsonTypes.get(i), currentPathMetaData);
                    }
                } else {
                    throw new CannotOverrideFieldException(currentPathMetaData.getCurrentFullPath(), currentArrayInObject, propertyKey);
                }
            } else {
                currentObjectJsonType.addField(field, currentPathMetaData.getJsonValue(), currentPathMetaData);
            }
        }
    }

    public Object getResolvedObject(String propertyValue, String propertyKey) {
        Optional<?> objectOptional = Optional.empty();
        for(TextToConcreteObjectResolver primitiveResolver : toObjectsResolvers) {
            if(!objectOptional.isPresent()) {
                objectOptional = primitiveResolver.returnConvertedValueForClearedText(this, propertyValue, propertyKey);
            }
        }
        return objectOptional.orElse(null);
    }

    public AbstractJsonType resolvePrimitiveTypeAndReturn(Object propertyValue, String propertyKey) {
        AbstractJsonType result;
        if(propertyValue == null) {
            result = nullToJsonTypeConverter.convertToJsonTypeOrEmpty(this, JsonNullReferenceType.NULL_OBJECT, propertyKey).get();
        } else {
            result = resolversHierarchyResolver.returnConcreteJsonTypeObject(this, propertyValue, propertyKey);
        }

        if(skipNulls && result instanceof JsonNullReferenceType) {
            result = SkipJsonField.SKIP_JSON_FIELD;
        }

        return result;
    }

    protected void addFieldToArray(PathMetadata currentPathMetaData) {
        if(arrayWithGivenFieldNameExist(currentPathMetaData.getFieldName())) {
            fetchArrayAndAddElement(currentPathMetaData);
        } else {
            createArrayAndAddElement(currentPathMetaData);
        }
    }

    private boolean arrayWithGivenFieldNameExist(String field) {
        return currentObjectJsonType.containsField(field);
    }

    private void createArrayAndAddElement(PathMetadata currentPathMetaData) {
        ArrayJsonType arrayJsonTypeObject = new ArrayJsonType();
        addElementToArray(currentPathMetaData, arrayJsonTypeObject);
        currentObjectJsonType.addField(currentPathMetaData.getFieldName(), arrayJsonTypeObject, currentPathMetaData);
    }

    private void fetchArrayAndAddElement(PathMetadata currentPathMetaData) {
        ArrayJsonType arrayJsonType = getArrayJsonWhenIsValid(currentPathMetaData);
        addElementToArray(currentPathMetaData, arrayJsonType);
    }

    private void addElementToArray(PathMetadata currentPathMetaData, ArrayJsonType arrayJsonTypeObject) {
        arrayJsonTypeObject.addElement(currentPathMetaData.getPropertyArrayHelper(), currentPathMetaData.getJsonValue(), currentPathMetaData);
    }

}
