package org.cyy.fw.android.dborm.sqlite;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.cyy.fw.android.dborm.ORMUtil;
import org.cyy.fw.android.dborm.ORMapInfo;
import org.cyy.fw.android.dborm.ObjectUtil;
import org.cyy.fw.android.dborm.ReflectUtil;
import org.cyy.fw.android.dborm.UniqueIDGenerator;

import android.content.ContentValues;
import android.text.TextUtils;

public class ContentValuesBuilder {
	public static ContentValues generateContentValues(Object object,
			ORMapInfo mapInfo, ValueConvertor typeValueConvertor,
			UniqueIDGenerator idGenerator) {
		return generateContentValues(object, mapInfo, null, typeValueConvertor,
				idGenerator);
	}

	private static ContentValues generateContentValues(Object object,
			ORMapInfo mapInfo, String[] excludeFields,
			ValueConvertor typeValueConvertor, UniqueIDGenerator idGenerator) {
		return generateContentValues(object, mapInfo, excludeFields, null,
				typeValueConvertor, idGenerator);
	}

	public static ContentValues generateContentValues(Object object,
			ORMapInfo mapInfo, String[] excludeFields,
			String[] notIgnoreNullFields, ValueConvertor typeValueConvertor,
			UniqueIDGenerator idGenerator) {
		ContentValues contentValues = new ContentValues();
		List<String> excludeFieldList = new ArrayList<String>();
		if (excludeFields != null) {
			excludeFieldList = Arrays.asList(excludeFields);
		}
		Class<?> clazz = object.getClass();
		Map<String, String> columnMap = mapInfo.getFieldColumnMap();
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			// exclude static fields
			if (Modifier.isStatic(field.getModifiers())) {
				continue;
			}
			// exclude final fields
			if (Modifier.isFinal(field.getModifiers())) {
				continue;
			}
			String fieldName = field.getName();

			if (excludeFieldList.contains(fieldName)) {
				continue;
			}
			String columnName = columnMap.get(fieldName);
			if (TextUtils.isEmpty(columnName)) {
				continue;
			}

			Object fieldValue = ReflectUtil.getValueOfField(object, fieldName);
			// If the object has not a non-null value and the genUIDBySelf is
			// set true, the framework generates the primary key for this object
			if (columnName.equals(mapInfo.getPrimaryKeyField())
					&& fieldValue == null && mapInfo.isGenUIDBySelf()) {
				fieldValue = ORMUtil.createUID(idGenerator);
				// set value
				ReflectUtil.setValueOfField(object, fieldName, fieldValue);
			}
			// convert value between java object and DB table
			Object convertedValue = typeValueConvertor
					.convertJavaValueToDBValue(fieldValue, field.getType());

			String[] notIgnoreNullColumns = ORMUtil.mappingColumnByField(
					notIgnoreNullFields, mapInfo);
			putFieldValueIntoContentValues(contentValues, convertedValue,
					columnName, notIgnoreNullColumns);
		}

		return contentValues;
	}

	private static void putFieldValueIntoContentValues(
			ContentValues contentValues, Object fieldValue, String columnName,
			String[] notIgnoreNullColumns) {
		List<String> notIgnoreNullColumnList = null;
		if (notIgnoreNullColumns != null) {
			notIgnoreNullColumnList = Arrays.asList(notIgnoreNullColumns);
		}
		if (fieldValue == null) {
			if (notIgnoreNullColumnList != null
					&& notIgnoreNullColumnList.contains(columnName)) {
				contentValues.putNull(columnName);
			}
			return;
		}
		if (ObjectUtil.isBoolean(fieldValue)) {
			contentValues.put(columnName, (Boolean) fieldValue);
			return;
		}
		if (ObjectUtil.isByteType(fieldValue)) {
			contentValues.put(columnName, (Byte) fieldValue);
			return;
		}
		if (ObjectUtil.isDouble(fieldValue)) {
			contentValues.put(columnName, (Double) fieldValue);
			return;
		}
		if (ObjectUtil.isFloat(fieldValue)) {
			contentValues.put(columnName, (Float) fieldValue);
			return;
		}
		if (ObjectUtil.isInteger(fieldValue)) {
			contentValues.put(columnName, (Integer) fieldValue);
			return;
		}
		if (ObjectUtil.isLong(fieldValue)) {
			contentValues.put(columnName, (Long) fieldValue);
			return;
		}
		if (ObjectUtil.isShort(fieldValue)) {
			contentValues.put(columnName, (Short) fieldValue);
			return;
		}
		if (fieldValue instanceof String) {
			contentValues.put(columnName, (String) fieldValue);
			return;
		}
		if (fieldValue.getClass() == byte[].class
				|| fieldValue.getClass() == Byte[].class) {
			contentValues.put(columnName, (byte[]) fieldValue);
			return;
		}
	}
}
