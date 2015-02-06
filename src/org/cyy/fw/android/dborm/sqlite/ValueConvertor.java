package org.cyy.fw.android.dborm.sqlite;

import org.cyy.fw.android.dborm.ReflectUtil;

import android.database.Cursor;

/**
 * 
 * Convert between Java value and DB value
 * 
 * @author cyy
 * @version [V1.0, 2012-11-16]
 */
public interface ValueConvertor {
	/**
	 * 
	 * Convert the value of result set to JAVA type<BR>
	 * 
	 * @param cursor
	 * @param columnIndex
	 * @param javaType
	 * @return value after converting
	 */
	Object convertCursorToJavaValue(Cursor cursor, int columnIndex,
			Class<?> javaType);

	/**
	 * 
	 * Convert DB type to JAVA type<BR>
	 * 
	 * @param value
	 * @param javaType
	 * @return value after converting
	 */
	Object convertDBValueToJavaValue(Object value, Class<?> javaType);

	/**
	 * Convert JAVA type to DB type<BR>
	 * 
	 * @param value
	 * @param javaType
	 * @return value after converting
	 */
	Object convertJavaValueToDBValue(Object value, Class<?> javaType);
}

/**
 * 
 * Default value convertor<BR>
 * Convert boolean value true to Y and false to N while store in DB
 * 
 * @author cyy
 * @version [V1.0, 2012-11-16]
 */
class DefaultValueConvertor implements ValueConvertor {

	private static final String BOOLEAN_FALSE = "N";
	private static final String BOOLEAN_TRUE = "Y";

	@Override
	public Object convertCursorToJavaValue(Cursor cursor, int columnIndex,
			Class<?> javaType) {
		Object dbValue = null;
		// return directly if DB value is null, otherwise, may be get the
		// default value of primary type, such as 0 of integer type, false of
		// boolean type
		if (cursor.isNull(columnIndex)) {
			return dbValue;
		}
		if (ReflectUtil.isDoubleType(javaType)) {
			dbValue = cursor.getDouble(columnIndex);
		}
		if (ReflectUtil.isFloatType(javaType)) {
			dbValue = cursor.getFloat(columnIndex);
		}
		if (ReflectUtil.isIntegerType(javaType)) {
			dbValue = cursor.getInt(columnIndex);
		}
		if (ReflectUtil.isLongType(javaType)) {
			dbValue = cursor.getLong(columnIndex);
		}
		if (ReflectUtil.isShortType(javaType)) {
			dbValue = cursor.getShort(columnIndex);
		}
		if (javaType == String.class) {
			dbValue = cursor.getString(columnIndex);
		}
		// true->Y and false->N
		if (ReflectUtil.isBooleanType(javaType)) {
			dbValue = cursor.getString(columnIndex);
		}
		if (javaType == byte[].class || javaType == Byte[].class) {
			dbValue = cursor.getBlob(columnIndex);
		}

		Object convertValue = convertDBValueToJavaValue(dbValue, javaType);
		return convertValue;
	}

	@Override
	public Object convertDBValueToJavaValue(Object value, Class<?> javaType) {
		Object ret = value;
		if (value != null && ReflectUtil.isBooleanType(javaType)) {
			ret = value.equals(BOOLEAN_TRUE)
					|| value.equals(BOOLEAN_TRUE.toLowerCase()) ? true : false;
		}
		return ret;
	}

	@Override
	public Object convertJavaValueToDBValue(Object value, Class<?> javaType) {
		Object ret = value;
		if (value != null && ReflectUtil.isBooleanType(javaType)) {
			ret = (Boolean) value ? BOOLEAN_TRUE : BOOLEAN_FALSE;
		}
		return ret;
	}

}
