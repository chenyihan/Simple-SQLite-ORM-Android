package org.cyy.fw.android.dborm;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.cyy.fw.android.dborm.ORMapInfo.AssociateInfo;

import android.text.TextUtils;
import android.util.Log;

/**
 * 
 * Mapped by java annotation<BR>
 * 
 * 
 * @author cyy
 * @version [V1.0, 2012-11-5]
 */
public class AnnotationORMapper extends AbstractORMapper {
	private static final String TAG = "AnnotationORMapper";

	@Override
	protected ORMapInfo createORMap(Class<?> clazz, String tableName) {
		ORMapInfo mapInfo = new ORMapInfo();
		mapInfo.setClazz(clazz);
		mapInfo.setTableName(tableName);
		Field[] fields = clazz.getDeclaredFields();
		Map<String, String> fieldColumnMap = mapInfo.getFieldColumnMap();
		if (fieldColumnMap == null) {
			fieldColumnMap = new HashMap<String, String>();
			mapInfo.setFieldColumnMap(fieldColumnMap);
		}
		Map<String, String> columnFieldMap = mapInfo.getColumnFieldMap();
		if (columnFieldMap == null) {
			columnFieldMap = new HashMap<String, String>();
			mapInfo.setColumnFieldMap(columnFieldMap);
		}
		// relation attribute
		List<Field> assFields = new ArrayList<Field>();
		for (Field f : fields) {
			PrimaryKey primaryKeyAnno = f.getAnnotation(PrimaryKey.class);
			// primary key
			if (primaryKeyAnno != null) {
				mapInfo.setPrimaryKeyField(f.getName());
				mapInfo.setPkSequence(primaryKeyAnno.sequence());
				mapInfo.setGenUIDBySelf(primaryKeyAnno.genUIDBySelf());
				String primaryKeyColumnName = primaryKeyAnno.columnName();
				// use attribute name as primary key name
				if (TextUtils.isEmpty(primaryKeyColumnName)) {
					primaryKeyColumnName = f.getName();
				}
				mapInfo.setPrimaryKeyColumn(primaryKeyColumnName);
				fieldColumnMap.put(f.getName(), primaryKeyColumnName);
				columnFieldMap.put(primaryKeyColumnName, f.getName());
			}
			Column columnAnno = f.getAnnotation(Column.class);
			if (columnAnno != null) {
				String columnName = columnAnno.name();
				// use attribute name as column name
				if (TextUtils.isEmpty(columnName)) {
					columnName = f.getName();
				}
				fieldColumnMap.put(f.getName(), columnName);
				columnFieldMap.put(columnName, f.getName());
			}
			OneToAny assAnno = f.getAnnotation(OneToAny.class);
			if (assAnno != null) {
				assFields.add(f);
			}
		}
		// process relation information
		if (!assFields.isEmpty()) {
			createAssoInfo(mapInfo, assFields);
		}
		return mapInfo;
	}

	@Override
	protected String[] obtainTableName(Class<?> clazz) {
		Table tableAnno = clazz.getAnnotation(Table.class);
		String[] tableNames = null;
		if (tableAnno != null) {
			tableNames = tableAnno.name();
		}

		tableNames = filtTables(tableNames);
		// use simple class name as table name
		if (ObjectUtil.isArrayEmpty(tableNames)) {
			tableNames = new String[] { clazz.getSimpleName() };
		}
		return tableNames;
	}

	private void createAssoInfo(ORMapInfo mapInfo, List<Field> assFields) {
		try {
			for (Field f : assFields) {
				Log.d(TAG, "createAssoInfo:" + f.getName());
				OneToAny assAnno = f.getAnnotation(OneToAny.class);
				String target = assAnno.target();
				Class<?> targetClass = null;
				// obtain by @OneToMany target first
				if (!TextUtils.isEmpty(target)) {
					targetClass = Class.forName(target);
				}
				if (targetClass == null) {
					Class<?> fieldType = f.getType();
					// Accord to the generic type then, only support Vector and
					// List so far
					if (fieldType.isAssignableFrom(List.class)
							|| fieldType.isAssignableFrom(Vector.class)) {
						ParameterizedType paraType = null;
						if (!(f.getGenericType() instanceof ParameterizedType)) {
							throw new POJOClassDefineException(
									"generic type was not specified:"
											+ f.getName());
						}

						paraType = (ParameterizedType) f.getGenericType();
						if (paraType == null) {
							throw new POJOClassDefineException(
									"@OneToMany has not enough info.");
						}

						Class<?> paraTypeClass = (Class<?>) paraType
								.getActualTypeArguments()[0];
						targetClass = paraTypeClass;
					}
					// Obtain the declare type of complex type attribute
					else if (!fieldType.isAssignableFrom(Collection.class)
							&& !fieldType.isArray()
							&& !fieldType.isAssignableFrom(Map.class)) {
						targetClass = fieldType;
					}
				}
				if (targetClass == null) {
					throw new POJOClassDefineException(
							"@OneToMany has not enough info.");
				}
				String targetField = assAnno.targetField();
				String sourceField = assAnno.sourceField();
				// use primary key as source relation attribute
				if (TextUtils.isEmpty(sourceField)) {
					sourceField = mapInfo.getPrimaryKeyField();
				}
				if (TextUtils.isEmpty(sourceField)) {
					throw new POJOClassDefineException(
							"@OneToMany hasn't sourceField attribute or the POJO hasn't PrimaryKey annotation.");
				}
				Map<Class<?>, AssociateInfo> assInfoMap = mapInfo
						.getAssociateInfo();
				AssociateInfo assInfo = assInfoMap.get(targetClass);
				if (assInfo == null) {
					assInfo = new AssociateInfo();
					assInfoMap.put(targetClass, assInfo);
				}
				assInfo.setSourceField(sourceField);
				assInfo.setTarget(targetClass);
				assInfo.setTargetField(targetField);
				assInfo.setField(f.getName());
			}
		} catch (ClassNotFoundException e) {
			throw new POJOClassDefineException(
					"The target of @OneToMany does not exist.", e);
		}
	}

	private String[] filtTables(String[] tableNames) {
		if (tableNames == null) {
			return null;
		}
		List<String> list = new ArrayList<String>();
		for (String name : tableNames) {
			if (TextUtils.isEmpty(name)) {
				continue;
			}
			if (list.contains(name)) {
				continue;
			}
			list.add(name);
		}
		return list.toArray(new String[list.size()]);
	}
}
