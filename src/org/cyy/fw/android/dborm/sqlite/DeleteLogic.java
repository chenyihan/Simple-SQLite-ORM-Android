package org.cyy.fw.android.dborm.sqlite;

import org.cyy.fw.android.dborm.sqlite.DBAccessTemplate.IDBAccessLogic;

import android.database.sqlite.SQLiteDatabase;

/**
 * 
 * 
 * 
 * @author cyy
 * @version [V1.0, 2013-5-7]
 */
class DeleteLogic implements IDBAccessLogic<Integer> {

	DeleteItem[] delInfos;

	DeleteLogic(DeleteItem[] delInfos) {
		super();
		this.delInfos = delInfos;
	}

	@Override
	public Integer doAccessLogic(SQLiteDatabase db) {
		if (this.delInfos == null) {
			return 0;
		}
		int rows = 0;
		for (int i = 0; i < this.delInfos.length; i++) {
			DeleteItem info = this.delInfos[i];
			if (info == null) {
				continue;
			}
			rows += db.delete(info.tableName, info.whereClause, info.whereArgs);
		}
		return rows;
	}

	@Override
	public boolean isOpenTransaction() {
		return true;
	}

}