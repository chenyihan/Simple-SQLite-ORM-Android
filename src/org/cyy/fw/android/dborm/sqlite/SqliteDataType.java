package org.cyy.fw.android.dborm.sqlite;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * 
 * Data type supported by SQLite DB<BR>
 * Including:<code>NULL,INTEGER,REAL,TEXT,BLOB</code>
 * 
 * @author cyy
 * @version [V1.0, 2012-11-1]
 */
public enum SqliteDataType {

	/**
	 * BLOB type
	 */
	BLOB("BLOB") {
		@Override
		protected Class<?>[] toJavaTypes() {
			return new Class<?>[] { byte[].class, Byte[].class };
		}
	},
	/**
	 * integer type
	 */
	INTEGER("INTEGER") {
		@Override
		protected Class<?>[] toJavaTypes() {
			return new Class<?>[] { int.class, Integer.class, short.class,
					Short.class, Long.class, long.class };
		}
	},
	/**
	 * null type
	 */
	NULL("NULL") {
		@Override
		protected Class<?>[] toJavaTypes() {
			return new Class<?>[] {};
		}
	},
	/**
	 * float type, store 8-byte IEEE float
	 */
	REAL("REAL") {
		@Override
		protected Class<?>[] toJavaTypes() {
			return new Class<?>[] { float.class, Float.class, double.class,
					Double.class };
		}
	},
	/**
	 * text type
	 */
	TEXT("TEXT") {
		@Override
		protected Class<?>[] toJavaTypes() {
			return new Class<?>[] { String.class, char.class, Character.class,
					Boolean.class, boolean.class };
		}
	};
	private static Map<Class<?>, SqliteDataType> sTypeCache = new WeakHashMap<Class<?>, SqliteDataType>();
	private String dbType;

	private SqliteDataType(String pType) {
		this.dbType = pType;
	}

	/**
	 * 
	 * Convert java type to SQLite column type
	 * 
	 * @param javaType
	 *            java type
	 * @return SQLite column type
	 */
	public static String convertJavaTypeToDbType(Class<?> javaType) {
		return convertJavaTypeToSqliteDataType(javaType).getDbType();
	}

	/**
	 * 
	 * Convert java type to SQLite column enum type
	 * 
	 * @param javaType
	 *            java type
	 * @return SQLite column enum type
	 */
	public static SqliteDataType convertJavaTypeToSqliteDataType(
			Class<?> javaType) {
		SqliteDataType sqliteDataType = sTypeCache.get(javaType);
		if (sqliteDataType != null) {
			return sqliteDataType;
		}
		SqliteDataType[] allEnum = SqliteDataType.values();
		if (allEnum == null) {
			throw new IllegalArgumentException("SqliteDataType define error");
		}
		for (SqliteDataType t : allEnum) {
			Class<?>[] allSupportTypes = t.toJavaTypes();
			if (allSupportTypes == null) {
				throw new IllegalArgumentException(
						"SqliteDataType define error,the SqliteDataType does not support any java type:"
								+ javaType);
			}
			List<Class<?>> allSupportTypeList = Arrays.asList(allSupportTypes);
			if (allSupportTypeList.contains(javaType)) {
				sTypeCache.put(javaType, t);
				return t;
			}
		}
		throw new UnsupportedOperationException(
				"Please pass the correct java type,SqliteDataType cannot support this java type:"
						+ javaType);
	}

	public String getDbType() {
		return this.dbType;
	}

	protected abstract Class<?>[] toJavaTypes();
}
