/**
 * Provide the SQLite ORM scheme and DB Access API, Expose the {@link org.cyy.fw.android.dborm.sqlite.SqliteDBFacade} for users<br>
 * User need to extend it and do a few things more:
 * (1).Name the DB, android will create the specified DB file in the databases folder of the application
 * (2).Implement the configureORMClasses() method, configure all mapped POJO classes, the DB will create tables according to this configured classes 
 * 
 */
package org.cyy.fw.android.dborm.sqlite;