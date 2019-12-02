package cn.iba8.module.generator.common.properties2json;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import static cn.iba8.module.generator.common.properties2json.Constants.ARRAY_END_SIGN;
import static cn.iba8.module.generator.common.properties2json.Constants.ARRAY_START_SIGN;
import static cn.iba8.module.generator.common.properties2json.Constants.EMPTY_STRING;
import static cn.iba8.module.generator.common.properties2json.Constants.SIMPLE_ARRAY_DELIMITER;
import static cn.iba8.module.generator.common.properties2json.path.PathMetadata.INDEXES_PATTERN;

@Getter
public class PropertyArrayHelper {

    private List<Integer> dimensionalIndexes;
    private String arrayFieldName;

    public PropertyArrayHelper(String field) {
        arrayFieldName = getNameFromArray(field);
        dimensionalIndexes = getIndexesFromArrayField(field);
    }

    public static String getNameFromArray(String fieldName) {
        return fieldName.replaceFirst(INDEXES_PATTERN + "$", EMPTY_STRING);
    }

    public static List<Integer> getIndexesFromArrayField(String fieldName) {
        String indexesAsText = fieldName.replace(getNameFromArray(fieldName), EMPTY_STRING);
        String[] indexesAsTextArray = indexesAsText
                .replace(ARRAY_START_SIGN, EMPTY_STRING)
                .replace(ARRAY_END_SIGN, SIMPLE_ARRAY_DELIMITER)
                .replaceAll("\\s", EMPTY_STRING)
                .split(SIMPLE_ARRAY_DELIMITER);
        List<Integer> indexes = new ArrayList<>();
        for (String indexAsText : indexesAsTextArray) {
            indexes.add(Integer.valueOf(indexAsText));
        }
        return indexes;
    }
}
