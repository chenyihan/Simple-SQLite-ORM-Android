package org.cyy.fw.android.dborm.sqlite;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.cyy.fw.android.dborm.ORMUtil;
import org.cyy.fw.android.dborm.ORMapInfo;
import org.cyy.fw.android.dborm.ORMapInfo.AssociateInfo;
import org.cyy.fw.android.dborm.ORMapper;
import org.cyy.fw.android.dborm.POJOClassDefineException;
import org.cyy.fw.android.dborm.ReflectUtil;
import org.cyy.fw.android.dborm.sqlite.SQLBuilder.ColumnIndex;
import org.cyy.fw.android.dborm.sqlite.SQLBuilder.QueryColumns;

import android.database.Cursor;
import android.util.Log;

/**
 * 
 * Result set Processor, convert the result set to POJOs<BR>
 * 
 * @author cyy
 * @version [V1.0, 2012-11-19]
 */
class ResultsetProcessor {

	private static class RelationInfo {
		String relationPath;

		List<Object> relationValues = new ArrayList<Object>();
	}

	private static final String TAG = "ResultsetProcessor";

	private Cursor cursor;

	private ValueConvertor valueConvertor;

	ResultsetProcessor(Cursor cursor) {
		this(cursor, null);
	}

	ResultsetProcessor(Cursor cursor, ValueConvertor valueConvertor) {
		this.cursor = cursor;
		this.valueConvertor = valueConvertor;
	}

	private ValueConvertor getValueConvertor() {
		if (valueConvertor == null) {
			valueConvertor = new DefaultValueConvertor();
		}
		return valueConvertor;
	}

	@SuppressWarnings("unchecked")
	<T> T[] processCursor(ORMapper orMapper, Class<T> clazz,
			QueryColumns queryColumns, Map<Class<?>, String> tableMap) {
		T[] results = null;
		if (cursor == null) {
			return (T[]) Array.newInstance(clazz, 0);
		}
		List<List<Object>> orgiList = new ArrayList<List<Object>>();
		ORMapInfo mapInfo = ORMUtil.getOrMapInfo(orMapper, clazz, tableMap);
		try {
			Log.d(TAG, "processCursorCascade 0");
			queryColumns.generateColumnIndexMaper();
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				do {
					List<Object> r = processCursorRow(orMapper, mapInfo,
							queryColumns, tableMap);
					orgiList.add(r);
				} while (cursor.moveToNext());
			}
			Log.d(TAG, "processCursorCascade 1");

			List<T> resultList = (List<T>) combineResult(clazz, orMapper,
					orgiList, queryColumns, tableMap);
			Log.d(TAG, "processCursorCascade 2");
			results = resultList.toArray((T[]) Array.newInstance(clazz,
					resultList.size()));
			Log.d(TAG, "processCursorCascade 3");
		} catch (IllegalAccessException e) {
			throw new POJOClassDefineException("Class is defined error:"
					+ e.getMessage(), e);
		} catch (InstantiationException e) {
			throw new POJOClassDefineException("Class is defined error:"
					+ e.getMessage(), e);
		} catch (NoSuchFieldException e) {
			throw new POJOClassDefineException("Class is defined error:"
					+ e.getMessage(), e);
		}
		return results;
	}

	@SuppressWarnings("unchecked")
	private <T> List<T> combineResult(Class<T> clazz, ORMapper orMapper,
			List<List<Object>> results, QueryColumns queryColumns,
			Map<Class<?>, String> tableMap) throws NoSuchFieldException,
			IllegalAccessException {
		List<T> newResults = new ArrayList<T>();
		Map<Object, Object> resultCache = new HashMap<Object, Object>();
		try {
			ORMapInfo mapInfo = ORMUtil.getOrMapInfo(orMapper, clazz, tableMap);
			for (List<Object> list : results) {
				if (list.size() < 1) {
					continue;
				}
				// the first object is the root object
				T rootObj = (T) list.get(0);
				if (rootObj == null) {
					continue;
				}
				Object pkValue = ORMUtil.getPkValue(rootObj, mapInfo);

				Object result = matchValueInMap(resultCache, pkValue);
				if (result == null) {
					newResults.add(rootObj);
					resultCache.put(pkValue, rootObj);
					result = rootObj;
				}
				RelationInfo relationInfo = new RelationInfo();
				relationInfo.relationValues.add(pkValue);
				for (int i = 1; i < list.size(); i++) {

					Object childObj = list.get(i);
					if (childObj == null) {
						continue;
					}
					int parentIndex = findParentIndex(queryColumns,
							childObj.getClass(), i);

					relationInfo.relationPath = null;
					fillParentRelationInfo(queryColumns, list, parentIndex,
							orMapper, relationInfo, tableMap);
					Object parentObj = matchRelatedValueInList(newResults,
							relationInfo, orMapper, tableMap, resultCache);
					combinResult(parentObj, childObj, orMapper, tableMap);
				}
			}
		} catch (InvocationTargetException e) {
			throw new POJOClassDefineException("Class is defined error:"
					+ e.getMessage(), e);
		}
		return newResults;
	}

	@SuppressWarnings("unchecked")
	private void combinResult(Object parentObj, Object childObj,
			ORMapper orMapper, Map<Class<?>, String> tableMap)
			throws NoSuchFieldException, IllegalAccessException,
			InvocationTargetException {
		if (parentObj == null) {
			return;
		}
		Class<?> parentClass = parentObj.getClass();
		Class<?> childClass = childObj.getClass();
		ORMapInfo mapInfo = ORMUtil.getOrMapInfo(orMapper, parentClass,
				tableMap);
		AssociateInfo assoInfo = mapInfo.getAssociateInfo().get(childClass);
		if (assoInfo == null) {
			return;
		}

		String fieldName = assoInfo.getField();
		Field field = mapInfo.getClazz().getDeclaredField(fieldName);
		Class<?> fieldType = field.getType();
		// Only support List so far
		if (fieldType.isAssignableFrom(List.class)) {
			List<Object> fieldValue = (List<Object>) ReflectUtil
					.getValueOfField(parentObj, fieldName);
			if (fieldValue == null) {
				fieldValue = new ArrayList<Object>();
				ReflectUtil.setValueOfField(parentObj, fieldName, fieldValue);
			}
			ORMapInfo childMapInfo = ORMUtil.getOrMapInfo(orMapper, childClass,
					tableMap);
			String childPkField = childMapInfo.getPrimaryKeyField();
			Object childPk = ORMUtil.getPkValue(childObj, childMapInfo);

			if (matchValueInList(fieldValue, childPkField, childPk) == null) {
				fieldValue.add(childObj);
			}
		} else if (fieldType.isAssignableFrom(Vector.class)) {
			Vector<Object> fieldValue = (Vector<Object>) ReflectUtil
					.getValueOfField(parentObj, fieldName);
			if (fieldValue == null) {
				fieldValue = new Vector<Object>();

				ReflectUtil.setValueOfField(parentObj, fieldName, fieldValue);
			}

			ORMapInfo childMapInfo = ORMUtil.getOrMapInfo(orMapper, childClass,
					tableMap);
			String childPkField = childMapInfo.getPrimaryKeyField();
			Object childPk = ORMUtil.getPkValue(childObj, childMapInfo);

			if (matchValueInList(fieldValue, childPkField, childPk) == null) {
				fieldValue.add(childObj);
			}
		}
		// Non-Collection Complex object
		else if (!fieldType.isAssignableFrom(Collection.class)
				&& !fieldType.isArray()
				&& !fieldType.isAssignableFrom(Map.class)) {
			Object fieldValue = ReflectUtil.getValueOfField(parentObj,
					fieldName);
			// if the value have already been set, the value cann't be set again
			if (fieldValue == null) {
				ReflectUtil.setValueOfField(parentObj, fieldName, childObj);
			}
		}

	}

	private void fillParentRelationInfo(QueryColumns queryColumns, List<?> list,
			int childIndex, ORMapper orMapper, RelationInfo relationInfo,
			Map<Class<?>, String> tableMap) {
		Class<?> childClass = list.get(childIndex).getClass();
		int parentIndex = findParentIndex(queryColumns, childClass, childIndex);
		StringBuffer sb = new StringBuffer();
		if (parentIndex == -1) {
			return;
		}
		Object parent = list.get(parentIndex);
		if (parent == null) {
			return;
		}
		Class<?> parentClass = parent.getClass();
		ORMapInfo parentMapInfo = ORMUtil.getOrMapInfo(orMapper, parentClass,
				tableMap);
		Map<Class<?>, AssociateInfo> assoMap = parentMapInfo.getAssociateInfo();
		AssociateInfo assoInfo = assoMap.get(childClass);
		if (assoInfo == null) {
			return;
		}
		String fieldName = assoInfo.getField();
		Object childValue = list.get(childIndex);
		Object childPk = null;
		if (childValue != null) {
			childPk = ORMUtil.getPkValue(childValue,
					ORMUtil.getOrMapInfo(orMapper, childClass, tableMap));
		}
		// the root object put in the first position
		relationInfo.relationValues.add(1, childPk);
		fillParentRelationInfo(queryColumns, list, parentIndex, orMapper, relationInfo,
				tableMap);
		sb = new StringBuffer();
		if (relationInfo.relationPath != null) {
			sb.append(relationInfo.relationPath);
		}
		if (sb.length() > 0) {
			sb.append(SQLBuilder.DOT);
		}
		sb.append(fieldName);
		relationInfo.relationPath = sb.toString();
	}

	private int findParentIndex(QueryColumns queryColumns, Class<?> childObj,
			int childIndex) {
		int relationLevel = queryColumns.relationLevelMap.get(childObj);
		for (int i = childIndex - 1; i >= 0; i--) {
			if (i == relationLevel - 1) {
				return i;
			}
		}
		return -1;
	}

	@SuppressWarnings("unchecked")
	private Object matchRelatedValueInList(List<?> list, RelationInfo relationInfo,
			ORMapper orMapper, Map<Class<?>, String> tableMap,
			Map<Object, Object> resultCache) throws NoSuchFieldException,
			IllegalAccessException, InvocationTargetException {
		if (list == null || list.isEmpty()) {
			return null;
		}
		List<Object> relationValues = relationInfo.relationValues;
		String relationPath = relationInfo.relationPath;
		Object rootObject = matchValueInMap(resultCache, relationValues.get(0));
		Object rootObjectCopy = rootObject;
		if (relationPath == null) {
			return rootObject;
		}
		String[] paths = relationPath.split("\\.");

		for (Object l : list) {
			if (l == null) {
				continue;
			}
			rootObject = rootObjectCopy;
			for (int i = 0; i < paths.length; i++) {
				Object fieldValue = ReflectUtil.getValueOfField(rootObject,
						paths[i]);
				if (fieldValue == null) {
					break;
				}
				List<Object> ll = null;
				if (fieldValue instanceof Vector) {
					ll = (Vector<Object>) fieldValue;
					if (ll.isEmpty()) {
						break;
					}
				} else if (fieldValue instanceof List) {
					ll = (List<Object>) fieldValue;
					if (ll.isEmpty()) {
						break;
					}
				} else if (!fieldValue.getClass().isAssignableFrom(
						Collection.class)
						&& !fieldValue.getClass().isArray()
						&& !fieldValue.getClass().isAssignableFrom(Map.class)) {
					ll = new ArrayList<Object>();
					ll.add(fieldValue);
				}
				if (ll != null) {
					String pkField = ORMUtil.getOrMapInfo(orMapper,
							ll.get(0).getClass(), tableMap)
							.getPrimaryKeyField();
					rootObject = matchValueInList(ll, pkField,
							relationValues.get(i + 1));
				}
				if (i == paths.length - 1) {
					return rootObject;
				}
			}
		}
		return null;
	}

	private Object matchValueInMap(Map<Object, Object> map, Object basedValue) {
		if (map == null || map.isEmpty()) {
			return null;
		}
		if (basedValue == null) {
			return null;
		}
		return map.get(basedValue);
	}

	private Object matchValueInList(List<?> list, String basedField,
			Object basedValue) throws NoSuchFieldException,
			IllegalAccessException, InvocationTargetException {
		if (list == null || list.isEmpty()) {
			return null;
		}
		if (basedValue == null) {
			return null;
		}
		for (Object val : list) {
			if (val == null) {
				continue;
			}
			Object fieldValue = ReflectUtil.getValueOfField(val, basedField);
			if (basedValue.equals(fieldValue)) {
				return val;
			}
		}
		return null;
	}

	private List<Object> processCursorRow(ORMapper orMapper, ORMapInfo mapInfo,
			QueryColumns queryColumns, Map<Class<?>, String> tableMap)
			throws IllegalAccessException, InstantiationException,
			NoSuchFieldException {
		List<Object> results = new ArrayList<Object>();

		Class<?> clazz = mapInfo.getClazz();
		Object result = clazz.newInstance();
		ColumnIndex colIdx = queryColumns.columnIndexMap.get(mapInfo
				.getTableName());
		int startColumn = colIdx.startColumn;
		int lastColumn = colIdx.lastColumn;
		boolean isNullObj = true;
		for (int i = startColumn; i < lastColumn; i++) {
			String columnName = queryColumns.get(i).column;
			String fieldName = mapInfo.getColumnFieldMap().get(columnName);
			int columnIndex = i;
			Field field = clazz.getDeclaredField(fieldName);
			Class<?> fieldType = field.getType();

			Object convertValue = getValueConvertor().convertCursorToJavaValue(
					cursor, columnIndex, fieldType);
			if (convertValue != null) {
				isNullObj = false;
			}
			ReflectUtil.setValueOfField(result, fieldName, convertValue);
		}
		// if values of all fields are null, the object is set null
		if (isNullObj) {
			result = null;
		}
		results.add(result);
		// iterates all columns
		if (queryColumns.size() > lastColumn) {
			Class<?> childClazz = queryColumns.get(lastColumn).clazz;
			ORMapInfo childMapInfo = ORMUtil.getOrMapInfo(orMapper, childClazz,
					tableMap);
			results.addAll(processCursorRow(orMapper, childMapInfo,
					queryColumns, tableMap));
		}
		return results;
	}

	// private List<Object> processCursorRow0(ORMapper orMapper,
	// ORMapInfo mapInfo, QueryColumns queryColumns,
	// Map<Class<?>, String> tableMap) throws InstantiationException,
	// IllegalAccessException, NoSuchFieldException {
	// List<Object> results = new ArrayList<Object>();
	// Class<?> mainClass = mapInfo.getClazz();
	// // Object result = mainClass.newInstance();
	// Map<Class<?>, Object> resultMap = new HashMap<Class<?>, Object>();
	// Map<Class<?>, Boolean> isNotNullFlagMap = new HashMap<Class<?>,
	// Boolean>();
	// for (int i = 0; i < cursor.getColumnCount(); i++) {
	// int columnIndex = i;
	// String qryColumnName = cursor.getColumnName(columnIndex);
	// String columnName = null;
	// String tableName = null;
	// ORMapInfo mapInfoOfTable = null;
	// if (qryColumnName.contains("____")) {
	// String[] columnInfos = qryColumnName.split("____");
	// if (columnInfos.length == 2) {
	// tableName = columnInfos[0];
	// columnName = columnInfos[1];
	// mapInfoOfTable = ORMUtil.findORMInfoByTableName(mainClass,
	// tableName, orMapper, tableMap);
	// } else {
	// columnName = qryColumnName;
	// tableName = mapInfo.getTableName();
	// mapInfoOfTable = mapInfo;
	// }
	// } else {
	// columnName = qryColumnName;
	// tableName = mapInfo.getTableName();
	// mapInfoOfTable = mapInfo;
	// }
	// String fieldName = mapInfoOfTable.getColumnFieldMap().get(
	// columnName);
	// Class<?> clazzOfTable = mapInfoOfTable.getClazz();
	// Field field = clazzOfTable.getDeclaredField(fieldName);
	// Class<?> fieldType = field.getType();
	//
	// Object convertValue = getValueConvertor().convertCursorToJavaValue(
	// cursor, columnIndex, fieldType);
	// if (convertValue != null) {
	// // isNullObj = false;
	// isNotNullFlagMap.put(clazzOfTable, true);
	// }
	// Object result = resultMap.get(clazzOfTable);
	// if (result == null) {
	// result = clazzOfTable.newInstance();
	// }
	// ReflectUtil.setValueOfField(result, fieldName, convertValue);
	// }
	// Set<Entry<Class<?>, Boolean>> entrySet = isNotNullFlagMap.entrySet();
	// for (Entry<Class<?>, Boolean> entry : entrySet) {
	// if (entry.getValue() != null && entry.getValue()) {
	// results.add(resultMap.get(entry.getKey()));
	// }
	// }
	//
	// return results;
	// }
}
