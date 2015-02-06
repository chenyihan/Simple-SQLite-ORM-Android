package org.cyy.fw.android.dborm.sqlite;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import org.cyy.fw.android.dborm.ORMapInfo;
import org.cyy.fw.android.dborm.ORMapper;
import org.cyy.fw.android.dborm.POJOClassDefineException;
import org.cyy.fw.android.dborm.ReflectUtil;
import org.cyy.fw.android.dborm.UniqueIDGenerator;
import org.cyy.fw.android.dborm.ORMapInfo.AssociateInfo;

import android.content.ContentValues;

/**
 * 
 * 
 * @author cyy
 * @version [V1.0, 2012-11-20]
 */
class InsertItem {

	ContentValues contentValues;

	String tableName;

	private static void fillInsertItem(Object object,
			List<InsertItem> insertItemList, List<Class<?>> includeClassList,
			Map<Class<?>, String> tableMap, ORMapper orMapper,
			ValueConvertor valueConvertor, UniqueIDGenerator idGenerator)
			throws NoSuchFieldException, IllegalAccessException,
			InvocationTargetException {
		if (object == null) {
			return;
		}
		InsertItem insertInfo = new InsertItem();
		Class<?> clazz = object.getClass();
		String tableName = tableMap == null ? null : tableMap.get(clazz);
		ORMapInfo mapInfo = orMapper.generateColumnMap(clazz, tableName);
		tableName = mapInfo.getTableName();
		insertInfo.tableName = tableName;
		ContentValues contentValues = ContentValuesBuilder
				.generateContentValues(object, mapInfo, valueConvertor,
						idGenerator);
		insertInfo.contentValues = contentValues;
		insertItemList.add(insertInfo);
		Map<Class<?>, AssociateInfo> assoInfoMap = mapInfo.getAssociateInfo();
		if (assoInfoMap == null || assoInfoMap.isEmpty()) {
			return;
		}
		Set<Entry<Class<?>, AssociateInfo>> entrySet = assoInfoMap.entrySet();
		for (Entry<Class<?>, AssociateInfo> entry : entrySet) {
			Class<?> target = entry.getKey();
			if (includeClassList != null && !includeClassList.contains(target)) {
				continue;
			}
			AssociateInfo assoInfo = entry.getValue();
			String fieldName = assoInfo.getField();
			Field field = clazz.getDeclaredField(fieldName);
			Class<?> fieldType = field.getType();
			Object fieldValue = ReflectUtil.getValueOfField(object, fieldName);
			Object sourceValue = ReflectUtil.getValueOfField(object,
					assoInfo.getSourceField());
			// Support List only so far
			if (fieldType.isAssignableFrom(List.class)) {
				List<?> valueList = (List<?>) fieldValue;
				if (valueList == null || valueList.isEmpty()) {
					continue;
				}
				for (int i = 0; i < valueList.size(); i++) {
					// set value
					ReflectUtil.setValueOfField(valueList.get(i),
							assoInfo.getTargetField(), sourceValue);
					fillInsertItem(valueList.get(i), insertItemList,
							includeClassList, tableMap, orMapper,
							valueConvertor, idGenerator);
				}
			} else if (fieldType.isAssignableFrom(Vector.class)) {
				Vector<?> valueList = (Vector<?>) fieldValue;
				if (valueList == null || valueList.isEmpty()) {
					continue;
				}
				for (int i = 0; i < valueList.size(); i++) {
					// set value
					ReflectUtil.setValueOfField(valueList.get(i),
							assoInfo.getTargetField(), sourceValue);
					fillInsertItem(valueList.get(i), insertItemList,
							includeClassList, tableMap, orMapper,
							valueConvertor, idGenerator);
				}
			}
			// complex object
			else if (!fieldType.isAssignableFrom(Collection.class)
					&& !fieldType.isArray()
					&& !fieldType.isAssignableFrom(Map.class)) {
				if (fieldValue == null) {
					continue;
				}
				// set value
				ReflectUtil.setValueOfField(fieldValue,
						assoInfo.getTargetField(), sourceValue);
				fillInsertItem(fieldValue, insertItemList, includeClassList,
						tableMap, orMapper, valueConvertor, idGenerator);
			}
		}

	}

	static List<InsertItem> fillInsertItems(Object[] objects,
			Class<?>[] includeClasses, Map<Class<?>, String> tableMap,
			ORMapper orMapper, ValueConvertor valueConvertor,
			UniqueIDGenerator idGenerator) {

		final List<InsertItem> insertItemQueue = new ArrayList<InsertItem>();
		if (objects == null || objects.length == 0) {
			return insertItemQueue;
		}
		List<Class<?>> includeClassList = null;
		if (includeClasses != null) {
			includeClassList = Arrays.asList(includeClasses);
		}
		try {
			for (int i = 0; i < objects.length; i++) {
				fillInsertItem(objects[i], insertItemQueue, includeClassList,
						tableMap, orMapper, valueConvertor, idGenerator);
			}
		} catch (Exception e) {
			throw new POJOClassDefineException("Class is defined error:"
					+ e.getMessage(), e);
		}
		return insertItemQueue;
	}
}