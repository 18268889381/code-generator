package cn.iba8.module.generator.common.properties2json.resolvers;

import cn.iba8.module.generator.common.properties2json.JsonObjectFieldsValidator;
import cn.iba8.module.generator.common.properties2json.PropertyArrayHelper;
import cn.iba8.module.generator.common.properties2json.object.AbstractJsonType;
import cn.iba8.module.generator.common.properties2json.object.ArrayJsonType;
import cn.iba8.module.generator.common.properties2json.object.ObjectJsonType;
import cn.iba8.module.generator.common.properties2json.path.PathMetadata;

import java.util.List;

import static cn.iba8.module.generator.common.properties2json.utils.collection.CollectionUtils.isLastIndex;

public class ArrayJsonTypeResolver extends JsonTypeResolver {

    @Override
    public ObjectJsonType traverse(PathMetadata currentPathMetaData) {
        fetchJsonObjectAndCreateArrayWhenNotExist(currentPathMetaData);
        return currentObjectJsonType;
    }

    private void fetchJsonObjectAndCreateArrayWhenNotExist(PathMetadata currentPathMetaData) {
        if(isArrayExist(currentPathMetaData.getFieldName())) {
            fetchArrayAndAddElement(currentPathMetaData);
        } else {
            createArrayAndAddElement(currentPathMetaData);
        }
    }

    private boolean isArrayExist(String field) {
        return currentObjectJsonType.containsField(field);
    }

    private void fetchArrayAndAddElement(PathMetadata currentPathMetaData) {
        PropertyArrayHelper propertyArrayHelper = currentPathMetaData.getPropertyArrayHelper();
        ArrayJsonType arrayJsonType = getArrayJsonWhenIsValid(currentPathMetaData);
        List<Integer> dimIndexes = propertyArrayHelper.getDimensionalIndexes();
        ArrayJsonType currentArray = arrayJsonType;
        for(int index = 0; index < dimIndexes.size(); index++) {
            if(isLastIndex(dimIndexes, index)) {
                int lastDimIndex = dimIndexes.get(index);
                if(currentArray.existElementByGivenIndex(lastDimIndex)) {
                    fetchJsonObjectWhenIsValid(currentPathMetaData, lastDimIndex, currentArray);
                } else {
                    createJsonObjectAndAddToArray(lastDimIndex, currentArray, currentPathMetaData);
                }
            } else {
                currentArray = ArrayJsonType.createOrGetNextDimensionOfArray(currentArray, dimIndexes, index, currentPathMetaData);
            }
        }
    }

    private void createJsonObjectAndAddToArray(int index, ArrayJsonType arrayJsonType, PathMetadata currentPathMetaData) {
        ObjectJsonType nextObjectJsonType = new ObjectJsonType();
        arrayJsonType.addElement(index, nextObjectJsonType, currentPathMetaData);
        currentObjectJsonType = nextObjectJsonType;
    }

    private void fetchJsonObjectWhenIsValid(PathMetadata currentPathMetaData, int index, ArrayJsonType arrayJsonType) {
        AbstractJsonType element = arrayJsonType.getElement(index);
        JsonObjectFieldsValidator.checkEarlierWasJsonObject(currentPathMetaData.getOriginalPropertyKey(), currentPathMetaData, element);
        currentObjectJsonType = (ObjectJsonType) element;
    }

    private void createArrayAndAddElement(PathMetadata currentPathMetaData) {
        ArrayJsonType arrayJsonTypeObject = new ArrayJsonType();
        ObjectJsonType nextObjectJsonType = new ObjectJsonType();
        arrayJsonTypeObject.addElement(currentPathMetaData.getPropertyArrayHelper(), nextObjectJsonType, currentPathMetaData);
        currentObjectJsonType.addField(currentPathMetaData.getFieldName(), arrayJsonTypeObject, currentPathMetaData);
        currentObjectJsonType = nextObjectJsonType;
    }
}
