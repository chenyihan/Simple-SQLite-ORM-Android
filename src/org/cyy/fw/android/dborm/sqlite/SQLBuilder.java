package org.cyy.fw.android.dborm.sqlite;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import org.cyy.fw.android.dborm.ORMUtil;
import org.cyy.fw.android.dborm.ORMapInfo;
import org.cyy.fw.android.dborm.ORMapInfo.AssociateInfo;
import org.cyy.fw.android.dborm.ORMapper;
import org.cyy.fw.android.dborm.ObjectUtil;
import org.cyy.fw.android.dborm.OneToAny;
import org.cyy.fw.android.dborm.POJOClassDefineException;
import org.cyy.fw.android.dborm.ReflectUtil;
import org.cyy.fw.android.dborm.SQLObject;
import org.cyy.fw.android.dborm.UniqueIDGenerator;

import android.text.TextUtils;

/**
 * 
 * SQL builder<BR>
 * 
 * @author cyy
 * @version [V1.0, 2012-11-19]
 */
class SQLBuilder {

	static class QueryColumn {
		Class<?> clazz;

		String column;

		String fieldName;

		int relationLevel;

		String tableName;
	}

	static class ColumnIndex {
		// first column, include
		int startColumn;

		// last column, exclude
		int lastColumn;

		String tableName;
	}

	static class QueryColumns extends ArrayList<QueryColumn> {

		private static final long serialVersionUID = 2391978379267772811L;
		transient Map<Class<?>, Integer> relationLevelMap = new HashMap<Class<?>, Integer>();

		transient Map<String, ColumnIndex> columnIndexMap;

		@Override
		public void add(int index, QueryColumn object) {
			if (!relationLevelMap.containsKey(object.clazz)) {
				relationLevelMap.put(object.clazz, object.relationLevel);
			}
			super.add(index, object);
		}

		@Override
		public boolean add(QueryColumn object) {
			if (!relationLevelMap.containsKey(object.clazz)) {
				relationLevelMap.put(object.clazz, object.relationLevel);
			}
			return super.add(object);
		}

		@Override
		public boolean addAll(Collection<? extends QueryColumn> collection) {
			if (collection != null) {
				for (QueryColumn c : collection) {
					if (!relationLevelMap.containsKey(c.clazz)) {
						relationLevelMap.put(c.clazz, c.relationLevel);
					}
				}
				return super.addAll(collection);
			}
			return false;
		}

		@Override
		public boolean addAll(int index,
				Collection<? extends QueryColumn> collection) {
			if (collection != null) {
				for (QueryColumn c : collection) {
					if (!relationLevelMap.containsKey(c.clazz)) {
						relationLevelMap.put(c.clazz, c.relationLevel);
					}
				}
			}
			return super.addAll(index, collection);
		}

		@Override
		public QueryColumn remove(int index) {
			QueryColumn c = this.get(index);
			if (relationLevelMap.containsKey(c.clazz)) {
				relationLevelMap.remove(c.clazz);
			}
			return super.remove(index);
		}

		@Override
		public boolean remove(Object object) {
			QueryColumn c = (QueryColumn) object;
			if (relationLevelMap.containsKey(c.clazz)) {
				relationLevelMap.remove(c.clazz);
			}
			return super.remove(object);
		}

		@Override
		protected void removeRange(int fromIndex, int toIndex) {
			for (int i = fromIndex; i < toIndex; i++) {
				QueryColumn c = this.get(i);
				if (relationLevelMap.containsKey(c.clazz)) {
					relationLevelMap.remove(c.clazz);
				}
			}
			super.removeRange(fromIndex, toIndex);
		}

		void generateColumnIndexMaper() {
			if (columnIndexMap == null) {
				columnIndexMap = new HashMap<String, ColumnIndex>();
			} else {
				columnIndexMap.clear();
			}
			for (int i = 0; i < size(); i++) {
				QueryColumn qryCol = get(i);
				String tableName = qryCol.tableName;
				ColumnIndex colIdx = columnIndexMap.get(tableName);
				if (colIdx == null) {
					colIdx = new ColumnIndex();
					colIdx.startColumn = i;
					columnIndexMap.put(tableName, colIdx);
				}
				if (colIdx.lastColumn < i + 1) {
					colIdx.lastColumn = i + 1;
				}
			}
		}
	}

	static class QuerySql {
		StringBuffer from = new StringBuffer();

		QueryColumns queryColumns = new QueryColumns();

		// StringBuffer select = new StringBuffer();
		WhereObject whereObject = new WhereObject();

		private QueryAidParameter aidParameter;

		String combineSqlFragment() {
			StringBuffer sql = new StringBuffer();
			StringBuffer select = this.buildSelectSegment();
			if (select.toString().endsWith(COMMA)) {
				select.replace(select.length() - 1, select.length(), "");
			}
			if (from.toString().endsWith(COMMA)) {
				from.replace(from.length() - 1, from.length(), "");
			}
			if (select.length() <= 0) {
				return null;
			}
			sql.append(" select ");
			if (isDistinct()) {
				sql.append(" distinct ");
			}
			sql.append(select);
			sql.append(" from ");
			sql.append(from);

			arrangeWherePart(whereObject, aidParameter);
			arrangeWhereArgValues(whereObject, aidParameter);

			if (whereObject.whereArg != null
					&& whereObject.whereArg.length() > 0) {
				sql.append(" where ");
				sql.append(whereObject.whereArg);
			}

			String aidPart = appendAidParam();
			sql.append(aidPart);

			return sql.toString();
		}

		private boolean isDistinct() {
			return aidParameter != null && aidParameter.isDistinct();
		}

		private String appendAidParam() {
			StringBuffer sb = new StringBuffer();
			if (aidParameter == null) {
				return sb.toString();
			}
			if (!TextUtils.isEmpty(aidParameter.getGroupBy())) {
				sb.append(" group by ");
				sb.append(aidParameter.getGroupBy());
				sb.append(BLANK);
				if (!TextUtils.isEmpty(aidParameter.getHaving())) {
					sb.append(" having ");
					sb.append(aidParameter.getHaving());
					sb.append(BLANK);
				}
			}
			if (!TextUtils.isEmpty(aidParameter.getOrderBy())) {
				sb.append(" order by ");
				sb.append(aidParameter.getOrderBy());
				sb.append(BLANK);
			}
			if (!TextUtils.isEmpty(aidParameter.getLimit())) {
				sb.append(" limit ");
				sb.append(aidParameter.getLimit());
			}
			return sb.toString();
		}

		void setAidParameter(QueryAidParameter aidParameter) {
			this.aidParameter = aidParameter;
		}

		private StringBuffer buildSelectSegment() {
			StringBuffer select = new StringBuffer();
			if (this.queryColumns == null) {
				return select;
			}
			for (QueryColumn column : this.queryColumns) {
				String tableName = column.tableName;
				String columnName = column.column;
				select.append(BLANK);
				select.append(tableName);
				select.append(DOT);
				select.append(columnName);
				select.append(COMMA);
			}
			return select;
		}
	}

	static class WhereObject {
		String whereArg;

		String[] whereArgValues;
	}

	static final String DOT = ".";

	private static final String BLANK = " ";

	private static final String COMMA = ",";

	// private static final String EMPTY = "";
	private static final String LEFT_PARENTHESIS = "(";

	private static final String QUESTION = "?";

	private static final String RIGHT_PARENTHESIS = ")";

	private static final String UNDERLINE = "_";

	private ORMapper orMapper;

	SQLBuilder(ORMapper orMapper) {
		this.orMapper = orMapper;
	}

	QuerySql buildQuerySqlByCondition(Class<?> mainClass,
			String[] childrenPaths, Object condition,
			ValueConvertor typeValueConvertor, Map<Class<?>, String> tableMap) {
		ORMapInfo mapInfo = getOrMapInfo(mainClass, tableMap);
		Map<String, String> columnFieldMap = mapInfo.getColumnFieldMap();
		if (columnFieldMap.isEmpty()) {
			return null;
		}
		QuerySql querySql = new QuerySql();
		try {
			fillSqlFragmentByCondition(mainClass, childrenPaths, querySql,
					condition, typeValueConvertor, tableMap);
		} catch (NoSuchFieldException e) {
			throw new POJOClassDefineException("Class is defined error:"
					+ e.getMessage(), e);
		}
		return querySql;
	}

	QuerySql buildQuerySqlByPks(Class<?> mainClass, String[] childrenPaths,
			String[] pks, Map<Class<?>, String> tableMap) {
		ORMapInfo mapInfo = getOrMapInfo(mainClass, tableMap);
		Map<String, String> columnFieldMap = mapInfo.getColumnFieldMap();
		if (columnFieldMap.isEmpty()) {
			return null;
		}
		QuerySql querySql = new QuerySql();
		try {
			fillSqlFragmentByPks(mainClass, childrenPaths, querySql, pks,
					tableMap);
		} catch (NoSuchFieldException e) {
			throw new POJOClassDefineException("Class is defined error:"
					+ e.getMessage(), e);
		}
		return querySql;
	}

	List<String> buildCreateTableSql(Class<?> clazz) {
		// ORMapInfo mapInfo = this.orMapper.generateColumnMap(clazz);
		List<String> sqls = new ArrayList<String>();
		ORMapInfo[] maps = this.orMapper.generateAllTableColumnMap(clazz);
		for (ORMapInfo m : maps) {
			String sql = genCreateTableSql(clazz, m);
			if (TextUtils.isEmpty(sql)) {
				continue;
			}
			sqls.add(sql);
		}
		return sqls;
	}

	List<SQLObject> buildCreateTableSqlObj(Class<?> clazz) {
		List<String> sqls = buildCreateTableSql(clazz);
		if (sqls == null) {
			return null;
		}
		List<SQLObject> sqlObjs = new ArrayList<SQLObject>();
		for (String sql : sqls) {
			sqlObjs.add(new SQLObject(sql, null));
		}
		return sqlObjs;
	}

	String buildCreateTableSql(Class<?> clazz, String tableName) {
		ORMapInfo map = this.orMapper.generateColumnMap(clazz, tableName);
		String sql = genCreateTableSql(clazz, map);
		return sql;
	}

	String buildCreateTempTableSql(Class<?> clazz, ORMapInfo mapInfo,
			String tempTableName) {
		StringBuffer sql = new StringBuffer();
		sql.append("create temp table ");
		sql.append(tempTableName);
		sql.append(LEFT_PARENTHESIS);
		String columnSql = buildCreateTableColumnSql(clazz, mapInfo, true);

		if (TextUtils.isEmpty(columnSql)) {
			return null;
		}
		sql.append(RIGHT_PARENTHESIS);
		return sql.toString();
	}

	SQLObject buildDeleteSql(Object condition, Class<?> clazz,
			ValueConvertor typeValueConvertor, String tableName) {
		if (condition == null && clazz == null) {
			return null;
		}
		// List<Object> argList = new ArrayList<Object>();
		Class<?> c = clazz;
		if (c == null) {
			c = condition.getClass();
		}
		ORMapInfo orMapInfo = orMapper.generateColumnMap(c, tableName);
		Map<String, String> fieldColumnMap = orMapInfo.getFieldColumnMap();
		if (fieldColumnMap.isEmpty()) {
			return null;
		}
		WhereObject whereObj = buildWhereInfo(condition, orMapInfo,
				typeValueConvertor);
		StringBuffer sql = new StringBuffer();
		sql.append(" delete ");
		sql.append(" from ");
		sql.append(orMapInfo.getTableName());
		if (whereObj != null && whereObj.whereArg != null) {
			sql.append(" where ");
			sql.append(whereObj.whereArg);
		}

		SQLObject sqlObj = new SQLObject(sql.toString(),
				whereObj.whereArgValues == null ? new Object[0]
						: whereObj.whereArgValues);
		return sqlObj;
	}

	SQLObject[] buildDropSqls(Class<?> clazz) {
		List<SQLObject> sqlList = new ArrayList<SQLObject>();
		ORMapInfo[] maps = orMapper.generateAllTableColumnMap(clazz);
		for (ORMapInfo m : maps) {
			String tableName = m.getTableName();
			String dropSql = buildDropSql(tableName);
			if (TextUtils.isEmpty(dropSql)) {
				continue;
			}
			SQLObject sqlObj = new SQLObject(dropSql, null);
			sqlList.add(sqlObj);
		}
		return sqlList.toArray(new SQLObject[0]);
	}

	String buildDropSql(String tableName) {
		StringBuffer sb = new StringBuffer();
		sb.append("drop table if exists ");
		sb.append(tableName);
		return sb.toString();
	}

	SQLObject[] buildInsertSql(Object[] entities,
			ValueConvertor typeValueConvertor, Map<Class<?>, String> tableMap,
			UniqueIDGenerator mIDGenerator) {
		if (ObjectUtil.isArrayEmpty(entities)) {
			return null;
		}
		SQLObject[] sqlObjs = new SQLObject[entities.length];
		for (int i = 0; i < sqlObjs.length; i++) {
			sqlObjs[i] = buildInsertSql(entities[i], typeValueConvertor,
					tableMap, mIDGenerator);
		}
		return sqlObjs;
	}

	SQLObject[] buildCascadeInsertSqls(Object[] entities,
			Class<?>[] includeClasses, ValueConvertor typeValueConvertor,
			Map<Class<?>, String> tableMap, UniqueIDGenerator mIDGenerator) {
		if (ObjectUtil.isArrayEmpty(entities)) {
			return null;
		}
		List<Class<?>> includeClassList = null;
		if (includeClasses != null) {
			includeClassList = Arrays.asList(includeClasses);
		}
		List<SQLObject> sqlObjs = new ArrayList<SQLObject>();
		try {
			// SQLObject[] sqlObjs = new SQLObject[entities.length];
			for (int i = 0; i < entities.length; i++) {
				buildCascadeInsertSql(entities[i], includeClassList,
						typeValueConvertor, tableMap, mIDGenerator, sqlObjs);
			}
		} catch (SecurityException e) {
			throw new POJOClassDefineException("Class is defined error:"
					+ e.getMessage(), e);
		} catch (NoSuchFieldException e) {
			throw new POJOClassDefineException("Class is defined error:"
					+ e.getMessage(), e);
		}
		return sqlObjs.toArray(new SQLObject[0]);
	}

	WhereObject buildPkWhereInfo(String pk, ORMapInfo mapInfo,
			ValueConvertor valueConvertor) {
		WhereObject whereObj = new WhereObject();
		if (pk == null) {
			return whereObj;
		}
		String pkField = mapInfo.getPrimaryKeyField();
		Class<?> clazz = mapInfo.getClazz();
		try {
			Object condition = clazz.newInstance();
			ReflectUtil.setValueOfField(condition, pkField, pk);
			return buildWhereInfo(condition, mapInfo, valueConvertor);
		} catch (InstantiationException e) {
			throw new POJOClassDefineException(e);
		} catch (IllegalAccessException e) {
			throw new POJOClassDefineException(e);
		}

	}

	SQLObject buildUpdateSql(Object newValue, Object condition,
			ValueConvertor typeValueConvertor, String[] notIgnoreNullFields,
			String tableName) {
		if (newValue == null) {
			return null;
		}
		Class<?> clazz = newValue.getClass();
		ORMapInfo orMapInfo = orMapper.generateColumnMap(clazz, tableName);
		Map<String, String> fieldColumnMap = orMapInfo.getFieldColumnMap();
		if (fieldColumnMap.isEmpty()) {
			return null;
		}
		// StringBuffer values = new StringBuffer();
		List<String> notIgnoreNullFieldList = null;
		if (notIgnoreNullFields != null) {
			notIgnoreNullFieldList = Arrays.asList(notIgnoreNullFields);
		}
		// int i = 0;
		Map<String, Object> valueMap = new HashMap<String, Object>();
		for (Entry<String, String> entry : fieldColumnMap.entrySet()) {
			String fieldName = entry.getKey();
			String columnName = entry.getValue();
			// Object fieldValue = ReflectUtil.getValue(newValue, fieldName);
			Object fieldValue = ReflectUtil
					.getValueOfField(newValue, fieldName);
			Object dbValue = null;
			if (fieldValue != null) {
				dbValue = typeValueConvertor.convertJavaValueToDBValue(
						fieldValue, fieldValue.getClass());
			}
			if ((notIgnoreNullFieldList != null && notIgnoreNullFieldList
					.contains(fieldName)) || dbValue != null) {
				valueMap.put(columnName, dbValue);
			}
		}
		List<Object> argList = new ArrayList<Object>();
		StringBuffer columns = new StringBuffer();

		int i = 0;
		for (Entry<String, Object> entry : valueMap.entrySet()) {
			argList.add(entry.getValue());
			columns.append(entry.getKey());
			columns.append(" = ");
			columns.append(" ? ");
			if (i < valueMap.size() - 1) {
				columns.append(COMMA);
			}
			i++;
		}
		WhereObject whereObj = buildWhereInfo(condition, orMapInfo,
				typeValueConvertor);
		if (whereObj != null && whereObj.whereArgValues != null) {
			argList.addAll(Arrays.asList(whereObj.whereArgValues));
		}
		StringBuffer sql = new StringBuffer();
		sql.append(" update ");
		sql.append(orMapInfo.getTableName());
		sql.append(" set ");
		sql.append(columns.toString());
		if (whereObj.whereArg != null && whereObj.whereArg.length() > 0) {
			sql.append(" where ");
			sql.append(whereObj.whereArg);
		}
		SQLObject sqlObj = new SQLObject(sql.toString(),
				argList.toArray(new Object[argList.size()]));
		return sqlObj;
	}

	SQLObject[] buildUpdateSqls(Object newValue, Object[] conditions,
			ValueConvertor typeValueConvertor, String[] notIgnoreNullFields,
			String tableName) {
		if (ObjectUtil.isArrayEmpty(conditions)) {
			return new SQLObject[] { buildUpdateSql(newValue, null,
					typeValueConvertor, notIgnoreNullFields, tableName) };
		}
		SQLObject[] sqlObjs = new SQLObject[conditions.length];
		for (int i = 0; i < sqlObjs.length; i++) {
			sqlObjs[i] = buildUpdateSql(newValue, conditions[i],
					typeValueConvertor, notIgnoreNullFields, tableName);
		}
		return sqlObjs;
	}

	WhereObject buildWhereInfo(Object condition, ORMapInfo mapInfo,
			ValueConvertor typeValueConvertor) {
		WhereObject whereObj = new WhereObject();
		if (condition == null) {
			return whereObj;
		}
		Class<?> clazz = condition.getClass();
		Field[] fields = clazz.getDeclaredFields();
		StringBuffer sb = new StringBuffer();
		List<String> argValues = new ArrayList<String>();
		int size = 0;
		for (Field f : fields) {
			// not mapping a table column
			String columnName = mapInfo.getFieldColumnMap().get(f.getName());
			if (columnName == null) {
				continue;
			}
			// exclude static field
			if (Modifier.isStatic(f.getModifiers())) {
				continue;
			}
			// exclude final field
			if (Modifier.isFinal(f.getModifiers())) {
				continue;
			}
			Object fieldValue = ReflectUtil.getValueOfField(condition,
					f.getName());

			fieldValue = typeValueConvertor.convertJavaValueToDBValue(
					fieldValue, f.getType());
			if (fieldValue != null) {
				sb.append(BLANK);
				if (size > 0) {
					sb.append("and");
				}
				sb.append(BLANK);
				sb.append(mapInfo.getTableName());
				sb.append(DOT);
				sb.append(columnName);
				sb.append(" = ? ");
				argValues.add(fieldValue.toString());
				size++;
			}
		}
		if (sb.length() > 0) {
			whereObj.whereArg = sb.toString();
			whereObj.whereArgValues = argValues.toArray(new String[argValues
					.size()]);
		}
		return whereObj;
	}

	static WhereObject arrangeWherePart(WhereObject whereObj,
			QueryAidParameter parameterObject) {
		if (parameterObject == null) {
			return whereObj;
		}
		final StringBuffer wherePart = new StringBuffer();
		if (whereObj.whereArg != null && whereObj.whereArg.length() > 0) {
			wherePart.append(whereObj.whereArg);
		}
		// extra where part
		if (parameterObject.getExtraWherePart() != null
				&& parameterObject.getExtraWherePart().length() > 0) {
			if (wherePart.length() > 0) {
				wherePart.append(" and ");
			}
			wherePart.append(parameterObject.getExtraWherePart());
		}
		if (wherePart.length() > 0) {
			whereObj.whereArg = wherePart.toString();
		} else {
			whereObj.whereArg = null;
		}
		return whereObj;
	}

	static WhereObject arrangeWhereArgValues(WhereObject whereObj,
			QueryAidParameter parameterObject) {
		if (parameterObject == null) {
			return whereObj;
		}
		final List<String> argValues = new ArrayList<String>();
		if (whereObj.whereArgValues != null) {
			argValues.addAll(Arrays.asList(whereObj.whereArgValues));
		}
		if (parameterObject.getExtraArgValues() != null) {
			argValues
					.addAll(Arrays.asList(parameterObject.getExtraArgValues()));
		}
		if (argValues.isEmpty()) {
			whereObj.whereArgValues = null;
		} else {
			whereObj.whereArgValues = argValues.toArray(new String[argValues
					.size()]);
		}
		return whereObj;
	}

	String generateTempTableName(String orgiTableName) {
		StringBuffer tempTableName = new StringBuffer(orgiTableName);
		tempTableName.append(UNDERLINE);
		tempTableName.append(Math.random());
		return tempTableName.toString();
	}

	Map<String, String> generateListColumnsOfTable(Class<?> clazz) {
		Map<String, String> sqlMap = new HashMap<String, String>();
		ORMapInfo[] mapInfos = orMapper.generateAllTableColumnMap(clazz);
		for (ORMapInfo mapInfo : mapInfos) {
			// mapInfo = orMapper.generateColumnMap(clazz);
			String tableName = mapInfo.getTableName();
			StringBuffer sb = new StringBuffer();
			sb.append("pragma table_info ");
			sb.append("('");
			sb.append(tableName);
			sb.append("')");
			// sqls.add(sb.toString());
			sqlMap.put(tableName, sb.toString());
		}
		return sqlMap;
	}

	ORMapInfo getOrMapInfo(Class<?> clazz, Map<Class<?>, String> tableMap) {
		ORMapInfo mapInfo = orMapper.generateColumnMap(clazz,
				tableMap == null ? null : tableMap.get(clazz));
		return mapInfo;
	}

	private void addQueryColumnInfo(QuerySql querySql, ORMapInfo mapInfo,
			int relationLevel) {
		Map<String, String> columnFieldMap = mapInfo.getColumnFieldMap();
		if (columnFieldMap == null || columnFieldMap.isEmpty()) {
			return;
		}
		String tableName = mapInfo.getTableName();
		Set<String> columns = columnFieldMap.keySet();
		for (String column : columns) {
			// buildSelect0(querySql, tableName, column);
			QueryColumn queryColumn = new QueryColumn();
			queryColumn.tableName = tableName;
			queryColumn.column = column;
			queryColumn.clazz = mapInfo.getClazz();
			queryColumn.fieldName = mapInfo.getColumnFieldMap().get(column);
			queryColumn.relationLevel = relationLevel;
			querySql.queryColumns.add(queryColumn);
		}
	}

	List<SQLObject> buildAddColumnSQLs(Class<?> clazz, String column) {
		List<SQLObject> sqlObjs = new ArrayList<SQLObject>();
		// ORMapInfo orMapInfo = orMapper.generateColumnMap(clazz, tableName);
		ORMapInfo[] orMapInfos = orMapper.generateAllTableColumnMap(clazz);
		for (ORMapInfo orMapInfo : orMapInfos) {
			if (orMapInfo == null) {
				continue;
			}
			sqlObjs.add(buildAddColumnSQL(clazz, orMapInfo.getTableName(),
					column));
		}
		return sqlObjs;
	}

	SQLObject buildAddColumnSQL(Class<?> clazz, String table, String column) {
		try {
			ORMapInfo orMapInfo = orMapper.generateColumnMap(clazz, table);
			String fieldName = orMapInfo.getColumnFieldMap().get(column);
			Field field = clazz.getDeclaredField(fieldName);
			Class<?> fieldType = field.getType();
			String dbType = SqliteDataType.convertJavaTypeToDbType(fieldType);
			StringBuffer sb = new StringBuffer();
			sb.append("ALTER TABLE ");
			sb.append(table);
			sb.append(" add COLUMN ");
			sb.append(column);
			sb.append(" ");
			sb.append(dbType);
			sb.append(" ");
			SQLObject sqlObj = new SQLObject(sb.toString(), null);
			return sqlObj;
		} catch (NoSuchFieldException e) {
			throw new POJOClassDefineException("Class is defined error:"
					+ clazz.getName() + "|" + e.getMessage(), e);
		}
	}

	private String buildCreateTableColumnSql(Class<?> clazz, ORMapInfo mapInfo,
			boolean isTempTable) {
		StringBuffer columnSql = new StringBuffer();
		try {
			String primaryColumn = mapInfo.getPrimaryKeyColumn();
			boolean hasColumn = false;
			if (primaryColumn != null) {
				Field primaryField = clazz.getDeclaredField(mapInfo
						.getPrimaryKeyField());
				Class<?> primaryFieldType = primaryField.getType();
				String primaryFieldDbType = SqliteDataType
						.convertJavaTypeToDbType(primaryFieldType);
				columnSql.append(primaryColumn);
				columnSql.append(BLANK);
				columnSql.append(primaryFieldDbType);
				columnSql.append(" primary key");
				// auto-increment
				if (mapInfo.isPkSequence() && !isTempTable) {
					columnSql.append(" autoincrement ");
				}
				columnSql.append(COMMA);
				hasColumn = true;
			}
			Map<String, String> columnsMap = mapInfo.getFieldColumnMap();
			Set<Entry<String, String>> entrySet = columnsMap.entrySet();
			for (Entry<String, String> entry : entrySet) {
				String fieldName = entry.getKey();
				// Primary key has already been processed
				if (ObjectUtil.isEqual(fieldName, mapInfo.getPrimaryKeyField())) {
					continue;
				}
				Field field = clazz.getDeclaredField(fieldName);
				Class<?> fieldType = field.getType();
				String dbType = SqliteDataType
						.convertJavaTypeToDbType(fieldType);
				String columnName = entry.getValue();
				columnSql.append(columnName);
				columnSql.append(BLANK);
				columnSql.append(dbType);
				columnSql.append(COMMA);
				hasColumn = true;
			}
			if (!hasColumn) {
				return null;
			}
			// delete the last comma sign
			columnSql.replace(columnSql.length() - 1, columnSql.length(), "");
		} catch (NoSuchFieldException e) {
			throw new POJOClassDefineException("Class is defined error:"
					+ clazz.getName() + "|" + e.getMessage(), e);
		}
		return columnSql.toString();
	}

	private void buildCascadeInsertSql(Object entity,
			List<Class<?>> includeClassList, ValueConvertor typeValueConvertor,
			Map<Class<?>, String> tableMap, UniqueIDGenerator mIDGenerator,
			List<SQLObject> results) throws SecurityException,
			NoSuchFieldException {
		if (entity == null) {
			return;
		}
		SQLObject mainTableSqlObj = buildInsertSql(entity, typeValueConvertor,
				tableMap, mIDGenerator);
		if (mainTableSqlObj != null) {
			results.add(mainTableSqlObj);
		}

		Class<?> clazz = entity.getClass();
		ORMapInfo orMapInfo = ORMUtil.getOrMapInfo(orMapper, clazz, tableMap);
		Map<Class<?>, AssociateInfo> assoInfoMap = orMapInfo.getAssociateInfo();
		if (assoInfoMap == null || assoInfoMap.isEmpty()) {
			return;
		}
		Set<Entry<Class<?>, AssociateInfo>> entrySet = assoInfoMap.entrySet();
		for (Entry<Class<?>, AssociateInfo> entry : entrySet) {
			Class<?> target = entry.getKey();
			if (includeClassList != null && !includeClassList.contains(target)) {
				continue;
			}
			AssociateInfo assoInfo = entry.getValue();
			String fieldName = assoInfo.getField();
			Field field = clazz.getDeclaredField(fieldName);
			Class<?> fieldType = field.getType();
			Object fieldValue = ReflectUtil.getValueOfField(entity, fieldName);
			Object sourceValue = ReflectUtil.getValueOfField(entity,
					assoInfo.getSourceField());

			if (fieldType.isAssignableFrom(List.class)) {
				List<?> valueList = (List<?>) fieldValue;
				if (valueList == null || valueList.isEmpty()) {
					continue;
				}
				for (int i = 0; i < valueList.size(); i++) {
					ReflectUtil.setValueOfField(valueList.get(i),
							assoInfo.getTargetField(), sourceValue);
					buildCascadeInsertSql(valueList.get(i), includeClassList,
							typeValueConvertor, tableMap, mIDGenerator, results);
				}
			} else if (fieldType.isAssignableFrom(Vector.class)) {
				Vector<?> valueList = (Vector<?>) fieldValue;
				if (valueList == null || valueList.isEmpty()) {
					continue;
				}
				for (int i = 0; i < valueList.size(); i++) {
					ReflectUtil.setValueOfField(valueList.get(i),
							assoInfo.getTargetField(), sourceValue);
					buildCascadeInsertSql(valueList.get(i), includeClassList,
							typeValueConvertor, tableMap, mIDGenerator, results);
				}
			} else if (!fieldType.isAssignableFrom(Collection.class)
					&& !fieldType.isArray()
					&& !fieldType.isAssignableFrom(Map.class)) {
				if (fieldValue == null) {
					continue;
				}
				ReflectUtil.setValueOfField(fieldValue,
						assoInfo.getTargetField(), sourceValue);
				buildCascadeInsertSql(fieldValue, includeClassList,
						typeValueConvertor, tableMap, mIDGenerator, results);
			}
		}
	}

	private SQLObject buildInsertSql(Object entity,
			ValueConvertor typeValueConvertor, Map<Class<?>, String> tableMap,
			UniqueIDGenerator mIDGenerator) {
		if (entity == null) {
			return null;
		}
		List<Object> argList = new ArrayList<Object>();
		Class<?> clazz = entity.getClass();
		ORMapInfo orMapInfo = ORMUtil.getOrMapInfo(orMapper, clazz, tableMap);
		Map<String, String> fieldColumnMap = orMapInfo.getFieldColumnMap();
		if (fieldColumnMap.isEmpty()) {
			return null;
		}
		StringBuffer columns = new StringBuffer();
		StringBuffer values = new StringBuffer();
		int i = 0;
		for (Entry<String, String> entry : fieldColumnMap.entrySet()) {
			String fieldName = entry.getKey();
			String columnName = entry.getValue();
			Object fieldValue = ReflectUtil.getValueOfField(entity, fieldName);
			Object dbValue = null;
			if (fieldValue != null) {
				dbValue = typeValueConvertor.convertJavaValueToDBValue(
						fieldValue, fieldValue.getClass());
			}
			// primary key generated by framework
			else if (columnName.equals(orMapInfo.getPrimaryKeyField())
					&& orMapInfo.isGenUIDBySelf()) {
				dbValue = ORMUtil.createUID(mIDGenerator);
				ReflectUtil.setValueOfField(entity, fieldName, dbValue);
			}
			argList.add(dbValue);
			values.append(" ? ");
			columns.append(columnName);
			if (i < fieldColumnMap.size() - 1) {
				values.append(COMMA);
				columns.append(COMMA);
			}
			i++;
		}
		StringBuffer sql = new StringBuffer();
		sql.append(" insert into ");
		sql.append(orMapInfo.getTableName());
		sql.append(LEFT_PARENTHESIS);
		sql.append(columns.toString());
		sql.append(RIGHT_PARENTHESIS);
		sql.append(" values ");
		sql.append(LEFT_PARENTHESIS);
		sql.append(values.toString());
		sql.append(RIGHT_PARENTHESIS);
		SQLObject sqlObj = new SQLObject(sql.toString(),
				argList.toArray(new Object[argList.size()]));
		return sqlObj;
	}

	private StringBuffer buildJoinSql(Class<?> mainClass, Class<?> childClass,
			Map<Class<?>, String> tableMap) {
		StringBuffer joinSql = new StringBuffer();
		ORMapInfo mapInfo = getOrMapInfo(mainClass, tableMap);
		String tableName = mapInfo.getTableName();
		Map<String, String> fieldColumnMap = mapInfo.getFieldColumnMap();
		Map<Class<?>, AssociateInfo> assInfoMap = mapInfo.getAssociateInfo();
		AssociateInfo assInfo = assInfoMap.get(childClass);
		ORMapInfo childMapInfo = getOrMapInfo(childClass, tableMap);
		String childTableName = childMapInfo.getTableName();
		joinSql.append(" left join ");
		joinSql.append(childTableName);
		joinSql.append(" on ");
		joinSql.append(tableName);
		joinSql.append(DOT);
		joinSql.append(fieldColumnMap.get(assInfo.getSourceField()));
		joinSql.append(" = ");
		joinSql.append(childTableName);
		joinSql.append(DOT);
		joinSql.append(childMapInfo.getFieldColumnMap().get(
				assInfo.getTargetField()));
		return joinSql;
	}

	private WhereObject buildWhereSqlByPks(Class<?> mainClass, String[] pks,
			Map<Class<?>, String> tableMap) {
		WhereObject whereObj = new WhereObject();
		if (mainClass == null || ObjectUtil.isArrayEmpty(pks)) {
			return whereObj;
		}
		StringBuffer whereSql = new StringBuffer();
		ORMapInfo mapInfo = getOrMapInfo(mainClass, tableMap);
		String tableName = mapInfo.getTableName();
		if (whereSql.length() > 0) {
			whereSql.append(" and ");
		}
		String primaryKeyColumn = mapInfo.getPrimaryKeyColumn();
		whereSql.append(LEFT_PARENTHESIS);
		// append with or
		for (int i = 0; i < pks.length; i++) {
			if (i > 0) {
				whereSql.append(" or ");
			}
			whereSql.append(tableName);
			whereSql.append(DOT);
			whereSql.append(primaryKeyColumn);
			whereSql.append(" = ");
			whereSql.append(QUESTION);
		}
		whereSql.append(RIGHT_PARENTHESIS);
		whereObj.whereArg = whereSql.toString();
		whereObj.whereArgValues = pks;
		return whereObj;
	}

	private void fillAssoTableSql(Class<?> mainClass, String childPath,
			QuerySql querySql, int relationLevel, Map<Class<?>, String> tableMap)
			throws NoSuchFieldException {
		if (childPath == null) {
			return;
		}
		int index = childPath.indexOf(DOT);
		String firstChildFieldName = null;
		String remainPath = null;
		if (index == -1) {
			firstChildFieldName = childPath;
		} else {
			firstChildFieldName = childPath.substring(0, index);
			remainPath = childPath.substring(index + 1);
		}

		Class<?> childClass = findAssoTargetClass(mainClass,
				firstChildFieldName);
		if (childClass == null) {
			return;
		}

		if (!hasJoined(querySql.queryColumns, childClass)) {
			ORMapInfo childMapInfo = getOrMapInfo(childClass, tableMap);
			addQueryColumnInfo(querySql, childMapInfo, relationLevel);

			StringBuffer joinSql = this.buildJoinSql(mainClass, childClass,
					tableMap);
			querySql.from.append(joinSql);
		}

		if (remainPath != null) {
			fillAssoTableSql(childClass, remainPath, querySql, relationLevel + 1,
					tableMap);
		}
	}

	private void fillSqlFragmentByCondition(Class<?> mainClass,
			String[] childrenPaths, QuerySql querySql, Object condition,
			ValueConvertor typeValueConvertor, Map<Class<?>, String> tableMap)
			throws NoSuchFieldException {
		ORMapInfo mapInfo = getOrMapInfo(mainClass, tableMap);
		String tableName = mapInfo.getTableName();
		addQueryColumnInfo(querySql, mapInfo, 0);
		querySql.from.append(tableName);
		querySql.whereObject = buildWhereInfo(condition, mapInfo,
				typeValueConvertor);
		if (ObjectUtil.isArrayEmpty(childrenPaths)) {
			return;
		}
		// fill relation table SQL
		for (String childPath : childrenPaths) {
			fillAssoTableSql(mainClass, childPath, querySql, 1, tableMap);
		}
	}

	private void fillSqlFragmentByPks(Class<?> mainClass,
			String[] childrenPaths, QuerySql querySql, String[] pks,
			Map<Class<?>, String> tableMap) throws NoSuchFieldException {
		ORMapInfo mapInfo = getOrMapInfo(mainClass, tableMap);
		String tableName = mapInfo.getTableName();
		addQueryColumnInfo(querySql, mapInfo, 0);
		querySql.from.append(tableName);
		querySql.whereObject = buildWhereSqlByPks(mainClass, pks, tableMap);
		if (ObjectUtil.isArrayEmpty(childrenPaths)) {
			return;
		}
		for (String childPath : childrenPaths) {
			fillAssoTableSql(mainClass, childPath, querySql, 1, tableMap);
		}
	}

	private Class<?> findAssoTargetClass(Class<?> mainClass,
			String childFieldName) throws NoSuchFieldException {
		Field firstChildField = mainClass.getDeclaredField(childFieldName);
		Class<?> fieldType = firstChildField.getType();
		Class<?> childClass = null;
		if (fieldType.isAssignableFrom(List.class)
				|| fieldType.isAssignableFrom(Vector.class)) {
			OneToAny assAnno = firstChildField.getAnnotation(OneToAny.class);
			if (assAnno != null) {
				String target = assAnno.target();

				if (!TextUtils.isEmpty(target)) {
					try {
						childClass = Class.forName(target);
						return childClass;
					} catch (ClassNotFoundException e) {
						throw new ClassCastException(
								"OneToAny target defined class not found:"
										+ target);
					}
				}
			}
		}

		return ORMUtil.findAssoTargetClass(mainClass, childFieldName);
	}

	private String genCreateTableSql(Class<?> clazz, ORMapInfo mapInfo) {
		StringBuffer sql = new StringBuffer();
		String tableName = mapInfo.getTableName();
		sql.append("create table if not exists ");
		sql.append(tableName);
		sql.append(LEFT_PARENTHESIS);
		String columnSql = buildCreateTableColumnSql(clazz, mapInfo, false);
		if (TextUtils.isEmpty(columnSql)) {
			return null;
		}
		sql.append(columnSql);
		sql.append(RIGHT_PARENTHESIS);
		return sql.toString();
	}

	// if has been joined in SQL
	private boolean hasJoined(QueryColumns queryColumns, Class<?> clazz) {
		if (queryColumns == null) {
			return false;
		}
		ORMapInfo mapInfo = orMapper.generateColumnMap(clazz, null);
		String tableName = mapInfo.getTableName();
		for (QueryColumn column : queryColumns) {
			if (tableName.equals(column.tableName)) {
				return true;
			}
		}
		return false;
	}
}
