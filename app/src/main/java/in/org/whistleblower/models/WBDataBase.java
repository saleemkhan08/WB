package in.org.whistleblower.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import in.org.whistleblower.WhistleBlower;

public class WBDataBase
{
    private SQLiteDatabase db;
    public static final String DATABASE_NAME = "whistle_blower";
    public WBDataBase()
    {
        DataBaseHelper dataBaseHelper = new DataBaseHelper(WhistleBlower.getAppContext(), 1);
        db = dataBaseHelper.getWritableDatabase();
    }

    class DataBaseHelper extends SQLiteOpenHelper
    {
        String[] mTableSchema = {
                IssuesDao.TABLE_SCHEMA,
                FavPlacesDao.TABLE_SCHEMA,
                AccountsDao.TABLE_SCHEMA,
                LocationAlarmDao.TABLE_SCHEMA,
                ShareLocationDao.TABLE_SCHEMA,
                NotifyLocationDao.TABLE_SCHEMA
        };
        String[] mDropTable = {
                IssuesDao.DROP_TABLE,
                FavPlacesDao.TABLE_SCHEMA,
                AccountsDao.TABLE_SCHEMA,
                LocationAlarmDao.TABLE_SCHEMA,
                ShareLocationDao.TABLE_SCHEMA,
                NotifyLocationDao.TABLE_SCHEMA
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
        Log.d("DatabaseProblem","insert : tblname : "+tblname +", values : "+values );
        return db.insertWithOnConflict(tblname, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public long insertShow(String tblname, ContentValues values)
    {
        Log.d("DatabaseProblem","insert : tblname : "+tblname +", values : "+values );
        return db.insert(tblname, null, values);
    }

    public Cursor query(String tableName, String[] columns, String selection, String[] selectionArgs, String groupBy, String orderBy)
    {
        return db.query(tableName, columns, selection, selectionArgs, groupBy, null, orderBy, null);
    }

    public Cursor query(String query)
    {
        return db.rawQuery(query, null);
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
