package in.org.whistleblower.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;

import in.org.whistleblower.models.Issue;
import in.org.whistleblower.utilities.VolleyUtil;

public class IssuesDao
{

    public static final String ISSUE_ID = "issueId";
    public static final String NO_OF_IMAGES = "noOfImages";
    public static final String USER_ID = "userId";
    public static final String USER_DP_URL = "userPhotoUrl";
    public static final String USERNAME = "username";
    public static final String DESCRIPTION = "description";
    public static final String AREA_TYPE = "areaType";
    public static final String RADIUS = "radius";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String STATUS = "status";


    public static final String TABLE = "Issues";
    public static final String PLACE_NAME = "placeName";
    public static final String IMAGE_URL = "FILE_URL";
    public static final String IMAGE_LOCAL_URI = "IMAGE_LOCAL_URI";
    public static final String ANONYMOUS = "ANONYMOUS";

    public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE;
    public static final String SPAM = "SPAM";
    public static final String TABLE_SCHEMA = "CREATE TABLE " + TABLE + "("
            + ISSUE_ID + " VARCHAR(255) PRIMARY KEY, "
            + NO_OF_IMAGES + " INTEGER, "
            + USER_ID + " VARCHAR(255), "
            + USER_DP_URL + " VARCHAR(255), "
            + USERNAME + " VARCHAR(255), "
            + DESCRIPTION + " VARCHAR(255), "
            + AREA_TYPE + " VARCHAR(255), "
            + RADIUS + " INTEGER, "
            + LATITUDE + " VARCHAR(255), "
            + LONGITUDE + " VARCHAR(255) );";

    public static final String ALTER_TABLE_SCHEMA = TABLE_SCHEMA;
    public static final String LATLNG = "LATLNG";
    //TODO write a query to save the old data and just alter the table.

    public static void insert(Issue issue)
    {
        WBDataBase mWBDataBase = new WBDataBase();
        ContentValues values = new ContentValues();
        values.put(IssuesDao.ISSUE_ID, issue.issueId);
        values.put(IssuesDao.NO_OF_IMAGES, 1);
        values.put(IssuesDao.USER_ID, issue.userId);
        values.put(IssuesDao.USER_DP_URL, issue.userDpUrl);
        values.put(IssuesDao.USERNAME, issue.username);
        values.put(IssuesDao.DESCRIPTION, issue.description);
        values.put(IssuesDao.AREA_TYPE, issue.areaType);
        values.put(IssuesDao.RADIUS, issue.radius);
        values.put(IssuesDao.LATITUDE, issue.latitude);
        values.put(IssuesDao.LONGITUDE, issue.longitude);
        mWBDataBase.insert(IssuesDao.TABLE, values);
    }

    public static void delete()
    {
        WBDataBase mWBDataBase = new WBDataBase();
        mWBDataBase.delete(IssuesDao.TABLE, null, null);
    }

    public static void delete(String issueId)
    {
        WBDataBase mWBDataBase = new WBDataBase();
        mWBDataBase.delete(IssuesDao.TABLE, ISSUE_ID + " = ? ", new String[]{issueId});
    }

    public static ArrayList<Issue> getList()
    {
        WBDataBase mWBDataBase = new WBDataBase();
        ArrayList<Issue> issuesList = new ArrayList<>();
        Cursor cursor = mWBDataBase.query(IssuesDao.TABLE, null, null, null, null, null);
        Log.d("Cursor", "Count : " + cursor.getCount());
        if (null != cursor && cursor.getCount() > 0)
        {
            while (cursor.moveToNext())
            {
                Issue issue = new Issue();
                issue.issueId = cursor.getString(cursor.getColumnIndex(IssuesDao.ISSUE_ID));
                issue.imgUrl = VolleyUtil.IMAGE_URL + issue.issueId + ".png";
                issue.userId = cursor.getString(cursor.getColumnIndex(IssuesDao.USER_ID));
                issue.userDpUrl = cursor.getString(cursor.getColumnIndex(IssuesDao.USER_DP_URL));
                issue.username = cursor.getString(cursor.getColumnIndex(IssuesDao.USERNAME));
                issue.description = cursor.getString(cursor.getColumnIndex(IssuesDao.DESCRIPTION));
                issue.areaType = cursor.getString(cursor.getColumnIndex(IssuesDao.AREA_TYPE));
                issue.radius = cursor.getInt(cursor.getColumnIndex(IssuesDao.RADIUS));
                issue.latitude = cursor.getString(cursor.getColumnIndex(IssuesDao.LATITUDE));
                issue.longitude = cursor.getString(cursor.getColumnIndex(IssuesDao.LONGITUDE));
                issuesList.add(issue);
            }
            cursor.close();
        }
        mWBDataBase.closeDb();
        return issuesList;
    }

    public static boolean issueExist(String id)
    {
        WBDataBase mWBDataBase = new WBDataBase();
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
