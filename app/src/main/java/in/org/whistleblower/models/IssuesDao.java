package in.org.whistleblower.models;

import android.content.Context;
import android.preference.PreferenceManager;

public class IssuesDao extends Issues
{
    public static final String KEY_DATABASE_VERSION = "KEY_DATABASE_VERSION";
    Context mContext;

    public IssuesDao(Context context)
    {
        mContext = context;
        /*
        util = new MiscUtil(context);
        */
    }

    public void createDatabase()
    {
        int databaseVersion =  PreferenceManager.getDefaultSharedPreferences(mContext).getInt(KEY_DATABASE_VERSION, 0);
        DataBaseHelper dataBaseHelper = new DataBaseHelper(mContext, databaseVersion);
    }
}
