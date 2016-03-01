package in.org.whistleblower.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;

public class IssuesDao
{
    public static final String TABLE = "Issues";
    public static final String ISSUE_ID = "issue_id";
    public static final String DESCRIPTION = "DESCRIPTION";
    public static final String PLACE_NAME = "PLACE_NAME";
    public static final String IMAGE_URL = "FILE_URL";
    public static final String USER_DP_URL = "USER_DP_URL";
    public static final String USERNAME = "USERNAME";

    public static final String RADIUS = "RADIUS";
    public static final String AREA_TYPE = "AREA_TYPE";
    public static final String USER_ID = "userId";
    public static final String ANONYMOUS = "ANONYMOUS";
    public static final String STATUS = "STATUS";

    public static final String LONGITUDE = "LONGITUDE";
    public static final String LATITUDE = "LATITUDE";
    public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE;
    public static final String SPAM = "SPAM";
    public static final String TABLE_SCHEMA = "CREATE TABLE " + TABLE + "("
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + ISSUE_ID + " VARCHAR(255), "
            + DESCRIPTION + " VARCHAR(255), "
            + USERNAME + " VARCHAR(255), "
            + PLACE_NAME + " VARCHAR(255), "
            + IMAGE_URL + " VARCHAR(255), "
            + USER_DP_URL + " VARCHAR(255), "
            + RADIUS + " INTEGER, "
            + AREA_TYPE + " VARCHAR(255), "
            + USER_ID + " VARCHAR(255), "
            + LONGITUDE + " REAL, "
            + LATITUDE + " REAL );";

    public static final String ALTER_TABLE_SCHEMA = TABLE_SCHEMA;
    //TODO write a query to save the old data and just alter the table.
    Context mContext;
    WBDataBase mWBDataBase;

    public IssuesDao(Context context)
    {
        mContext = context;
        mWBDataBase = new WBDataBase(mContext);
    }

    public void insert(Issue issue)
    {

        ContentValues values = new ContentValues();
        values.put(IssuesDao.IMAGE_URL, issue.imgUrl);
        values.put(IssuesDao.ISSUE_ID, issue.issueId);
        values.put(IssuesDao.LATITUDE, issue.latitude);
        values.put(IssuesDao.LONGITUDE, issue.longitude);
        values.put(IssuesDao.DESCRIPTION, issue.description);
        values.put(IssuesDao.PLACE_NAME, issue.placeName);
        values.put(IssuesDao.USER_DP_URL, issue.userDpUrl);
        values.put(IssuesDao.USER_ID, issue.userId);
        values.put(IssuesDao.USERNAME, issue.username);
        values.put(IssuesDao.RADIUS, issue.radius);
        values.put(IssuesDao.AREA_TYPE, issue.areaType);
        mWBDataBase.insert(IssuesDao.TABLE, values);
    }

    public void delete()
    {
        mWBDataBase.delete(IssuesDao.TABLE, null, null);
    }

    public ArrayList<Issue> getIssuesList()
    {
        ArrayList<Issue> issuesList = new ArrayList<>();
        Cursor cursor = mWBDataBase.query(IssuesDao.TABLE, null, null, null, null, null);
        if (null != cursor)
        {
            while (cursor.moveToNext())
            {
                Issue issue = new Issue();
                issue.issueId = cursor.getString(cursor.getColumnIndex(IssuesDao.ISSUE_ID));
                issue.imgUrl = cursor.getString(cursor.getColumnIndex(IssuesDao.IMAGE_URL));
                issue.latitude = cursor.getFloat(cursor.getColumnIndex(IssuesDao.LATITUDE));
                issue.longitude = cursor.getFloat(cursor.getColumnIndex(IssuesDao.LONGITUDE));

                issue.description = cursor.getString(cursor.getColumnIndex(IssuesDao.DESCRIPTION));
                issue.placeName = cursor.getString(cursor.getColumnIndex(IssuesDao.PLACE_NAME));
                issue.userDpUrl = cursor.getString(cursor.getColumnIndex(IssuesDao.USER_DP_URL));
                issue.userId = cursor.getString(cursor.getColumnIndex(IssuesDao.USER_ID));
                issue.username = cursor.getString(cursor.getColumnIndex(IssuesDao.USERNAME));
                issue.radius = cursor.getInt(cursor.getColumnIndex(IssuesDao.RADIUS));
                issue.areaType = cursor.getString(cursor.getColumnIndex(IssuesDao.AREA_TYPE));

                issuesList.add(issue);
            }
            cursor.close();
        }
        mWBDataBase.closeDb();
        return issuesList;
    }

    public boolean issueExist(String id)
    {
        Cursor cursor = mWBDataBase.query(IssuesDao.TABLE, new String[]{ISSUE_ID}, ISSUE_ID +" = ?", new String[]{id},null, null);
        if (null != cursor)
        {
            if(cursor.getCount() <= 0){
                cursor.close();
                return false;
            }
            cursor.close();
        }
        mWBDataBase.closeDb();
        return true;
    }
}
