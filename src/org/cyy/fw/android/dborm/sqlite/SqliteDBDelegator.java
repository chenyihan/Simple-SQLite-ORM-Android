package org.cyy.fw.android.dborm.sqlite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.cyy.fw.android.dborm.AnnotationORMapper;
import org.cyy.fw.android.dborm.ORMUtil;
import org.cyy.fw.android.dborm.ORMapInfo;
import org.cyy.fw.android.dborm.ORMapper;
import org.cyy.fw.android.dborm.ObjectUtil;
import org.cyy.fw.android.dborm.SQLObject;
import org.cyy.fw.android.dborm.UniqueIDGenerator;
import org.cyy.fw.android.dborm.sqlite.DBAccessTemplate.IDBAccessLogic;
import org.cyy.fw.android.dborm.sqlite.SQLBuilder.QuerySql;
import org.cyy.fw.android.dborm.sqlite.SQLBuilder.WhereObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

/**
 * 
 * SQLite DB proxy<BR>
 * 
 * @author cyy
 * @version [V1.0, 2012-11-5]
 */
class SqliteDBDelegator {

	static final String TAG = "SqliteDBDelegator";

	private SQLiteDatabase database;

	private DBAccessTemplate dbAccessTemplate;

	private UniqueIDGenerator mIDGenerator;

	private ORMapper orMapper;

	private SQLBuilder sqlBuilder;

	private ValueConvertor valueConvertor;

	SqliteDBDelegator() {
		this(null);
	}

	SqliteDBDelegator(SQLiteDatabase db) {
		this.database = db;
		this.dbAccessTemplate = new DBAccessTemplate();
	}

	void upgradeTables(Class<?>[] classes) {
		Map<String, List<String>> colMap = listColumns(classes);
		List<SQLObject> sqls = new ArrayList<SQLObject>();
		for (Class<?> clazz : classes) {
			ORMapInfo[] orMapInfos = getOrMapper().generateAllTableColumnMap(
					clazz);
			for (ORMapInfo orMapInfo : orMapInfos) {
				String table = orMapInfo.getTableName();
				List<String> oldColumns = colMap.get(table);
				if (oldColumns == null || oldColumns.isEmpty()) {
					String createSql = getSqlBuilder().buildCreateTableSql(
							clazz, table);
					if (createSql != null) {
						Log.d(TAG, "sql:" + createSql);
						sqls.add(new SQLObject(createSql, null));
					}
					continue;
				}
				List<String> addedColumns = checkNewAddedColumns(clazz,
						oldColumns);
				for (String addedColumn : addedColumns) {
					SQLObject addColumnSQL = getSqlBuilder().buildAddColumnSQL(
							clazz, table, addedColumn);
					Log.d(TAG, "sql:" + addColumnSQL.getSql());
					sqls.add(addColumnSQL);
				}
			}
		}
		if (!sqls.isEmpty()) {
			execSQL(sqls.toArray(new SQLObject[0]));
		}
	}

	private List<String> checkNewAddedColumns(Class<?> clazz,
			List<String> oldColumns) {
		List<String> newAddedColumns = new ArrayList<String>();
		ORMapInfo orMapInfo = getOrMapper().generateColumnMap(clazz, null);
		Map<String, String> newColumnMap = orMapInfo.getColumnFieldMap();
		Set<Entry<String, String>> entrySet = newColumnMap.entrySet();
		for (Entry<String, String> entry : entrySet) {
			String newColumn = entry.getKey();
			String lowerColumn = newColumn;
			// DB columns are not case-sensitive
			if (lowerColumn != null) {
				lowerColumn = newColumn.toLowerCase(Locale.US);
			}
			if (!oldColumns.contains(lowerColumn)) {
				newAddedColumns.add(newColumn);
			}
		}
		return newAddedColumns;
	}

	private Map<String, List<String>> listColumns(Class<?>[] classes) {
		Map<String, List<String>> colMap = new HashMap<String, List<String>>();
		for (Class<?> clazz : classes) {
			if (clazz == null) {
				continue;
			}
			listColumns(clazz, colMap);
		}
		return colMap;
	}

	private void listColumns(Class<?> clazz, Map<String, List<String>> sqlMap) {
		if (clazz == null) {
			return;
		}
		Map<String, String> sqls = getSqlBuilder().generateListColumnsOfTable(
				clazz);
		Set<Entry<String, String>> sqlEntrySet = sqls.entrySet();
		for (Entry<String, String> entry : sqlEntrySet) {
			List<String> columns = new ArrayList<String>();
			Cursor cursor = null;
			try {
				String sql = entry.getValue();
				cursor = rawQuery(sql, null);
				if (cursor.getCount() > 0) {
					cursor.moveToFirst();
					do {
						int columnIndex = cursor.getColumnIndex("name");
						String name = cursor.getString(columnIndex);
						// DB columns are not case-sensitive, convert to lower
						// case uniformity
						if (name != null) {
							name = name.toLowerCase(Locale.US);
						}
						columns.add(name);
					} while (cursor.moveToNext());
				}
			} finally {
				sqlMap.put(entry.getKey(), columns);
				if (cursor != null) {
					cursor.close();
				}
			}
		}
	}

	void createTable(Class<?> clazz) {
		createTables(new Class<?>[] { clazz });
	}

	void createTables(Class<?>[] classes) {
		if (classes == null || classes.length == 0) {
			return;
		}
		final List<String> createSqls = new ArrayList<String>();
		// 生成建表SQL
		for (Class<?> clazz : classes) {
			Log.d(TAG, "create table:" + clazz.getName());
			createSqls.addAll(getSqlBuilder().buildCreateTableSql(clazz));
		}
		SQLObject[] sqlObjs = SQLObject.createSqls(createSqls);
		dbAccessTemplate.accessDb(this.database, new ExecSqlsLogic(sqlObjs));
	}

	String createTempTable(Class<?> clazz) {
		ORMapInfo mapInfo = getOrMapper().generateColumnMap(clazz, null);
		String tempTableName = getSqlBuilder().generateTempTableName(
				mapInfo.getTableName());
		// generate create table SQL
		final String createSql = getSqlBuilder().buildCreateTempTableSql(clazz,
				mapInfo, tempTableName);
		if (TextUtils.isEmpty(createSql)) {
			return null;
		}
		SQLObject[] sqlObjs = new SQLObject[] { new SQLObject(createSql, null) };
		dbAccessTemplate.accessDb(this.database, new ExecSqlsLogic(sqlObjs));
		return tempTableName;
	}

	int delete(Class<?> clazz, Object condition, QueryAidParameter parameter,
			String tableName) {

		if (condition == null && clazz == null) {
			throw new IllegalArgumentException("paramter error.");
		}
		int rows = 0;
		Class<?> classOfObject = clazz;
		if (classOfObject == null) {
			classOfObject = condition.getClass();
		}
		ORMapInfo mapInfo = getOrMapper().generateColumnMap(classOfObject,
				tableName);

		WhereObject whereObj = getSqlBuilder().buildWhereInfo(condition,
				mapInfo, getValueConvertor());

		String wherePart = SQLBuilder.arrangeWherePart(whereObj, parameter).whereArg;
		String[] whereValue = SQLBuilder.arrangeWhereArgValues(whereObj,
				parameter).whereArgValues;

		String tableName0 = mapInfo.getTableName();
		rows = this.dbAccessTemplate.accessDb(
				this.database,
				new DeleteLogic(new DeleteItem[] { DeleteItem.createDeleteItem(
						tableName0, wherePart, whereValue) }));
		return rows;

	}

	int deleteByPks(Class<?> clazz, Object[] pks, String tableName) {

		int rows = 0;
		if (clazz == null || pks == null) {
			throw new IllegalArgumentException("paramter error.");
		}
		if (pks.length == 0) {
			return rows;
		}
		DeleteItem[] delItems = new DeleteItem[pks.length];
		for (int i = 0; i < pks.length; i++) {
			ORMapInfo mapInfo = getOrMapper().generateColumnMap(clazz,
					tableName);

			String pkStr = pks[i].toString();
			WhereObject whereObj = getSqlBuilder().buildPkWhereInfo(pkStr,
					mapInfo, getValueConvertor());
			String tableName0 = mapInfo.getTableName();
			delItems[i] = DeleteItem.createDeleteItem(tableName0,
					whereObj.whereArg, whereObj.whereArgValues);
		}
		rows = this.dbAccessTemplate.accessDb(this.database, new DeleteLogic(
				delItems));
		return rows;

	}

	void dropTable(final Class<?> clazz) {
		dropTables(new Class<?>[] { clazz });
	}

	void dropTable(final String table) {
		if (table == null) {
			return;
		}
		dropTables(new String[] { table });
	}

	void dropTables(Class<?>[] classes) {
		if (classes == null) {
			return;
		}
		List<SQLObject> dropSqlObjs = new ArrayList<SQLObject>();
		// generate drop SQL
		for (Class<?> clazz : classes) {
			List<SQLObject> sqlList = Arrays.asList(getSqlBuilder()
					.buildDropSqls(clazz));
			dropSqlObjs.addAll(sqlList);
		}
		dbAccessTemplate.accessDb(this.database,
				new ExecSqlsLogic(dropSqlObjs.toArray(new SQLObject[0])));
	}

	void dropTables(String[] tables) {
		if (tables == null || tables.length == 0) {
			return;
		}
		final List<String> dropSqls = new ArrayList<String>();
		// generate drop SQL
		for (String table : tables) {
			dropSqls.add(getSqlBuilder().buildDropSql(table));
		}
		SQLObject[] sqlObjs = SQLObject.createSqls(dropSqls);
		dbAccessTemplate.accessDb(this.database, new ExecSqlsLogic(sqlObjs));
	}

	void execSQL(final SQLObject[] execSqls) {
		if (ObjectUtil.isArrayEmpty(execSqls)) {
			return;
		}
		this.dbAccessTemplate.accessDb(this.database, new ExecSqlsLogic(
				execSqls));
	}

	void execSQL(final String sql) {
		SQLObject[] sqlObjs = new SQLObject[] { new SQLObject(sql, null) };
		execSQL(sqlObjs);
	}

	private ORMapper getOrMapper() {
		if (this.orMapper == null) {
			this.orMapper = new AnnotationORMapper();
		}
		return orMapper;
	}

	private ValueConvertor getValueConvertor() {
		if (valueConvertor == null) {
			valueConvertor = new DefaultValueConvertor();
		}
		return valueConvertor;
	}

	long insert(Object object, String table) {
		long rowId = -1;
		if (object == null) {
			return rowId;
		}
		Map<Class<?>, String> tableMap = new HashMap<Class<?>, String>();
		tableMap.put(object.getClass(), table);
		long[] results = insert(new Object[] { object }, tableMap);
		if (ObjectUtil.isArrayEmpty(results)) {
			return -1;
		}
		return results[0];
	}

	long[] insert(Object[] objects, Map<Class<?>, String> tableMap) {
		return insertCascade(objects, new Class<?>[] {}, tableMap);
	}

	long[] insertCascade(Object[] objects, Class<?>[] childrenClasses,
			Map<Class<?>, String> tableMap) {

		if (objects == null || objects.length == 0) {
			return null;
		}
		long[] rowIds = new long[objects.length];
		if (objects.length == 0) {
			return rowIds;
		}

		final List<InsertItem> insertItemQueue = InsertItem.fillInsertItems(
				objects, childrenClasses, tableMap, getOrMapper(),
				getValueConvertor(), mIDGenerator);
		rowIds = this.dbAccessTemplate.accessDb(
				this.database,
				new InsertLogic(insertItemQueue
						.toArray(new InsertItem[insertItemQueue.size()])));
		return rowIds;

	}

	@SuppressWarnings("unchecked")
	<T> T[] query(Class<T> clazz, T condition, String[] fields,
			final QueryAidParameter parameterObject, String tableName) {

		if (condition == null && clazz == null) {
			throw new IllegalArgumentException("paramter error.");
		}
		Class<T> classOfObject = clazz;
		if (classOfObject == null) {
			classOfObject = (Class<T>) condition.getClass();
		}
		// final ORMapInfo mapInfo = getOrMapper().generateColumnMap(
		// classOfObject, tableName);
		final Class<T> c = classOfObject;
		final QuerySql querySql = getSqlBuilder().buildQuerySqlByCondition(c,
				null, condition, getValueConvertor(), null);
		querySql.setAidParameter(parameterObject);

		// T[] result = this.dbAccessTemplate.accessDb(this.database,
		// new QueryLogic<T>(querySql, mapInfo, clazz,
		// getValueConvertor(), getOrMapper()));
		T[] result = this.dbAccessTemplate.accessDb(this.database,
				new QueryDataLogic<T>(clazz, null, querySql, getOrMapper(),
						getValueConvertor()));
		return result;

	}

	<T> T[] queryCascadeByCondition(final Class<T> clazz, T condition,
			final String[] childrenPaths, final Map<Class<?>, String> tableMap) {
		if (condition == null && clazz == null) {
			throw new IllegalArgumentException("paramter error.");
		}
		Log.d(TAG, "queryByConditionCascade build sql begin");

		QuerySql querySql = getSqlBuilder().buildQuerySqlByCondition(clazz,
				childrenPaths, condition, getValueConvertor(), tableMap);
		Log.d(TAG, "queryByConditionCascade build sql end");
		T[] results = null;
		if (querySql == null) {
			return results;
		}
		results = this.dbAccessTemplate.accessDb(this.database,
				new QueryDataLogic<T>(clazz, tableMap, querySql, getOrMapper(),
						getValueConvertor()));
		return results;
	}

	<T> T queryByPk(final Class<T> clazz, Object pk, final String tableName) {

		if (clazz == null || pk == null) {
			throw new IllegalArgumentException("paramter error.");
		}
		T[] datasInDB = queryByPks(clazz, new Object[] { pk }, tableName);
		if (ObjectUtil.isArrayEmpty(datasInDB)) {
			return null;
		}
		return datasInDB[0];

	}

	<T> T[] queryByPks(final Class<T> clazz, Object[] pks, String tableName) {

		Map<Class<?>, String> tableMap = new HashMap<Class<?>, String>();
		tableMap.put(clazz, tableName);
		return queryCascadeByPks(clazz, pks, null, tableMap);

	}

	<T> T[] queryCascadeByPks(final Class<T> clazz, Object[] pks,
			final String[] childrenPaths, final Map<Class<?>, String> tableMap) {
		T[] results = null;
		if (clazz == null || pks == null) {
			throw new IllegalArgumentException("paramter error.");
		}

		Object[] pkFiltered = ORMUtil.filtNullPks(pks);
		if (pkFiltered.length == 0) {
			return null;
		}
		String[] argValues = ObjectUtil.toStringArray(pkFiltered);

		QuerySql querySql = getSqlBuilder().buildQuerySqlByPks(clazz,
				childrenPaths, argValues, tableMap);
		if (querySql == null) {
			return results;
		}
		results = this.dbAccessTemplate.accessDb(this.database,
				new QueryDataLogic<T>(clazz, tableMap, querySql, getOrMapper(),
						getValueConvertor()));
		return results;
	}

	Cursor rawQuery(final String sql, final String[] selectionArgs) {
		Cursor cursor = this.dbAccessTemplate.accessDb(this.database,
				new IDBAccessLogic<Cursor>() {

					@Override
					public Cursor doAccessLogic(SQLiteDatabase db) {
						return db.rawQuery(sql, selectionArgs);
					}

					@Override
					public boolean isOpenTransaction() {
						return false;
					}
				});
		return cursor;
	}

	void setDatabase(SQLiteDatabase database) {
		this.database = database;
	}

	void setOrMapper(ORMapper orMapper) {
		this.orMapper = orMapper;
	}

	void setValueConvertor(ValueConvertor valueConvertor) {
		this.valueConvertor = valueConvertor;
	}

	void setUIDGenerator(UniqueIDGenerator gen) {
		mIDGenerator = gen;
	}

	int update(Object newValue, Object condition, String[] notIgnoreNullFields,
			String tableName) {

		int rows = 0;
		if (newValue == null) {
			return rows;
		}
		Class<?> clazz = newValue.getClass();
		ORMapInfo mapInfo = getOrMapper().generateColumnMap(clazz, tableName);
		ContentValues contentValues = ContentValuesBuilder
				.generateContentValues(newValue, mapInfo, null,
						notIgnoreNullFields, getValueConvertor(), mIDGenerator);
		WhereObject whereObj = getSqlBuilder().buildWhereInfo(condition,
				mapInfo, getValueConvertor());
		String tableName0 = mapInfo.getTableName();
		UpdateItem updateItem = UpdateItem.createUpdateItem(tableName0,
				contentValues, whereObj.whereArg, whereObj.whereArgValues);
		rows = this.dbAccessTemplate.accessDb(database, new UpdateLogic(
				new UpdateItem[] { updateItem }));
		return rows;

	}

	int updateByPks(Object[] values, String[] notIgnoreNullFields,
			String tableName) {

		int rows = 0;
		if (values == null || values.length == 0) {
			return rows;
		}
		UpdateItem[] updInfos = new UpdateItem[values.length];
		for (int i = 0; i < values.length; i++) {
			Object value = values[i];
			Class<?> clazz = value.getClass();
			ORMapInfo mapInfo = getOrMapper().generateColumnMap(clazz,
					tableName);
			String pkFieldName = mapInfo.getPrimaryKeyField();
			ContentValues contentValues = ContentValuesBuilder
					.generateContentValues(value, mapInfo,
							new String[] { pkFieldName }, notIgnoreNullFields,
							getValueConvertor(), mIDGenerator);
			Object pk = ORMUtil.getPkValue(value, mapInfo);
			if (pk == null) {
				throw new IllegalArgumentException(
						"Invalid parameter, can't pass a null pk");
			}

			String pkStr = pk.toString();
			WhereObject whereObj = getSqlBuilder().buildPkWhereInfo(pkStr,
					mapInfo, getValueConvertor());
			String tableName0 = mapInfo.getTableName();
			UpdateItem updItem = UpdateItem.createUpdateItem(tableName0,
					contentValues, whereObj.whereArg, whereObj.whereArgValues);
			updInfos[i] = updItem;
		}
		rows = this.dbAccessTemplate.accessDb(this.database, new UpdateLogic(
				updInfos));
		return rows;

	}

	private SQLBuilder getSqlBuilder() {
		if (this.sqlBuilder == null) {
			this.sqlBuilder = new SQLBuilder(getOrMapper());
		}
		return this.sqlBuilder;
	}

}
