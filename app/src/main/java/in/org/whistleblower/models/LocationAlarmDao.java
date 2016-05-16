package in.org.whistleblower.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;

public class LocationAlarmDao
{

    public static final String TABLE_SCHEMA = "CREATE TABLE " + LocationAlarm.TABLE + " ("
            + LocationAlarm.ADDRESS + " VARCHAR(255) PRIMARY KEY, "
            + LocationAlarm.RADIUS + " INTEGER, "
            + LocationAlarm.LATITUDE + " VARCHAR(255), "
            + LocationAlarm.STATUS + " INTEGER, "
            + LocationAlarm.LONGITUDE + " VARCHAR(255));";

    private WBDataBase mWBDataBase;

    public LocationAlarmDao(Context context)
    {
        mWBDataBase = new WBDataBase(context);
    }

    public void insert(LocationAlarm locationAlarm)
    {
        if (null != locationAlarm)
        {
            ContentValues values = new ContentValues();
            values.put(LocationAlarm.ADDRESS, locationAlarm.address);
            values.put(LocationAlarm.LATITUDE, locationAlarm.latitude);
            values.put(LocationAlarm.LONGITUDE, locationAlarm.longitude);
            values.put(LocationAlarm.RADIUS, locationAlarm.radius);
            values.put(LocationAlarm.STATUS, locationAlarm.status);
            mWBDataBase.insert(LocationAlarm.TABLE, values);
        }
    }

    public void delete()
    {
        mWBDataBase.delete(LocationAlarm.TABLE, null, null);
    }

    public void delete(String address)
    {
        mWBDataBase.delete(LocationAlarm.TABLE, LocationAlarm.ADDRESS + " = " + address, null);
    }

    public ArrayList<LocationAlarm> getList()
    {
        ArrayList<LocationAlarm> alarmList = new ArrayList<>();
        Cursor cursor = mWBDataBase.query(LocationAlarm.TABLE, null, null, null, null, null);
        if (null != cursor)
        {
            while (cursor.moveToNext())
            {
                LocationAlarm alarm = new LocationAlarm();
                alarm.address = cursor.getString(cursor.getColumnIndex(LocationAlarm.ADDRESS));
                alarm.radius = cursor.getInt(cursor.getColumnIndex(LocationAlarm.RADIUS));
                alarm.latitude =  cursor.getString(cursor.getColumnIndex(LocationAlarm.LATITUDE));
                alarm.longitude =  cursor.getString(cursor.getColumnIndex(LocationAlarm.LONGITUDE));
                alarm.status = cursor.getInt(cursor.getColumnIndex(LocationAlarm.STATUS));
                alarmList.add(alarm);
            }
            cursor.close();
        }
        return alarmList;
    }

    public void update(String address, int status)
    {
        ContentValues values = new ContentValues();
        values.put(LocationAlarm.STATUS, status);
        mWBDataBase.update(LocationAlarm.TABLE, values, LocationAlarm.ADDRESS + " = '" + address + "'", null);
    }
}