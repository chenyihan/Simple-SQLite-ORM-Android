package org.cyy.fw.android.dborm.sqlite;

import org.cyy.fw.android.dborm.DBAccessException;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * 
 * DB Accessing template<BR>
 * Extract some common codes for accessing DB, such as will call
 * {@link SQLiteDatabase#beginTransaction()} when begin to access table, will
 * call {@link SQLiteDatabase#setTransactionSuccessful()} after accessing
 * successfully, and will call {@link SQLiteDatabase#endTransaction()} to end
 * the logic. The user's code only need to implement the {@link IDBAccessLogic}
 * interface. It's the sample code as follow:
 * 
 * <pre>
 * DBAccessTemplate dbAccessTemplate = new DBAccessTemplate();
 * dbAccessTemplate.accessDb(this.database, new IDBAccessLogic&lt;Void&gt;() {
 * 	&#064;Override
 * 	public Void doAccessLogic(SQLiteDatabase db) throws Exception {
 * 		String createSql = generateCreateSql(clazz);
 * 		database.execSQL(createSql);
 * 		return null;
 * 	}
 * });
 * </pre>
 * 
 * @author cyy
 * @version [V1.0, 2012-11-5]
 */
public final class DBAccessTemplate {

	/**
	 * DB Accessing logic<BR>
	 * 
	 * @author cyy
	 * @version [V1.0, 2012-11-5]
	 * @param <T>
	 */
	public static interface IDBAccessLogic<T> {
		/**
		 * 
		 * Access DB core logic<BR>
		 * 
		 * @param db
		 *            DB instance
		 * @return result
		 */
		T doAccessLogic(SQLiteDatabase db);

		/**
		 * 
		 * If need to open transaction while access the DB<BR>
		 * In general case, it's true for writing logic and false for reading
		 * logic
		 * 
		 * @return true: need transaction，false: need not
		 */
		boolean isOpenTransaction();
	}

	private static final String TAG = "DBAccessTemplate";

	/**
	 * 
	 * Access DB<BR>
	 * 
	 * @param db
	 *            DB instance
	 * @param logic
	 *            accessing logic
	 * @param <T>
	 * 
	 * @return result
	 */
	public <T> T accessDb(SQLiteDatabase db, IDBAccessLogic<T> logic) {
		if (logic.isOpenTransaction()) {
			return accessDBOpenTransaction(db, logic);
		}
		return accessDBWithoutOpenTransaction(db, logic);
	}

	private <T> T accessDBWithoutOpenTransaction(SQLiteDatabase db,
			IDBAccessLogic<T> logic) {
		Log.d(TAG, "accessDBWithoutOpenTransaction");
		T result = null;
		try {
			result = logic.doAccessLogic(db);
		} catch (SQLException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new DBAccessException(e);
		}
		return result;
	}

	private <T> T accessDBOpenTransaction(SQLiteDatabase db,
			IDBAccessLogic<T> logic) {
		Log.d(TAG, "accessDBOpenTransaction");
		T result = null;
		try {
			db.beginTransaction();
			result = logic.doAccessLogic(db);
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new DBAccessException(e);
		} finally {
			db.endTransaction();
		}

		Log.d(TAG, "accessDBOpenTransaction end");
		return result;
	}
}
