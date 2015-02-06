package org.cyy.fw.android.dborm;

import java.util.UUID;

/**
 * UUID generator
 * 
 * @author d00207889
 * @version [RCS Client V100R001C03, 2014-7-1]
 */
public class UUIDGenerator implements UniqueIDGenerator {

	@Override
	public Object generateUID() {
		UUID uuid = UUID.randomUUID();
		return uuid.toString();
	}

}
