package org.cyy.fw.android.dborm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * One to one or one to many relation<BR>
 * 
 * @author cyy
 * @version [V1.0, 2012-11-17]
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface OneToAny {
	/**
	 * 
	 * Relation source field name, it must be POJO field name<BR>
	 * 
	 */
	public String sourceField() default "";

	/**
	 * 
	 * Relation target class, the class full name<BR>
	 * 
	 */
	public String target() default "";

	/**
	 * 
	 * Relation target field name, it must be POJO field name<BR>
	 */
	public String targetField();
}
