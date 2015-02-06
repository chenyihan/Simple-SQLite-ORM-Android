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
class InsertLogic implements IDBAccessLogic<long[]> {
	InsertItem[] insertItems;

	public InsertLogic(InsertItem[] insertItems) {
		this.insertItems = insertItems;
	}

	@Override
	public long[] doAccessLogic(SQLiteDatabase db) {
		if (insertItems == null) {
			return null;
		}
		long[] rowIds = new long[insertItems.length];
		// insert item
		for (int i = 0; i < insertItems.length; i++) {
			InsertItem ins = insertItems[i];
			if (TextUtils.isEmpty(ins.tableName)) {
				continue;
			}
			rowIds[i] = db.insert(ins.tableName, null, ins.contentValues);
		}
		return rowIds;
	}

	@Override
	public boolean isOpenTransaction() {
		return true;
	}

}