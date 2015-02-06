package org.cyy.fw.android.dborm.sqlite;

import java.util.Map;

import org.cyy.fw.android.dborm.ORMapper;
import org.cyy.fw.android.dborm.sqlite.DBAccessTemplate.IDBAccessLogic;
import org.cyy.fw.android.dborm.sqlite.SQLBuilder.QuerySql;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

class QueryDataLogic<T> implements IDBAccessLogic<T[]> {
	private Class<T> clazz;
	private Map<Class<?>, String> tableMap;
	private QuerySql querySql;
	private ORMapper orMapper;
	private ValueConvertor valConvertor;

	QueryDataLogic(Class<T> clazz, Map<Class<?>, String> tableMap,
			QuerySql querySql, ORMapper orMapper, ValueConvertor valConvertor) {
		this.clazz = clazz;
		this.tableMap = tableMap;
		this.querySql = querySql;
		this.orMapper = orMapper;
		this.valConvertor = valConvertor;
	}

	@Override
	public T[] doAccessLogic(SQLiteDatabase db) {
		Cursor cursor = null;
		try {
			String sqlForQuery = querySql.combineSqlFragment();
			if (sqlForQuery == null) {
				return null;
			}
			Log.d(SqliteDBDelegator.TAG, "queryByConditionCascade query begin");
			cursor = db.rawQuery(sqlForQuery,
					querySql.whereObject.whereArgValues);
			Log.d(SqliteDBDelegator.TAG, "queryByConditionCascade query end");

			ResultsetProcessor processor = new ResultsetProcessor(cursor,
					valConvertor);
			Log.d(SqliteDBDelegator.TAG,
					"queryByConditionCascade combin resultset begin");
			// parse result set to POJO
			T[] results = processor.processCursor(orMapper, clazz,
					querySql.queryColumns, tableMap);
			Log.d(SqliteDBDelegator.TAG,
					"queryByConditionCascade combin resultset end");
			return results;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	@Override
	public boolean isOpenTransaction() {
		return false;
	}
}