package in.org.whistleblower.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class WBDataBase
{
    private SQLiteDatabase db;

    public WBDataBase(Context context)
    {
        DataBaseHelper dataBaseHelper = new DataBaseHelper(context, 1);
        db = dataBaseHelper.getWritableDatabase();
    }

    class DataBaseHelper extends SQLiteOpenHelper
    {
        public static final String DATABASE_NAME = "whistle_blower";
        String[] mTableSchema = {
                Issues.TABLE_SCHEMA
        };
        String[] mDropTable = {
                Issues.DROP_TABLE
        };

        Context mContext;

        public DataBaseHelper(Context context, int version)
        {
            super(context, DATABASE_NAME, null, version);
            mContext = context;

        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            try
            {
                for (String schema : mTableSchema)
                {
                    db.execSQL(schema);
                }
            }
            catch (SQLException e)
            {
                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            try
            {
                for (String schema : mDropTable)
                {
                    db.execSQL(schema);
                }

                onCreate(db);
            }
            catch (SQLException e)
            {
                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    public long insert(String tblname, ContentValues values)
    {
        return db.insert(tblname, null, values);
    }

    public Cursor query(String tableName, String[] columns, String selection, String[] selectionArgs, String groupBy, String orderBy)
    {
        return db.query(tableName, columns, selection, selectionArgs, groupBy, null, orderBy, null);
    }

    public long update(String tblname, ContentValues values, String whereClause, String[] whereArgs)
    {
        return db.update(tblname, values, whereClause, whereArgs);
    }

    public long delete(String tblname, String whereClause, String[] whereArgs)
    {
        return db.delete(tblname, whereClause, whereArgs);
    }

    public void closeDb()
    {
        db.close();
    }
}
