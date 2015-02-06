package org.cyy.fw.android.dborm;

/**
 * 
 * O-R mapping tool<BR>
 * 
 * @author cyy
 * @version [V1.0, 2012-11-5]
 * @see AnnotationORMapper
 * @see DefaultORMapper
 */
public interface ORMapper {
	/**
	 * 
	 * generate all the O-R mapping information<BR>
	 * One POJO class may be mapped to several tables
	 * 
	 * @param clazz
	 *            POJO class
	 * @return mapping info
	 */
	ORMapInfo[] generateAllTableColumnMap(Class<?> clazz);

	/**
	 * 
	 * generate one O-R mapping object, if the class class is mapped to more
	 * than one tables, return the first table<BR>
	 * 
	 * @param clazz
	 *            POJO class
	 * @deprecated It's suggested to use
	 *             {@link #generateColumnMap(Class, String)} instead
	 * @return mapping info
	 */
	ORMapInfo generateColumnMap(Class<?> clazz);

	/**
	 * 
	 * generate mapping info of specified table<BR>
	 * 
	 * @param clazz
	 *            POJO class
	 * @param tableName
	 *            The specified table name, the first table's info will be
	 *            returned if this parameter is null
	 * @return mapping info
	 */
	ORMapInfo generateColumnMap(Class<?> clazz, String tableName);
}
