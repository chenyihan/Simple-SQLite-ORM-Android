package org.cyy.fw.android.dborm;

import java.util.Arrays;
import java.util.List;

/**
 * 
 * SQL object includes SQL and binded parameter<BR>
 * 
 * @author cyy
 * @version [V1.0, 2012-11-29]
 */
public class SQLObject {
	private Object[] bindArgs;

	private String sql;

	public SQLObject(String sql, Object[] bindArgs) {
		super();
		this.sql = sql;
		this.bindArgs = bindArgs;
	}

	public static SQLObject[] createSqls(List<String> sqls) {
		if (sqls == null) {
			return null;
		}
		SQLObject[] sqlObjs = new SQLObject[sqls.size()];
		for (int i = 0; i < sqlObjs.length; i++) {
			sqlObjs[i] = new SQLObject(sqls.get(i), null);
		}
		return sqlObjs;
	}

	public Object[] getBindArgs() {
		return bindArgs;
	}

	public String getSql() {
		return sql;
	}

	public void setBindArgs(Object[] bindArgs) {
		this.bindArgs = bindArgs;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	@Override
	public String toString() {
		return "SQLObject [bindArgs=" + Arrays.toString(bindArgs) + ", sql="
				+ sql + "]";
	}

}
