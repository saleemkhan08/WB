package in.org.whistleblower.dao;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;

import in.org.whistleblower.models.NotifyLocation;

public class NotifyLocationDao
{
    public static final String TABLE_SCHEMA = "CREATE TABLE " + NotifyLocation.TABLE + " ("
            + NotifyLocation.SENDER_EMAIL + " VARCHAR(255), "
            + NotifyLocation.SENDER_NAME + " VARCHAR(255), "
            + NotifyLocation.SENDER_PHOTO_URL + " VARCHAR(255), "
            + NotifyLocation.RECEIVER_EMAIL + " VARCHAR(255) PRIMARY KEY, "
            + NotifyLocation.RECEIVER_NAME + " VARCHAR(255), "
            + NotifyLocation.RECEIVER_PHOTO_URL + " VARCHAR(255), "
            + NotifyLocation.LATITUDE + " VARCHAR(255), "
            + NotifyLocation.LONGITUDE + " VARCHAR(255), "
            + NotifyLocation.MESSAGE + " VARCHAR(255), "
            + NotifyLocation.RADIUS + " INTEGER, "
            + NotifyLocation.STATUS + " INTEGER );";

    public static void insert(NotifyLocation account)
    {
        WBDataBase mWBDataBase = new WBDataBase();
        if (null != account)
        {
            ContentValues values = new ContentValues();
            values.put(NotifyLocation.SENDER_EMAIL, account.senderEmail);
            values.put(NotifyLocation.SENDER_NAME, account.senderName);
            values.put(NotifyLocation.SENDER_PHOTO_URL, account.senderPhotoUrl);
            values.put(NotifyLocation.RECEIVER_EMAIL, account.receiverEmail);
            values.put(NotifyLocation.RECEIVER_NAME, account.receiverName);
            values.put(NotifyLocation.RECEIVER_PHOTO_URL, account.receiverPhotoUrl);
            values.put(NotifyLocation.LONGITUDE, account.longitude);
            values.put(NotifyLocation.LATITUDE, account.latitude);
            values.put(NotifyLocation.MESSAGE, account.message);
            values.put(NotifyLocation.RADIUS, account.radius);
            values.put(NotifyLocation.STATUS, account.status);
            mWBDataBase.insert(NotifyLocation.TABLE, values);
        }
        mWBDataBase.closeDb();
    }

    public static void delete()
    {
        WBDataBase mWBDataBase = new WBDataBase();
        mWBDataBase.delete(NotifyLocation.TABLE, null, null);
        mWBDataBase.closeDb();
    }

    public static void delete(String email)
    {
        WBDataBase mWBDataBase = new WBDataBase();
        mWBDataBase.delete(NotifyLocation.TABLE, NotifyLocation.RECEIVER_EMAIL + " = ? ", new String []{email});
        mWBDataBase.closeDb();
    }

    public static ArrayList<NotifyLocation> getList()
    {
        WBDataBase mWBDataBase = new WBDataBase();
        ArrayList<NotifyLocation> accountsList = new ArrayList<>();
        Cursor cursor = mWBDataBase.query(NotifyLocation.TABLE, null, null, null, null, null);
        if (null != cursor)
        {
            while (cursor.moveToNext())
            {
                NotifyLocation account = new NotifyLocation();
                account.senderEmail = cursor.getString(cursor.getColumnIndex(NotifyLocation.SENDER_EMAIL));
                account.senderName = cursor.getString(cursor.getColumnIndex(NotifyLocation.SENDER_NAME));
                account.senderPhotoUrl = cursor.getString(cursor.getColumnIndex(NotifyLocation.SENDER_PHOTO_URL));
                account.receiverEmail = cursor.getString(cursor.getColumnIndex(NotifyLocation.RECEIVER_EMAIL));
                account.receiverName = cursor.getString(cursor.getColumnIndex(NotifyLocation.RECEIVER_NAME));
                account.receiverPhotoUrl = cursor.getString(cursor.getColumnIndex(NotifyLocation.RECEIVER_PHOTO_URL));

                account.latitude =  cursor.getString(cursor.getColumnIndex(NotifyLocation.LATITUDE));
                account.longitude =  cursor.getString(cursor.getColumnIndex(NotifyLocation.LONGITUDE));
                account.message =  cursor.getString(cursor.getColumnIndex(NotifyLocation.MESSAGE));
                account.radius =  cursor.getInt(cursor.getColumnIndex(NotifyLocation.RADIUS));
                account.status =  cursor.getInt(cursor.getColumnIndex(NotifyLocation.STATUS));
                accountsList.add(account);
            }
            cursor.close();
        }
        mWBDataBase.closeDb();
        return accountsList;
    }
}