package org.cyy.fw.android.dborm.sqlite;

/**
 * 
 * delete item<BR>
 * 
 * @author cyy
 * @version [V1.0, 2013-5-7]
 */
class DeleteItem {

	String tableName;

	String[] whereArgs;

	String whereClause;

	static DeleteItem createDeleteItem(String tableName, String whereClause,
			String[] whereArgValues) {
		DeleteItem delItem = new DeleteItem();
		delItem.tableName = tableName;
		delItem.whereClause = whereClause;
		delItem.whereArgs = whereArgValues;
		return delItem;
	}
}