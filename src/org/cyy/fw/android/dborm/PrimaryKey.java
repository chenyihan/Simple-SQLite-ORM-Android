package org.cyy.fw.android.dborm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * Primary key annotation<BR>
 * Acts on attributes
 * 
 * @author cyy
 * @version [V1.0, 2012-11-5]
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PrimaryKey {
	/**
	 * 
	 * Name of primary key<BR>
	 * 
	 */
	public String columnName() default "";

	/**
	 * 
	 * If it's set true, the framework will generate primary key for POJO whose
	 * primary key's value is null<BR>
	 */
	public boolean genUIDBySelf() default false;

	/**
	 * 
	 * The column will be auto-increment if it's set true<BR>
	 * 
	 * @deprecated It's suggested to use {@link UniqueIDGenerator}
	 * 
	 */
	public boolean sequence() default false;
}
