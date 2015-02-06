package org.cyy.fw.android.dborm;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * 
 * Some useful tool method for ORM<BR>
 * 
 * @author cyy
 * @version [V1.0, 2012-11-19]
 */
public final class ORMUtil {
	private ORMUtil() {
		super();
	}

	/**
	 * 
	 * Look up relation target POJO class<BR>
	 * 
	 * @param mainClass
	 *            relation source class
	 * @param childFieldName
	 *            relation attribute name
	 * @return the target class, null if failed to look up
	 * @throws NoSuchFieldException
	 * 
	 */
	public static Class<?> findAssoTargetClass(Class<?> mainClass,
			String childFieldName) throws NoSuchFieldException {
		Field firstChildField = mainClass.getDeclaredField(childFieldName);
		Class<?> fieldType = firstChildField.getType();
		Class<?> childClass = null;
		if (fieldType.isAssignableFrom(List.class)
				|| fieldType.isAssignableFrom(Vector.class)) {
			// look up generic type
			ParameterizedType paraType = (ParameterizedType) firstChildField
					.getGenericType();
			// must specify the generic type
			if (paraType != null) {
				Class<?> paraTypeClass = (Class<?>) paraType
						.getActualTypeArguments()[0];
				childClass = paraTypeClass;
			}
		} else if (!fieldType.isAssignableFrom(Collection.class)
				&& !fieldType.isArray()
				&& !fieldType.isAssignableFrom(Map.class)) {
			childClass = fieldType;
		}
		return childClass;
	}

	/**
	 * 
	 * Obtain the O-R mapping info of the specified POJO class<BR>
	 * 
	 * @param orMapper
	 *            mapping tool
	 * @param clazz
	 *            POJO class
	 * @param tableMap
	 *            table names map
	 * @return O-R mapping info
	 */
	public static ORMapInfo getOrMapInfo(ORMapper orMapper, Class<?> clazz,
			Map<Class<?>, String> tableMap) {
		ORMapInfo mapInfo = orMapper.generateColumnMap(clazz,
				tableMap == null ? null : tableMap.get(clazz));
		return mapInfo;
	}

	// public static ORMapInfo findORMInfoByTableName(Class<?> mainClass,
	// String tableName, ORMapper orMapper, Map<Class<?>, String> tableMap) {
	// ORMapInfo mapInfo = getOrMapInfo(orMapper, mainClass, tableMap);
	// if (mapInfo == null) {
	// return null;
	// }
	// if (ObjectUtil.isEqual(mapInfo.getTableName(), tableName)) {
	// return mapInfo;
	// }
	// Set<Entry<Class<?>, AssociateInfo>> entrySet = mapInfo
	// .getAssociateInfo().entrySet();
	// for (Entry<Class<?>, AssociateInfo> entry : entrySet) {
	// AssociateInfo assInfo = entry.getValue();
	// if (assInfo == null) {
	// continue;
	// }
	// Class<?> childClass = assInfo.getTarget();
	// ORMapInfo relationORMInfo = findORMInfoByTableName(childClass,
	// tableName, orMapper, tableMap);
	// if (relationORMInfo != null) {
	// return relationORMInfo;
	// }
	// }
	// return null;
	// }

	/**
	 * 
	 * Obtain the primary key value of POJO<BR>
	 * 
	 * @param value
	 *            POJO
	 * @param mapInfo
	 *            O-R mapping info
	 * @return the value of primary key
	 */
	public static Object getPkValue(Object value, ORMapInfo mapInfo) {
		if (value == null) {
			throw new NullPointerException(
					"invalid paramter, pass non-null value please");
		}
		String pkFieldName = mapInfo.getPrimaryKeyField();
		if (pkFieldName == null) {
			return null;
		}
		Object fieldValue = ReflectUtil.getValueOfField(value, pkFieldName);
		return fieldValue;
	}

	/**
	 * 
	 * mapping attributes to columns<BR>
	 * 
	 * @param fields
	 *            names of POJO's attributes
	 * @param mapInfo
	 *            O-R mapping info
	 * @return columns of DB table
	 */
	public static String[] mappingColumnByField(String[] fields,
			ORMapInfo mapInfo) {
		if (fields == null) {
			return null;
		}
		List<String> columnList = new ArrayList<String>();
		for (String field : fields) {
			String column = mapInfo.getFieldColumnMap().get(field);
			if (column == null) {
				continue;
			}
			columnList.add(column);
		}
		return columnList.toArray(new String[columnList.size()]);
	}

	public static String createUID(UniqueIDGenerator idGenerator) {
		return (String) idGenerator.generateUID();
	}

	/**
	 * 
	 * filter out primary keys of null value<BR>
	 * 
	 * @param pks
	 *            original primary keys
	 * @return primary keys after filtering out
	 */
	public static Object[] filtNullPks(Object[] pks) {
		List<Object> list = new ArrayList<Object>();
		for (Object pk : pks) {
			if (pk != null && !list.contains(pk)) {
				list.add(pk);
			}
		}
		return list.toArray(new Object[list.size()]);
	}
}
