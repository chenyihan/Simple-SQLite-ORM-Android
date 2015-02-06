package org.cyy.fw.android.dborm.sqlite;

/**
 * 
 * Aid parameter<BR>
 * Including group by, order by, having, limit, distinct and so on
 * 
 * @author cyy
 * @version [V1.0, 2012-11-5]
 */
public class QueryAidParameter {
	/**
	 * where parameter
	 */
	private String[] extraArgValues;
	/**
	 * extra where
	 */
	private String extraWherePart;
	/**
	 * group by
	 */
	private String groupBy;
	/**
	 * having
	 */
	private String having;

	/**
	 * limit
	 */
	private String limit;
	/**
	 * order by
	 */
	private String orderBy;

	private boolean distinct;

	public String[] getExtraArgValues() {
		return extraArgValues;
	}

	public String getExtraWherePart() {
		return extraWherePart;
	}

	public String getGroupBy() {
		return groupBy;
	}

	public String getHaving() {
		return having;
	}

	public String getLimit() {
		return limit;
	}

	public String getOrderBy() {
		return orderBy;
	}

	public boolean isDistinct() {
		return distinct;
	}

	public QueryAidParameter setExtraArgValues(String[] pExtraArgValues) {
		this.extraArgValues = pExtraArgValues;
		return this;
	}

	public QueryAidParameter setExtraWherePart(String pExtraWherePart) {
		this.extraWherePart = pExtraWherePart;
		return this;
	}

	public QueryAidParameter setGroupBy(String pGroupBy) {
		this.groupBy = pGroupBy;
		return this;
	}

	public QueryAidParameter setHaving(String pHaving) {
		this.having = pHaving;
		return this;
	}

	public QueryAidParameter setLimit(String pLimit) {
		this.limit = pLimit;
		return this;
	}

	public QueryAidParameter setOrderBy(String pOrderBy) {
		this.orderBy = pOrderBy;
		return this;
	}

	public QueryAidParameter setDistinct(boolean distinct) {
		this.distinct = distinct;
		return this;
	}

}
