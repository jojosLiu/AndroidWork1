package com.bignerdranch.android.databasetest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLiteOpenHelper extends SQLiteOpenHelper {
    // 数据库的名字
    private final static String DB_NAME = "db_mycontacts.db";
    // 数据库版本号
    private final static int VERSION = 1;
    // 数据库操作对象
    public SQLiteDatabase dbConn;

    public MySQLiteOpenHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
        dbConn = this.getReadableDatabase();
    }
    // 创建数据库和其中的表结构的
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建表结构
        db.execSQL("create table if not exists tb_mycontacts(_id integer primary key autoincrement , username , phonenumber)");
    }
    // 更新数据库及其中的 表结构的
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {   //检查是否为第一次安装
        // 如果新的版本号高于旧的版本号
        if (newVersion > oldVersion) {
            // 删除表结构
            db.execSQL("drop table if exists tb_mycontacts");
            onCreate(db);
        }
    }

     //执行带占位符的select语句，查询数据，返回Cursor
    public Cursor selectCursor(String sql, String[] selectionArgs) {
        return dbConn.rawQuery(sql, selectionArgs);
    }

  //执行带占位符的select语句，返回结果集的个数
    public int selectCount(String sql, String[] selectionArgs) {
        Cursor cursor = dbConn.rawQuery(sql, selectionArgs);
        if (cursor != null) {
            cursor.moveToFirst();
            int count = cursor.getInt(0);
            cursor.close();
            return count;
        } else {
            return 0;
        }
    }

    //执行带占位符的select语句，返回多条数据，放进List集合中。
    public List<Map<String, Object>> selectList(String sql, String[] selectionArgs) {
        Cursor cursor = dbConn.rawQuery(sql, selectionArgs);
        return cursorToList(cursor);
    }

  //将Cursor对象转成List集合
    public List<Map<String, Object>> cursorToList(Cursor cursor) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

        String[] arrColumnName = cursor.getColumnNames();
        while (cursor.moveToNext()) {
            Map<String, Object> map = new HashMap<String, Object>();
            for (int i = 0; i < arrColumnName.length; i++) {
                Object cols_value = cursor.getString(i);
                map.put(arrColumnName[i], cols_value);
            }
            list.add(map);
        }
        if (cursor != null) {
            cursor.close();
        }
        return list;
    }

    //执行带占位符的update、insert、delete语句，更新数据库，返回true或false
    public boolean execData(String sql, Object[] bindArgs) {
        try {
            dbConn.execSQL(sql, bindArgs);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void destroy() {
        if (dbConn != null) {
            dbConn.close();
        }
    }
    public boolean deleteData(String sql){
        try{
            dbConn.execSQL(sql);
            return true;
        }catch (Exception e)
        {
            return false;
        }

    }
}