package in.org.whistleblower.dao;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;

import in.org.whistleblower.models.ShareLocation;

public class ShareLocationDao
{
    public static final String TABLE_SCHEMA = "CREATE TABLE " + ShareLocation.TABLE + " ("
            + ShareLocation.SENDER_EMAIL + " VARCHAR(255), "
            + ShareLocation.SENDER_NAME + " VARCHAR(255), "
            + ShareLocation.SENDER_PHOTO_URL + " VARCHAR(255), "
            + ShareLocation.SERVER_NOTIFICATION_ID + " INTEGER PRIMARY KEY, "
            + ShareLocation.RECEIVER_EMAIL + " VARCHAR(255));";

    public static void insert(ShareLocation shareLocation)
    {
        WBDataBase mWBDataBase = new WBDataBase();
        if (null != shareLocation)
        {
            ContentValues values = new ContentValues();
            values.put(ShareLocation.SENDER_NAME, shareLocation.senderName);
            values.put(ShareLocation.SENDER_EMAIL, shareLocation.senderEmail);
            values.put(ShareLocation.SENDER_PHOTO_URL, shareLocation.senderPhotoUrl);
            values.put(ShareLocation.RECEIVER_EMAIL, shareLocation.receiverEmail);
            values.put(ShareLocation.SERVER_NOTIFICATION_ID, shareLocation.serverNotificationId);
            mWBDataBase.insert(ShareLocation.TABLE, values);
        }
        mWBDataBase.closeDb();
    }

    public static void delete()
    {
        WBDataBase mWBDataBase = new WBDataBase();
        mWBDataBase.delete(ShareLocation.TABLE, null, null);
        mWBDataBase.closeDb();
    }

    public static void delete(String email)
    {
        WBDataBase mWBDataBase = new WBDataBase();
        mWBDataBase.delete(ShareLocation.TABLE, ShareLocation.RECEIVER_EMAIL + " = ? ", new String []{email});
        mWBDataBase.closeDb();
    }

    public static ArrayList<ShareLocation> getList()
    {
        WBDataBase mWBDataBase = new WBDataBase();
        ArrayList<ShareLocation> accountsList = new ArrayList<>();
        Cursor cursor = mWBDataBase.query(ShareLocation.TABLE, null, null, null, null, null);
        if (null != cursor)
        {
            while (cursor.moveToNext())
            {
                ShareLocation shareLocation = new ShareLocation();
                shareLocation.senderName = cursor.getString(cursor.getColumnIndex(ShareLocation.SENDER_NAME));
                shareLocation.senderEmail = cursor.getString(cursor.getColumnIndex(ShareLocation.SENDER_EMAIL));
                shareLocation.senderPhotoUrl = cursor.getString(cursor.getColumnIndex(ShareLocation.SENDER_PHOTO_URL));
                shareLocation.receiverEmail =  cursor.getString(cursor.getColumnIndex(ShareLocation.RECEIVER_EMAIL));
                shareLocation.serverNotificationId =  cursor.getLong(cursor.getColumnIndex(ShareLocation.SERVER_NOTIFICATION_ID));
                accountsList.add(shareLocation);
            }
            cursor.close();
        }
        mWBDataBase.closeDb();
        return accountsList;
    }
}