package in.org.whistleblower.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;

import in.org.whistleblower.models.Notifications;

public class NotificationsDao
{
    public static final String TABLE_SCHEMA = "CREATE TABLE " + Notifications.TABLE + " ("
            + Notifications.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + Notifications.SENDER_NAME + " VARCHAR(255), "
            + Notifications.SENDER_EMAIL + " VARCHAR(255), "
            + Notifications.SENDER_PHOTO_URL + " VARCHAR(255), "
            + Notifications.MESSAGE + " VARCHAR(255), "
            + Notifications.TYPE + " VARCHAR(255), "
            + Notifications.LATITUDE + " VARCHAR(255), "
            + Notifications.LONGITUDE + " VARCHAR(255), "
            + Notifications.STATUS + " INTEGER, "
            + Notifications.TIME_STAMP + " INTEGER )";

    public static void insert(Notifications notification)
    {
        WBDataBase mWBDataBase = new WBDataBase();
        if (null != notification)
        {
            ContentValues values = new ContentValues();
            values.put(Notifications.SENDER_NAME, notification.name);
            values.put(Notifications.SENDER_EMAIL, notification.userEmail);
            values.put(Notifications.SENDER_PHOTO_URL, notification.photoUrl);
            values.put(Notifications.MESSAGE, notification.message);
            values.put(Notifications.TYPE, notification.type);
            values.put(Notifications.LATITUDE, notification.latitude);
            values.put(Notifications.LONGITUDE, notification.longitude);
            values.put(Notifications.STATUS, notification.status);
            values.put(Notifications.TIME_STAMP, notification.timeStamp);

            Log.d("NotificationsDao", "insert : "+mWBDataBase.insert(Notifications.TABLE, values));
        }
        mWBDataBase.closeDb();
    }

    public static void delete()
    {
        WBDataBase mWBDataBase = new WBDataBase();
        mWBDataBase.delete(Notifications.TABLE, null, null);
    }

    public static void delete(long id)
    {
        WBDataBase mWBDataBase = new WBDataBase();
        mWBDataBase.delete(Notifications.TABLE, Notifications.ID + " = " + id, null);
        mWBDataBase.closeDb();
    }

    public static ArrayList<Notifications> getAllNotifications()
    {
        WBDataBase mWBDataBase = new WBDataBase();
        ArrayList<Notifications> NotificationsList = new ArrayList<>();
        Cursor cursor = mWBDataBase.query(Notifications.TABLE, null, null ,null, null, null);
        Log.d("NotificationsDao", "cursor : "+cursor.getCount());
        if (null != cursor)
        {
            while (cursor.moveToNext())
            {
                Notifications notification = new Notifications();
                notification.id = cursor.getLong(cursor.getColumnIndex(Notifications.ID));
                notification.userEmail = cursor.getString(cursor.getColumnIndex(Notifications.SENDER_EMAIL));
                notification.name = cursor.getString(cursor.getColumnIndex(Notifications.SENDER_NAME));
                notification.photoUrl = cursor.getString(cursor.getColumnIndex(Notifications.SENDER_PHOTO_URL));
                notification.message = cursor.getString(cursor.getColumnIndex(Notifications.MESSAGE));
                notification.type = cursor.getString(cursor.getColumnIndex(Notifications.TYPE));
                notification.latitude = cursor.getString(cursor.getColumnIndex(Notifications.LATITUDE));
                notification.longitude = cursor.getString(cursor.getColumnIndex(Notifications.LONGITUDE));
                notification.timeStamp = cursor.getLong(cursor.getColumnIndex(Notifications.TIME_STAMP));
                notification.status = cursor.getInt(cursor.getColumnIndex(Notifications.STATUS));
                NotificationsList.add(notification);
            }
            cursor.close();
        }
        mWBDataBase.closeDb();
        return NotificationsList;
    }

    public static ArrayList<Notifications> getUnreadList()
    {
        WBDataBase mWBDataBase = new WBDataBase();
        ArrayList<Notifications> NotificationsList = new ArrayList<>();
        Cursor cursor = mWBDataBase.query(Notifications.TABLE, null, Notifications.STATUS +" = "+Notifications.UNREAD, null, null, null);
        if (null != cursor)
        {
            while (cursor.moveToNext())
            {
                Notifications notification = new Notifications();
                notification.id = cursor.getLong(cursor.getColumnIndex(Notifications.ID));
                notification.name = cursor.getString(cursor.getColumnIndex(Notifications.SENDER_NAME));
                notification.userEmail = cursor.getString(cursor.getColumnIndex(Notifications.SENDER_EMAIL));
                notification.photoUrl = cursor.getString(cursor.getColumnIndex(Notifications.SENDER_PHOTO_URL));
                notification.message = cursor.getString(cursor.getColumnIndex(Notifications.MESSAGE));
                notification.type = cursor.getString(cursor.getColumnIndex(Notifications.TYPE));
                notification.latitude = cursor.getString(cursor.getColumnIndex(Notifications.LATITUDE));
                notification.longitude = cursor.getString(cursor.getColumnIndex(Notifications.LONGITUDE));
                notification.timeStamp = cursor.getLong(cursor.getColumnIndex(Notifications.TIME_STAMP));
                notification.status = cursor.getInt(cursor.getColumnIndex(Notifications.STATUS));
                NotificationsList.add(notification);
            }
            cursor.close();
        }
        mWBDataBase.closeDb();
        return NotificationsList;
    }

    public static void markRead(long id)
    {
        WBDataBase mWBDataBase = new WBDataBase();
        ContentValues values = new ContentValues();
        values.put(Notifications.STATUS, Notifications.READ);
        mWBDataBase.update(Notifications.TABLE, values, Notifications.ID + " = " + id, null);
        mWBDataBase.closeDb();
    }
}
