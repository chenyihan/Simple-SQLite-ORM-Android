package org.cyy.fw.android.dborm.sqlite.test;

import java.util.ArrayList;
import java.util.List;

import org.cyy.fw.android.dborm.Column;
import org.cyy.fw.android.dborm.OneToAny;
import org.cyy.fw.android.dborm.PrimaryKey;
import org.cyy.fw.android.dborm.SQLObject;
import org.cyy.fw.android.dborm.Table;
import org.cyy.fw.android.dborm.sqlite.QueryAidParameter;
import org.cyy.fw.android.dborm.sqlite.SqliteDBFacade;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

public class SqliteORMTester extends AndroidTestCase {
	public static class TestDBFacade extends SqliteDBFacade {

		public TestDBFacade(Context context, String name, int version) {
			super(context, name, version);
		}

		@Override
		protected Class<?>[] configureORMClasses() {
			return new Class<?>[] { T1.class, T2.class, T21.class, T22.class,
					T211.class, T212.class };
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			super.onUpgrade(db, oldVersion, newVersion);
			// this.onCreate(db);
		}

	}

	@Table
	public static class T1 {

		@Column
		private String strAttr;
		@Column
		private Integer intAttr;
		@Column
		private Float floatAttr;
		@Column
		private Long longAttr;
		@Column
		private Double doubleAttr;
		@Column
		private Short shortAttr;
		@Column
		private Boolean boolAttr;

		private String otherAttr;

		public String getStrAttr() {
			return strAttr;
		}

		public void setStrAttr(String strAttr) {
			this.strAttr = strAttr;
		}

		public Integer getIntAttr() {
			return intAttr;
		}

		public void setIntAttr(Integer intAttr) {
			this.intAttr = intAttr;
		}

		public Float getFloatAttr() {
			return floatAttr;
		}

		public void setFloatAttr(Float floatAttr) {
			this.floatAttr = floatAttr;
		}

		public Long getLongAttr() {
			return longAttr;
		}

		public void setLongAttr(Long longAttr) {
			this.longAttr = longAttr;
		}

		public Double getDoubleAttr() {
			return doubleAttr;
		}

		public void setDoubleAttr(Double doubleAttr) {
			this.doubleAttr = doubleAttr;
		}

		public Short getShortAttr() {
			return shortAttr;
		}

		public void setShortAttr(Short shortAttr) {
			this.shortAttr = shortAttr;
		}

		public Boolean getBoolAttr() {
			return boolAttr;
		}

		public void setBoolAttr(Boolean boolAttr) {
			this.boolAttr = boolAttr;
		}

		public String getOtherAttr() {
			return otherAttr;
		}

		public void setOtherAttr(String otherAttr) {
			this.otherAttr = otherAttr;
		}

	}

	@Table(name = "t2")
	public static class T2 {
		@PrimaryKey(genUIDBySelf = true)
		private String id;
		@Column
		private String name;
		@Column
		private String alias;

		@OneToAny(targetField = "pid")
		private List<T21> t21Children;

		@OneToAny(targetField = "pid")
		private T22 t22;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getAlias() {
			return alias;
		}

		public void setAlias(String alias) {
			this.alias = alias;
		}

		public List<T21> getT21Children() {
			return t21Children;
		}

		public void setT21Children(List<T21> t21Children) {
			this.t21Children = t21Children;
		}

		public T22 getT22() {
			return t22;
		}

		public void setT22(T22 t22) {
			this.t22 = t22;
		}

	}

	@Table
	public static class T21 {
		@PrimaryKey(genUIDBySelf = true)
		private String id;
		@Column
		private String name;
		@Column
		private String pid;

		@OneToAny(targetField = "pid")
		private List<T211> t211Children;

		@OneToAny(targetField = "pid")
		private T212 t212;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getPid() {
			return pid;
		}

		public void setPid(String pid) {
			this.pid = pid;
		}

		public List<T211> getT211Children() {
			return t211Children;
		}

		public void setT211Children(List<T211> t211Children) {
			this.t211Children = t211Children;
		}

		public T212 getT212() {
			return t212;
		}

		public void setT212(T212 t212) {
			this.t212 = t212;
		}

	}

	@Table
	public static class T22 {
		@PrimaryKey(genUIDBySelf = true)
		private String id22;
		@Column
		private String name;
		@Column
		private String pid;

		public String getId22() {
			return id22;
		}

		public void setId22(String id22) {
			this.id22 = id22;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getPid() {
			return pid;
		}

		public void setPid(String pid) {
			this.pid = pid;
		}

	}

	@Table
	public static class T211 {

		@PrimaryKey(genUIDBySelf = true)
		private String id;
		@Column
		private String name;
		@Column
		private String pid;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getPid() {
			return pid;
		}

		public void setPid(String pid) {
			this.pid = pid;
		}

	}

	@Table
	public static class T212 {

		@PrimaryKey(genUIDBySelf = true)
		private String id212;
		@Column
		private String name;
		@Column
		private String pid;

		public String getId212() {
			return id212;
		}

		public void setId212(String id212) {
			this.id212 = id212;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getPid() {
			return pid;
		}

		public void setPid(String pid) {
			this.pid = pid;
		}

	}

	public static final String TEST_DB_NAME = "test.db";
	public static final int DB_VER = 1;
	private SqliteDBFacade facade;

	private SqliteDBFacade getDBFacade() {
		if (facade == null) {

			facade = new TestDBFacade(getContext(), TEST_DB_NAME, DB_VER);
		}
		return facade;
	}

	public void test1() {
		SqliteDBFacade facade = getDBFacade();

		// clear data
		facade.delete(T1.class, null);

		// insert data
		T1 t = new T1();
		t.setStrAttr("strValue");
		t.setIntAttr(100);
		t.setFloatAttr(101.22f);
		t.setLongAttr(Long.MAX_VALUE);
		t.setDoubleAttr(100000000000000000000000.2222333333d);
		Short shortValue = 2;
		t.setShortAttr(shortValue);
		t.setBoolAttr(true);
		t.setOtherAttr("other");
		facade.insert(t);

		// query and check data
		T1[] datasInDB = facade.query(T1.class, null);
		assertNotNull(datasInDB);
		assertTrue(datasInDB.length == 1);

		T1 tInDB = datasInDB[0];
		assertNotNull(tInDB);
		assertEquals(t.getStrAttr(), tInDB.getStrAttr());
		assertEquals(t.getIntAttr(), tInDB.getIntAttr());
		assertEquals(t.getFloatAttr(), tInDB.getFloatAttr());
		assertEquals(t.getLongAttr(), tInDB.getLongAttr());
		assertEquals(t.getDoubleAttr(), tInDB.getDoubleAttr());
		assertEquals(t.getShortAttr(), tInDB.getShortAttr());
		assertEquals(t.getBoolAttr(), tInDB.getBoolAttr());
		assertNull(tInDB.getOtherAttr());

	}

	public void test2() {
		SqliteDBFacade facade = getDBFacade();

		// clear data
		facade.delete(T2.class, null);

		// insert data
		T2 t2 = new T2();
		facade.insert(t2);

		// query and check data
		T2[] datasInDB = facade.query(T2.class, null);
		assertNotNull(datasInDB);
		assertTrue(datasInDB.length == 1);

		T2 tInDB = datasInDB[0];
		assertNotNull(tInDB);
		assertNotNull(tInDB.getId());
	}

	public void test3() {
		SqliteDBFacade facade = getDBFacade();
		facade.delete(T2.class, null);

		// insert data
		T2 t2 = new T2();
		facade.insert(t2);

		// clear data
		facade.delete(T2.class, null);
		// query and check data
		T2[] datasInDB = facade.query(T2.class, null);

		assertNotNull(datasInDB);
		assertTrue(datasInDB.length == 0);

	}

	public void test4() {
		SqliteDBFacade facade = getDBFacade();

		// clear data
		facade.delete(T1.class, null);

		// insert data
		T1 t = new T1();
		t.setStrAttr("strValue");
		t.setIntAttr(100);
		t.setFloatAttr(101.22f);
		t.setLongAttr(Long.MAX_VALUE);
		t.setDoubleAttr(100000000000000000000000.2222333333d);
		Short shortValue = 2;
		t.setShortAttr(shortValue);
		t.setBoolAttr(true);
		t.setOtherAttr("other");
		facade.insert(t);

		T1 updT = new T1();
		updT.setStrAttr("strValue1");
		updT.setIntAttr(101);
		updT.setFloatAttr(102.33f);
		updT.setLongAttr(Long.MAX_VALUE - 100);
		updT.setDoubleAttr(102.454545455545454545454d);
		Short shortValue1 = 3;
		updT.setShortAttr(shortValue1);
		updT.setBoolAttr(false);
		updT.setOtherAttr("other1");

		facade.update(updT, null);

		// query and check data
		T1[] datasInDB = facade.query(T1.class, null);
		assertNotNull(datasInDB);
		assertTrue(datasInDB.length == 1);

		T1 tInDB = datasInDB[0];
		assertNotNull(tInDB);
		assertEquals(updT.getStrAttr(), tInDB.getStrAttr());
		assertEquals(updT.getIntAttr(), tInDB.getIntAttr());
		assertEquals(updT.getFloatAttr(), tInDB.getFloatAttr());
		assertEquals(updT.getLongAttr(), tInDB.getLongAttr());
		assertEquals(updT.getDoubleAttr(), tInDB.getDoubleAttr());
		assertEquals(updT.getShortAttr(), tInDB.getShortAttr());
		assertEquals(updT.getBoolAttr(), tInDB.getBoolAttr());
		assertNull(tInDB.getOtherAttr());

		updT = new T1();
		updT.setStrAttr("strValue2");
		updT.setIntAttr(102);
		updT.setFloatAttr(1022.33f);
		updT.setLongAttr(Long.MAX_VALUE - 200);
		updT.setDoubleAttr(102.454545455444545454545454d);
		Short shortValue2 = 4;
		updT.setShortAttr(shortValue2);
		updT.setBoolAttr(true);
		updT.setOtherAttr("other2");

		T1 condition = new T1();
		condition.setIntAttr(100);
		int rows = facade.update(updT, condition);
		assertEquals(0, rows);

		condition = new T1();
		condition.setIntAttr(101);
		condition.setStrAttr("strValue1");
		rows = facade.update(updT, condition);
		assertEquals(1, rows);

		datasInDB = facade.query(T1.class, null);
		assertNotNull(datasInDB);
		assertTrue(datasInDB.length == 1);

		tInDB = datasInDB[0];
		assertNotNull(tInDB);
		assertEquals(updT.getStrAttr(), tInDB.getStrAttr());
		assertEquals(updT.getIntAttr(), tInDB.getIntAttr());
		assertEquals(updT.getFloatAttr(), tInDB.getFloatAttr());
		assertEquals(updT.getLongAttr(), tInDB.getLongAttr());
		assertEquals(updT.getDoubleAttr(), tInDB.getDoubleAttr());
		assertEquals(updT.getShortAttr(), tInDB.getShortAttr());
		assertEquals(updT.getBoolAttr(), tInDB.getBoolAttr());
		assertNull(tInDB.getOtherAttr());
	}

	public void test5() {
		SqliteDBFacade facade = getDBFacade();
		facade.delete(T2.class, null);

		// insert data
		T2 t2 = new T2();
		t2.setName("cyy");
		t2.setAlias("duoduo");
		facade.insert(t2);

		T2[] datasInDB = facade.query(T2.class, null);
		assertNotNull(datasInDB);
		assertTrue(datasInDB.length == 1);

		T2 updT = datasInDB[0];
		updT.setName("cyh");

		facade.updateByPk(updT);

		datasInDB = facade.query(T2.class, null);
		assertNotNull(datasInDB);
		assertTrue(datasInDB.length == 1);

		T2 tInDB = datasInDB[0];
		String id = tInDB.getId();

		assertNotNull(id);
		assertEquals(updT.getName(), tInDB.getName());
		assertEquals(t2.getAlias(), tInDB.getAlias());

		facade.updateByPks(new T2[] { updT }, new String[] { "alias" });
		datasInDB = facade.query(T2.class, null);
		assertNotNull(datasInDB);
		assertTrue(datasInDB.length == 1);
		tInDB = datasInDB[0];

		assertEquals(updT.getName(), tInDB.getName());
		assertEquals(updT.getAlias(), tInDB.getAlias());

	}

	public void test6() {

		SqliteDBFacade facade = getDBFacade();
		facade.delete(T2.class, null);
		// insert data
		String id = "id";
		T2 t2 = new T2();
		t2.setId(id);
		t2.setName("wx");
		t2.setAlias("lp");
		facade.insert(t2);

		T2 tInDB = facade.queryByPk(T2.class, id);
		assertNotNull(tInDB);
		assertEquals(t2.getId(), tInDB.getId());
		assertEquals(t2.getName(), tInDB.getName());
		assertEquals(t2.getAlias(), tInDB.getAlias());

		// clear data
		facade.deleteByPk(T2.class, id);
		// query and check data
		tInDB = facade.queryByPk(T2.class, id);

		assertNull(tInDB);

	}

	public void test7() {
		SqliteDBFacade facade = getDBFacade();
		facade.delete(T2.class, null);
		// insert data
		String id = "id";
		T2 t2 = new T2();
		t2.setId(id);
		t2.setName("wx");
		t2.setAlias("lp");
		facade.insert(t2);

		QueryAidParameter p = new QueryAidParameter();
		p.setExtraWherePart("name=?");
		p.setExtraArgValues(new String[] { "wx" });

		T2[] datasInDB = facade.query(T2.class, null, null, p);
		assertNotNull(datasInDB);
		assertTrue(datasInDB.length == 1);

		T2 tInDB = datasInDB[0];
		assertNotNull(tInDB);
		assertEquals(t2.getId(), tInDB.getId());
		assertEquals(t2.getName(), tInDB.getName());
		assertEquals(t2.getAlias(), tInDB.getAlias());

		T2 condition = new T2();
		condition.setName("wx1");
		int rows = facade.delete(T2.class, condition);
		assertEquals(0, rows);
		datasInDB = facade.query(T2.class, null, null, p);
		assertNotNull(datasInDB);
		assertEquals(1, datasInDB.length);

		condition = new T2();
		condition.setName("wx");
		rows = facade.delete(T2.class, condition);
		assertEquals(1, rows);
		datasInDB = facade.query(T2.class, null, null, p);
		assertNotNull(datasInDB);
		assertEquals(0, datasInDB.length);

	}

	public void test8() {
		SqliteDBFacade facade = getDBFacade();
		facade.delete(T2.class, null);
		// insert data
		String id = "id";
		T2 t2 = new T2();
		t2.setId(id);
		t2.setName("wx");
		t2.setAlias("lp");
		facade.insert(t2);
		QueryAidParameter p = new QueryAidParameter();
		p.setExtraWherePart("name=?");
		p.setExtraArgValues(new String[] { "wx" });
		p.setGroupBy("name");
		p.setOrderBy("name");
		p.setLimit("10");

		T2[] datasInDB = facade.query(T2.class, null, null, p);
		assertNotNull(datasInDB);
		assertTrue(datasInDB.length == 1);
	}

	public void test9() {

		SqliteDBFacade facade = getDBFacade();
		facade.delete(T2.class, null);
		// insert data
		String id = "id";
		T2 t2 = new T2();
		t2.setId(id);
		t2.setName("wx");
		t2.setAlias("lp");
		facade.insert(t2);
		QueryAidParameter p = new QueryAidParameter();
		p.setDistinct(true);
		p.setExtraWherePart("name=?");
		p.setExtraArgValues(new String[] { "wx1" });
		p.setGroupBy("name");
		p.setOrderBy("name");
		p.setLimit("10");

		T2[] datasInDB = facade.query(T2.class, null, null, p);
		assertNotNull(datasInDB);
		assertTrue(datasInDB.length == 0);

	}

	public void test10() {
		SqliteDBFacade facade = getDBFacade();
		facade.delete(T2.class, null);
		// insert data
		String id = "id";
		T2 t2 = new T2();
		t2.setId(id);
		t2.setName("wx");
		t2.setAlias("lp");
		facade.insert(t2);
		String sql = "select * from t2 where name = ? group by name order by name limit 10";
		Cursor cursor = facade.rawQuery(sql, new String[] { "wx" });
		assertNotNull(cursor);
		assertTrue(cursor.getCount() > 0);
		cursor.close();
	}

	public void test11() {
		SqliteDBFacade facade = getDBFacade();
		facade.delete(T2.class, null);

		T2 t2 = new T2();
		t2.setId("id");
		t2.setName("wx");
		t2.setAlias("lp");

		T2 t21 = new T2();
		t21.setId("id1");
		t21.setName("wx1");
		t21.setAlias("lp1");

		long[] rows = facade.insert(new T2[] { t2, t21 });
		assertNotNull(rows);
		assertEquals(2, rows.length);

		T2[] datasInDB = facade.query(T2.class, null);
		assertNotNull(datasInDB);
		assertEquals(2, datasInDB.length);
		assertEquals(t2.getId(), datasInDB[0].getId());
		assertEquals(t21.getId(), datasInDB[1].getId());

		int row = facade.deleteByPk(T2.class, "id");
		assertEquals(1, row);

		T2 t2InDB = facade.queryByPk(T2.class, "id");
		assertNull(t2InDB);

		T2 t21InDB = facade.queryByPk(T2.class, "id1");
		assertNotNull(t21InDB);
		assertEquals(t21.getId(), t21InDB.getId());
		assertEquals(t21.getName(), t21InDB.getName());
		assertEquals(t21.getAlias(), t21InDB.getAlias());

		t2 = new T2();
		t2.setId("id2");
		t2.setName("wx2");
		t2.setAlias("lp2");
		facade.insert(t2);

		datasInDB = facade.query(T2.class, null);
		assertNotNull(datasInDB);
		assertEquals(2, datasInDB.length);
		assertEquals(t21.getId(), datasInDB[0].getId());
		assertEquals(t2.getId(), datasInDB[1].getId());

		datasInDB = facade.queryByPks(T2.class, new String[] { "id2", "id1" });
		assertNotNull(datasInDB);
		assertEquals(2, datasInDB.length);
		assertEquals(t21.getId(), datasInDB[0].getId());
		assertEquals(t2.getId(), datasInDB[1].getId());

		datasInDB = facade.queryByPks(T2.class, new String[] { "id2", "id1",
				"id3" });
		assertNotNull(datasInDB);
		assertEquals(2, datasInDB.length);
		assertEquals(t21.getId(), datasInDB[0].getId());
		assertEquals(t2.getId(), datasInDB[1].getId());

		T2 condition = new T2();
		condition.setName("wx2");
		datasInDB = facade.query(T2.class, condition);
		assertNotNull(datasInDB);
		assertEquals(1, datasInDB.length);
		assertEquals("wx2", datasInDB[0].getName());

		condition = new T2();
		condition.setName("wx3");
		datasInDB = facade.query(T2.class, condition);
		assertNotNull(datasInDB);
		assertEquals(0, datasInDB.length);

		facade.deleteByPks(T2.class, new String[] { "id2", "id1" });
		datasInDB = facade.query(T2.class, null);
		assertNotNull(datasInDB);
		assertEquals(0, datasInDB.length);
	}

	public void test12() {
		SqliteDBFacade facade = getDBFacade();
		facade.delete(T211.class, null);
		facade.delete(T21.class, null);
		facade.delete(T2.class, null);

		T2 t2 = new T2();
		t2.setName("name1");
		t2.setAlias("alias2");

		List<T21> t21list = new ArrayList<SqliteORMTester.T21>();
		T21 t21 = new T21();
		t21.setName("name21");
		t21list.add(t21);

		List<T211> t211list = new ArrayList<SqliteORMTester.T211>();
		T211 t211 = new T211();
		t211.setName("name211");
		t211list.add(t211);

		t21.setT211Children(t211list);
		t2.setT21Children(t21list);

		facade.insertCascade(new T2[] { t2 }, new Class<?>[] { T21.class,
				T211.class });

		T2[] t2InDB = facade.queryCascadeByCondition(T2.class, null,
				new String[] { "t21Children.t211Children" });
		assertNotNull(t2InDB);
		assertEquals(1, t2InDB.length);

		assertNotNull(t2InDB[0]);
		assertNotNull(t2InDB[0].getT21Children());
		assertEquals(1, t2InDB[0].getT21Children().size());

		T21 t21InDB = t2InDB[0].getT21Children().get(0);
		assertNotNull(t21InDB);
		assertEquals(t21.getName(), t21InDB.getName());
		assertEquals(t2InDB[0].getId(), t21InDB.getPid());

		assertNotNull(t21InDB.getT211Children());

		assertEquals(1, t21InDB.getT211Children().size());
		T211 t211InDB = t21InDB.getT211Children().get(0);
		assertNotNull(t211InDB);
		assertEquals(t211.getName(), t211InDB.getName());
		assertEquals(t21InDB.getId(), t211InDB.getPid());

	}

	public void test13() {
		SqliteDBFacade facade = getDBFacade();
		facade.delete(T211.class, null);
		facade.delete(T21.class, null);
		facade.delete(T2.class, null);

		T2 t2 = new T2();
		t2.setName("name1");
		t2.setAlias("alias2");

		List<T21> t21list = new ArrayList<SqliteORMTester.T21>();
		T21 t21 = new T21();
		t21.setName("name21");
		t21list.add(t21);

		List<T211> t211list = new ArrayList<SqliteORMTester.T211>();
		T211 t211 = new T211();
		t211.setName("name211");
		t211list.add(t211);

		T211 t212 = new T211();
		t211.setName("name212");
		t211list.add(t212);

		t21.setT211Children(t211list);
		t2.setT21Children(t21list);

		facade.insertCascade(new T2[] { t2 }, new Class<?>[] { T21.class,
				T211.class });

		T2[] t2InDB = facade.queryCascadeByCondition(T2.class, null,
				new String[] { "t21Children.t211Children" });
		assertNotNull(t2InDB);
		assertEquals(1, t2InDB.length);

		assertNotNull(t2InDB[0]);
		assertNotNull(t2InDB[0].getT21Children());
		assertEquals(1, t2InDB[0].getT21Children().size());

		T21 t21InDB = t2InDB[0].getT21Children().get(0);
		assertNotNull(t21InDB);
		assertEquals(t21.getName(), t21InDB.getName());
		assertEquals(t2InDB[0].getId(), t21InDB.getPid());

		assertNotNull(t21InDB.getT211Children());

		assertEquals(2, t21InDB.getT211Children().size());
		T211 t211InDB = t21InDB.getT211Children().get(0);
		assertNotNull(t211InDB);
		assertEquals(t211.getName(), t211InDB.getName());
		assertEquals(t21InDB.getId(), t211InDB.getPid());

		t211InDB = t21InDB.getT211Children().get(1);
		assertNotNull(t211InDB);
		assertEquals(t212.getName(), t211InDB.getName());
		assertEquals(t21InDB.getId(), t211InDB.getPid());

	}

	public void test14() {
		SqliteDBFacade facade = getDBFacade();
		facade.delete(T211.class, null);
		facade.delete(T22.class, null);
		facade.delete(T21.class, null);
		facade.delete(T2.class, null);

		T2 t2 = new T2();
		t2.setName("name1");
		t2.setAlias("alias2");

		List<T21> t21list = new ArrayList<SqliteORMTester.T21>();
		T21 t21 = new T21();
		t21.setName("name21");
		t21list.add(t21);

		List<T211> t211list = new ArrayList<SqliteORMTester.T211>();
		T211 t211 = new T211();
		t211.setName("name211");
		t211list.add(t211);

		T211 t212 = new T211();
		t211.setName("name212");
		t211list.add(t212);

		t21.setT211Children(t211list);
		t2.setT21Children(t21list);

		SQLObject[] sqlObjs = facade.buildCascadeInsertSqls(new T2[] { t2 },
				new Class<?>[] { T21.class, T211.class }, null);
		facade.execSQL(sqlObjs);

		T2[] t2InDB = facade.queryCascadeByCondition(T2.class, null,
				new String[] { "t21Children.t211Children" });
		assertNotNull(t2InDB);
		assertEquals(1, t2InDB.length);

		assertNotNull(t2InDB[0]);
		assertNotNull(t2InDB[0].getT21Children());
		assertEquals(1, t2InDB[0].getT21Children().size());

		T21 t21InDB = t2InDB[0].getT21Children().get(0);
		assertNotNull(t21InDB);
		assertEquals(t21.getName(), t21InDB.getName());
		assertEquals(t2InDB[0].getId(), t21InDB.getPid());

		assertNotNull(t21InDB.getT211Children());

		assertEquals(2, t21InDB.getT211Children().size());
		T211 t211InDB = t21InDB.getT211Children().get(0);
		assertNotNull(t211InDB);
		assertEquals(t211.getName(), t211InDB.getName());
		assertEquals(t21InDB.getId(), t211InDB.getPid());

		t211InDB = t21InDB.getT211Children().get(1);
		assertNotNull(t211InDB);
		assertEquals(t212.getName(), t211InDB.getName());
		assertEquals(t21InDB.getId(), t211InDB.getPid());

	}

	public void test15() {
		SqliteDBFacade facade = getDBFacade();
		facade.delete(T211.class, null);
		facade.delete(T212.class, null);
		facade.delete(T21.class, null);
		facade.delete(T22.class, null);
		facade.delete(T2.class, null);

		T2 t2 = new T2();
		t2.setName("name1");
		t2.setAlias("alias2");

		List<T21> t21list = new ArrayList<SqliteORMTester.T21>();
		T21 t21 = new T21();
		t21.setName("name21");
		t21list.add(t21);

		List<T211> t211list = new ArrayList<SqliteORMTester.T211>();
		T211 t211 = new T211();
		t211.setName("name211");
		t211list.add(t211);

		T211 t211_1 = new T211();
		t211.setName("name211_1");
		t211list.add(t211_1);

		T212 t212 = new T212();
		t212.setName("name212");
		t21.setT212(t212);

		t21.setT211Children(t211list);
		t2.setT21Children(t21list);

		T22 t22 = new T22();
		t22.setName("name22");
		t2.setT22(t22);

		SQLObject[] sqlObjs = facade
				.buildCascadeInsertSqls(new T2[] { t2 }, new Class<?>[] {
						T21.class, T22.class, T211.class, T212.class }, null);
		facade.execSQL(sqlObjs);

		T2[] t2InDB = facade.queryCascadeByCondition(T2.class, null,
				new String[] { "t21Children.t211Children", "t22",
						"t21Children.t212" });
		assertNotNull(t2InDB);
		assertEquals(1, t2InDB.length);

		assertNotNull(t2InDB[0]);
		assertNotNull(t2InDB[0].getT21Children());
		assertEquals(1, t2InDB[0].getT21Children().size());

		T21 t21InDB = t2InDB[0].getT21Children().get(0);
		assertNotNull(t21InDB);
		assertEquals(t21.getName(), t21InDB.getName());
		assertEquals(t2InDB[0].getId(), t21InDB.getPid());

		assertNotNull(t21InDB.getT211Children());

		assertEquals(2, t21InDB.getT211Children().size());
		T211 t211InDB = t21InDB.getT211Children().get(0);
		assertNotNull(t211InDB);
		assertEquals(t211.getName(), t211InDB.getName());
		assertEquals(t21InDB.getId(), t211InDB.getPid());

		t211InDB = t21InDB.getT211Children().get(1);
		assertNotNull(t211InDB);
		assertEquals(t211_1.getName(), t211InDB.getName());
		assertEquals(t21InDB.getId(), t211InDB.getPid());

		T212 t212InDB = t21InDB.getT212();
		assertNotNull(t212InDB);
		assertNotNull(t212InDB.getId212());
		assertEquals(t212.getName(), t212InDB.getName());

		T22 t22InDB = t2InDB[0].getT22();
		assertNotNull(t22InDB);
		assertNotNull(t22InDB.getId22());
		assertEquals(t22.getName(), t22InDB.getName());

	}
}
