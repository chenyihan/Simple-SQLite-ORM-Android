package org.cyy.fw.android.dborm.sqlite.test;

import org.cyy.fw.android.dborm.Column;
import org.cyy.fw.android.dborm.PrimaryKey;
import org.cyy.fw.android.dborm.Table;
import org.cyy.fw.android.dborm.sqlite.DBAutoUpgradeAdapter;
import org.cyy.fw.android.dborm.sqlite.SqliteDBFacade;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.test.AndroidTestCase;

public class DBUpgradeTester extends AndroidTestCase
{
    @Table(name = {"t33", "t34"})
    public static class Table33
    {
        @Column
        private String id33;
        
        @Column
        private String name33;
        
        public String getId33()
        {
            return id33;
        }
        
        public void setId33(String id33)
        {
            this.id33 = id33;
        }
        
        public String getName33()
        {
            return name33;
        }
        
        public void setName33(String name33)
        {
            this.name33 = name33;
        }
        
    }
    
    @Table(name = {"t22", "t23", "t24"})
    public static class Table22
    {
        @Column
        private String id22;
        
        @Column
        private String name22;
        
        @Column
        private String name23;
        
        public String getId22()
        {
            return id22;
        }
        
        public void setId22(String id22)
        {
            this.id22 = id22;
        }
        
        public String getName22()
        {
            return name22;
        }
        
        public void setName22(String name22)
        {
            this.name22 = name22;
        }
        
        public String getName23()
        {
            return name23;
        }
        
        public void setName23(String name23)
        {
            this.name23 = name23;
        }
        
    }
    
    @Table(name = {"t1", "t2", "t3", "t4"})
    public static class Table1
    {
        @PrimaryKey
        private String id;
        
        @Column
        private String name;
        
        @Column(name = "namec11")
        private String name11;
        
        @Column
        private String name12;
        
        @Column
        private String name13;
        
        @Column
        private String name14;
        
        @Column
        private String name15;
        
        @Column
        private String name16;
        
        @Column
        private String name17;
        
        @Column
        private String name18;
        
        public String getId()
        {
            return id;
        }
        
        public void setId(String id)
        {
            this.id = id;
        }
        
        public String getName()
        {
            return name;
        }
        
        public void setName(String name)
        {
            this.name = name;
        }
        
        public String getName11()
        {
            return name11;
        }
        
        public void setName11(String name11)
        {
            this.name11 = name11;
        }
        
        public String getName12()
        {
            return name12;
        }
        
        public void setName12(String name12)
        {
            this.name12 = name12;
        }
        
        public String getName13()
        {
            return name13;
        }
        
        public void setName13(String name13)
        {
            this.name13 = name13;
        }
        
        public String getName14()
        {
            return name14;
        }
        
        public void setName14(String name14)
        {
            this.name14 = name14;
        }
        
        public String getName15()
        {
            return name15;
        }
        
        public void setName15(String name15)
        {
            this.name15 = name15;
        }
        
        public String getName16()
        {
            return name16;
        }
        
        public void setName16(String name16)
        {
            this.name16 = name16;
        }
        
        public String getName17()
        {
            return name17;
        }
        
        public void setName17(String name17)
        {
            this.name17 = name17;
        }
        
        public String getName18()
        {
            return name18;
        }
        
        public void setName18(String name18)
        {
            this.name18 = name18;
        }
        
    }
    
    private static class TestDBFacade extends SqliteDBFacade implements
        DBAutoUpgradeAdapter
    {
        
        public TestDBFacade(Context context, String name,
            CursorFactory factory, int version)
        {
            super(context, name, factory, version);
        }
        
        @Override
        protected Class<?>[] configureORMClasses()
        {
            return new Class<?>[] {Table1.class, Table22.class, Table33.class};
        }
        
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            super.onUpgrade(db, oldVersion, newVersion);
            //            this.onCreate(db);
        }
        
    }
    
    public void testDB()
    {
        TestDBFacade facade =
            new TestDBFacade(getContext(), "upgrade_test.db", null, 6);
        Table1 table1 = new Table1();
        table1.setName("name1");
        facade.insert(table1);
    }
}
