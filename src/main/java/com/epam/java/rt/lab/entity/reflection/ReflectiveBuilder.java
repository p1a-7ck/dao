package com.epam.java.rt.lab.entity.reflection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * com.epam.java.rt.lab.dao
 */
public class ReflectiveBuilder {
    private final Map<String, List<?>> fieldValueMap;

    public ReflectiveBuilder() {
        this.fieldValueMap = new HashMap<>();
    }

    public void clearFieldValueMap() {
        this.fieldValueMap.clear();
    }

    public <T> void setFieldValue(String fieldName, T fieldValue) {
        List<T> valueList = new ArrayList<>();
        valueList.add(fieldValue);
        this.fieldValueMap.put(fieldName, valueList);
    }

    public <T> void setFieldValueList(String fieldName, List<T> fieldValueList) {
        this.fieldValueMap.put(fieldName, fieldValueList);
    }

    public <T> void addFieldValue(String fieldName, T fieldValue) {
        List<T> valueList = (List<T>) this.fieldValueMap.get(fieldName);
        if (valueList == null) {
            valueList = new ArrayList<>();
            valueList.add(fieldValue);
            setFieldValueList(fieldName, valueList);
        } else {
            valueList.add(fieldValue);
        }
    }

    public <T> T getFieldValue(String fieldName) {
        List<T> valueList = (List<T>) this.fieldValueMap.get(fieldName);
        if (valueList == null && !this.fieldValueMap.containsKey(fieldName))
            throw new IllegalArgumentException("Field '" + fieldName + "' not found");
        return valueList.get(0);
    }

    public <T> List<T> getFieldValueList(String fieldName) {
        List<T> valueList = (List<T>) this.fieldValueMap.get(fieldName);
        if (valueList == null && !this.fieldValueMap.containsKey(fieldName))
            throw new IllegalArgumentException("Field '" + fieldName + "' not found");
        return valueList;
    }
}