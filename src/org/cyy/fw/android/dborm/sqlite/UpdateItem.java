package org.cyy.fw.android.dborm.sqlite;

import android.content.ContentValues;

/**
 * 
 * 
 * @author cyy
 * @version [V1.0, 2013-5-7]
 */
class UpdateItem {
	ContentValues contentValues;

	String tableName;

	String[] whereArgs;

	String whereClause;

	static UpdateItem createUpdateItem(String tableName,
			ContentValues contentValues, String whereClause,
			String[] whereArgValues) {
		UpdateItem updItem = new UpdateItem();
		updItem.tableName = tableName;
		updItem.contentValues = contentValues;
		updItem.whereClause = whereClause;
		updItem.whereArgs = whereArgValues;
		return updItem;
	}
}