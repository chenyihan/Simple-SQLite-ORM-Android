package org.cyy.fw.android.dborm.sqlite;

import org.cyy.fw.android.dborm.SQLObject;
import org.cyy.fw.android.dborm.sqlite.DBAccessTemplate.IDBAccessLogic;

import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

/**
 * 
 * 
 * @author cyy
 * @version [RCS Client V100R001C03, 2014-12-31]
 */
class ExecSqlsLogic implements IDBAccessLogic<Void> {
	private final SQLObject[] execSqls;

	ExecSqlsLogic(SQLObject[] execSqls) {
		this.execSqls = execSqls;
	}

	@Override
	public Void doAccessLogic(SQLiteDatabase db) {
		for (SQLObject execSql : execSqls) {
			if (execSql == null) {
				continue;
			}
			String sql = execSql.getSql();
			if (TextUtils.isEmpty(sql)) {
				continue;
			}
			Log.d(SqliteDBDelegator.TAG, "exec sql:" + sql);
			if (execSql.getBindArgs() == null) {
				db.execSQL(sql);
			} else {
				db.execSQL(sql, execSql.getBindArgs());
			}
		}
		return null;
	}

	@Override
	public boolean isOpenTransaction() {
		return true;
	}
}