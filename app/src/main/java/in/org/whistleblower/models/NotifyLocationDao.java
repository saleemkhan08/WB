package in.org.whistleblower.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;

public class NotifyLocationDao
{
    public static final String TABLE_SCHEMA = "CREATE TABLE " + NotifyLocation.TABLE + " ("
            + NotifyLocation.EMAIL + " VARCHAR(255), "
            + NotifyLocation.NAME + " VARCHAR(255), "
            + NotifyLocation.PHOTO_URL + " VARCHAR(255), "
            + NotifyLocation.LATITUDE + " VARCHAR(255), "
            + NotifyLocation.LONGITUDE + " VARCHAR(255), "
            + NotifyLocation.MESSAGE + " VARCHAR(255), "
            + NotifyLocation.RADIUS + " INTEGER, "
            + NotifyLocation.USER_EMAIL + " VARCHAR(255) PRIMARY KEY );";

    private WBDataBase mWBDataBase;

    public NotifyLocationDao(Context context)
    {
        mWBDataBase = new WBDataBase(context);
    }

    public void insert(NotifyLocation account)
    {
        if (null != account)
        {
            ContentValues values = new ContentValues();
            values.put(NotifyLocation.NAME, account.name);
            values.put(NotifyLocation.EMAIL, account.email);
            values.put(NotifyLocation.PHOTO_URL, account.photoUrl);
            values.put(NotifyLocation.USER_EMAIL, account.userEmail);
            values.put(NotifyLocation.LONGITUDE, account.longitude);
            values.put(NotifyLocation.LATITUDE, account.latitude);
            values.put(NotifyLocation.MESSAGE, account.message);
            values.put(NotifyLocation.RADIUS, account.radius);
            mWBDataBase.insert(NotifyLocation.TABLE, values);
        }
    }

    public void delete()
    {
        mWBDataBase.delete(NotifyLocation.TABLE, null, null);
    }

    public void delete(String email)
    {
        mWBDataBase.delete(NotifyLocation.TABLE, NotifyLocation.USER_EMAIL + " = " + email, null);
    }

    public ArrayList<NotifyLocation> getList()
    {
        ArrayList<NotifyLocation> accountsList = new ArrayList<>();
        Cursor cursor = mWBDataBase.query(NotifyLocation.TABLE, null, null, null, null, null);
        if (null != cursor)
        {
            while (cursor.moveToNext())
            {
                NotifyLocation account = new NotifyLocation();
                account.name = cursor.getString(cursor.getColumnIndex(NotifyLocation.NAME));
                account.email = cursor.getString(cursor.getColumnIndex(NotifyLocation.EMAIL));
                account.photoUrl = cursor.getString(cursor.getColumnIndex(NotifyLocation.PHOTO_URL));
                account.userEmail =  cursor.getString(cursor.getColumnIndex(NotifyLocation.USER_EMAIL));
                account.latitude =  cursor.getString(cursor.getColumnIndex(NotifyLocation.LATITUDE));
                account.longitude =  cursor.getString(cursor.getColumnIndex(NotifyLocation.LONGITUDE));
                account.message =  cursor.getString(cursor.getColumnIndex(NotifyLocation.MESSAGE));
                account.radius =  cursor.getInt(cursor.getColumnIndex(NotifyLocation.RADIUS));
                accountsList.add(account);
            }
            cursor.close();
        }
        return accountsList;
    }
}