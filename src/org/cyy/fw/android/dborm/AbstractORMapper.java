package org.cyy.fw.android.dborm;

import java.util.Map;
import java.util.WeakHashMap;

import android.text.TextUtils;

/**
 * 
 * abstract mapper class<BR>
 * Caches the orm info
 * 
 * @author cyy
 * @version [V1.0, 2012-11-5]
 */
public abstract class AbstractORMapper implements ORMapper {
	private static Map<String, ORMapInfo> sORMapInfoCache = new WeakHashMap<String, ORMapInfo>();

	@Override
	public ORMapInfo[] generateAllTableColumnMap(Class<?> clazz) {
		// find from cache first
		String[] tableNames = obtainTableName(clazz);
		if (ObjectUtil.isArrayEmpty(tableNames)) {
			throw new POJOClassDefineException("Class define error:"
					+ clazz.getName());
		}
		ORMapInfo[] maps = new ORMapInfo[tableNames.length];
		for (int i = 0; i < tableNames.length; i++) {
			maps[i] = generateColumnMap(clazz, tableNames[i]);
		}
		return maps;
	}

	@Override
	public ORMapInfo generateColumnMap(Class<?> clazz) {

		// find from cache first
		String[] tableNames = obtainTableName(clazz);
		if (ObjectUtil.isArrayEmpty(tableNames)) {
			throw new POJOClassDefineException("Class define error:"
					+ clazz.getName());
		}
		String tableName = tableNames[0];
		return generateColumnMap(clazz, tableName);
	}

	@Override
	public ORMapInfo generateColumnMap(Class<?> clazz, String tableName) {
		if (TextUtils.isEmpty(tableName)) {
			return generateColumnMap(clazz);
		}

		// Logger.d(TAG, "generateColumnMap:" + tableName);

		ORMapInfo mapInfo = obtainMapInfoFromCache(tableName);
		if (mapInfo == null) {
			mapInfo = createORMap(clazz, tableName);
			// caches the orm info
			putMapInfoIntoCache(mapInfo);
		}
		return mapInfo;
	}

	/**
	 * 
	 * Create the O-R map information<BR>
	 * 
	 * @param clazz
	 *            POJO class
	 * @param tableName
	 *            table name
	 * @return map info
	 */
	protected abstract ORMapInfo createORMap(Class<?> clazz, String tableName);

	/**
	 * 
	 * Find O-R map info from cache<BR>
	 * 
	 * @param tableName
	 *            table name
	 * @return map info
	 */
	protected ORMapInfo obtainMapInfoFromCache(String tableName) {
		return sORMapInfoCache.get(tableName);
	}

	/**
	 * 
	 * Obtain table name, may be several<BR>
	 * 
	 * @param clazz
	 *            POJO class
	 * @return table name
	 */
	protected abstract String[] obtainTableName(Class<?> clazz);

	protected void putMapInfoIntoCache(ORMapInfo mapInfo) {
		// String className = mapInfo.getClazzOfObject().getName();
		String tableName = mapInfo.getTableName();
		sORMapInfoCache.put(tableName, mapInfo);
	}

}
