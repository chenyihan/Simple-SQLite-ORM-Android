package org.cyy.fw.android.dborm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * Annotation for table acts on class<BR>
 * 
 * @author cyy
 * @version [V1.0, 2012-11-5]
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Table {

	/**
	 * 
	 * table name<BR>
	 * The framework will use the simple class name for table name if this
	 * attribute is not be specified. It can be set more than one names, such as
	 * name={"a","b" }, means that the class will mapping table "a" and "b"
	 */
	public String[] name() default "";
}
