package org.cyy.fw.android.dborm;

/**
 * 
 * Access database exception<BR>
 * 
 * @author cyy
 * @version [V1.0, 2012-11-5]
 */
public class DBAccessException extends RuntimeException {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -1042916009553988694L;

	public DBAccessException() {
		super();
	}

	public DBAccessException(String detailMessage) {
		super(detailMessage);
	}

	public DBAccessException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public DBAccessException(Throwable throwable) {
		super(throwable);
	}

}
