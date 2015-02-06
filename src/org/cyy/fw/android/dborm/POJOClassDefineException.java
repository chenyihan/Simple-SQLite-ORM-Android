package org.cyy.fw.android.dborm;

/**
 * 
 * POJO defined exception<BR>
 * It describes not comply with the POJO specifications of the framework, such
 * as the setter and getter specifications
 * 
 * @author cyy
 * @version [V1.0, 2012-11-5]
 */
public class POJOClassDefineException extends RuntimeException {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 659144459683878606L;

	public POJOClassDefineException() {
		super();
	}

	public POJOClassDefineException(String detailMessage) {
		super(detailMessage);
	}

	public POJOClassDefineException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public POJOClassDefineException(Throwable throwable) {
		super(throwable);
	}

}
