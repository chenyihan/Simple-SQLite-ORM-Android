package org.cyy.fw.android.dborm.sqlite;

import org.cyy.fw.android.dborm.sqlite.DBAccessTemplate.IDBAccessLogic;

import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

/**
 * 
 * 
 * @author cyy
 * @version [V1.0, 2013-5-7]
 */
class UpdateLogic implements IDBAccessLogic<Integer> {
	UpdateItem[] updateInfos;

	UpdateLogic(UpdateItem[] updateInfos) {
		this.updateInfos = updateInfos;
	}

	@Override
	public Integer doAccessLogic(SQLiteDatabase db) {
		int rows = 0;
		if (this.updateInfos == null) {
			return rows;
		}
		for (UpdateItem info : updateInfos) {
			if (info == null || TextUtils.isEmpty(info.tableName)) {
				continue;
			}
			rows += db.update(info.tableName, info.contentValues,
					info.whereClause, info.whereArgs);
		}
		return rows;
	}

	@Override
	public boolean isOpenTransaction() {
		return true;
	}

}