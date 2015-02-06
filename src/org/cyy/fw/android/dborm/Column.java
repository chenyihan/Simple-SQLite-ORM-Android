package org.cyy.fw.android.dborm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * Map to DB column<BR>
 * This annotation acts on class field
 * 
 * @author cyy
 * @version [V1.0, 2012-11-5]
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {

	/**
	 * 
	 * DB column name, the DB column will be the same with java class field name
	 * if you haven't set this value<BR>
	 * 
	 */
	public String name() default "";
}
