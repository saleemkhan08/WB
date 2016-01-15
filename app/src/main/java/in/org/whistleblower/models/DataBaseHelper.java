package in.org.whistleblower.models;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class DataBaseHelper extends SQLiteOpenHelper
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
