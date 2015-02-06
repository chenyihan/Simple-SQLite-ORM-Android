package org.cyy.fw.android.dborm;

/**
 * UID generator<BR>
 * 
 * @author d00207889
 * @version [RCS Client V100R001C03, 2014-7-1]
 */
public interface UniqueIDGenerator {

	/**
	 * Generate UID
	 * 
	 * @return UID
	 */
	Object generateUID();
}
