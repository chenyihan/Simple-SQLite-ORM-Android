package org.cyy.fw.android.dborm.sqlite;

import android.database.sqlite.SQLiteDatabase;

/**
 * 
 * The adapter of auto-upgrade tables<BR>
 * It should be implemented by the user's facade if the user want to the
 * framework check the table's alteration and upgrade the tables automatically
 * while DB upgrades, otherwise user must implement the
 * {@link SqliteDBFacade#onUpgrade(SQLiteDatabase, int, int)} for upgrading
 * logic.<br>
 * Note: because the framework will compare all columns of tables will all
 * attributes of POJOs, so if your application have a large amount of tables,
 * this process may be time-consuming.
 * 
 * @author cyy
 * @version [RCS Client V100R001C03, 2014-8-4]
 */
public interface DBAutoUpgradeAdapter {
	/**
	 * 
	 * Upgrade the tables<BR>
	 * 
	 * @param db
	 *            DB instance
	 */
	void upgradeTables(SQLiteDatabase db);
}
