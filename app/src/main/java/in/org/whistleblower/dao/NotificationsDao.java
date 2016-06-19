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
            + Notifications.SENDER_LATITUDE + " VARCHAR(255), "
            + Notifications.SENDER_LONGITUDE + " VARCHAR(255), "
            + Notifications.RECEIVER_STATUS + " INTEGER, "
            + Notifications.SERVER_NOTIFICATION_ID + " INTEGER, "
            + Notifications.TIME_STAMP + " INTEGER )";

    public static void insert(Notifications notification)
    {
        WBDataBase mWBDataBase = new WBDataBase();
        if (null != notification)
        {
            ContentValues values = new ContentValues();
            values.put(Notifications.SENDER_NAME, notification.senderName);
            values.put(Notifications.SENDER_EMAIL, notification.senderEmail);
            values.put(Notifications.SENDER_PHOTO_URL, notification.senderPhotoUrl);
            values.put(Notifications.MESSAGE, notification.message);
            values.put(Notifications.TYPE, notification.type);
            values.put(Notifications.SENDER_LATITUDE, notification.senderLatitude);
            values.put(Notifications.SENDER_LONGITUDE, notification.senderLongitude);
            values.put(Notifications.RECEIVER_STATUS, notification.status);
            values.put(Notifications.TIME_STAMP, notification.timeStamp);
            values.put(Notifications.SERVER_NOTIFICATION_ID, notification.serverNotificationId);

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
                notification.senderEmail = cursor.getString(cursor.getColumnIndex(Notifications.SENDER_EMAIL));
                notification.senderName = cursor.getString(cursor.getColumnIndex(Notifications.SENDER_NAME));
                notification.senderPhotoUrl = cursor.getString(cursor.getColumnIndex(Notifications.SENDER_PHOTO_URL));
                notification.message = cursor.getString(cursor.getColumnIndex(Notifications.MESSAGE));
                notification.type = cursor.getString(cursor.getColumnIndex(Notifications.TYPE));
                notification.senderLatitude = cursor.getString(cursor.getColumnIndex(Notifications.SENDER_LATITUDE));
                notification.senderLongitude = cursor.getString(cursor.getColumnIndex(Notifications.SENDER_LONGITUDE));
                notification.timeStamp = cursor.getLong(cursor.getColumnIndex(Notifications.TIME_STAMP));
                notification.serverNotificationId = cursor.getLong(cursor.getColumnIndex(Notifications.SERVER_NOTIFICATION_ID));
                notification.status = cursor.getInt(cursor.getColumnIndex(Notifications.RECEIVER_STATUS));
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
        Cursor cursor = mWBDataBase.query(Notifications.TABLE, null, Notifications.RECEIVER_STATUS +" = "+Notifications.UNREAD, null, null, null);
        if (null != cursor)
        {
            while (cursor.moveToNext())
            {
                Notifications notification = new Notifications();
                notification.id = cursor.getLong(cursor.getColumnIndex(Notifications.ID));
                notification.senderName = cursor.getString(cursor.getColumnIndex(Notifications.SENDER_NAME));
                notification.senderEmail = cursor.getString(cursor.getColumnIndex(Notifications.SENDER_EMAIL));
                notification.senderPhotoUrl = cursor.getString(cursor.getColumnIndex(Notifications.SENDER_PHOTO_URL));
                notification.message = cursor.getString(cursor.getColumnIndex(Notifications.MESSAGE));
                notification.type = cursor.getString(cursor.getColumnIndex(Notifications.TYPE));
                notification.senderLatitude = cursor.getString(cursor.getColumnIndex(Notifications.SENDER_LATITUDE));
                notification.senderLongitude = cursor.getString(cursor.getColumnIndex(Notifications.SENDER_LONGITUDE));
                notification.timeStamp = cursor.getLong(cursor.getColumnIndex(Notifications.TIME_STAMP));
                notification.serverNotificationId = cursor.getLong(cursor.getColumnIndex(Notifications.SERVER_NOTIFICATION_ID));
                notification.status = cursor.getInt(cursor.getColumnIndex(Notifications.RECEIVER_STATUS));
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
        values.put(Notifications.RECEIVER_STATUS, Notifications.READ);
        mWBDataBase.update(Notifications.TABLE, values, Notifications.ID + " = " + id, null);
        mWBDataBase.closeDb();
    }
}
