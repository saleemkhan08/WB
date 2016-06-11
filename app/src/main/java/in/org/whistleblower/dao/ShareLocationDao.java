package in.org.whistleblower.dao;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;

import in.org.whistleblower.models.ShareLocation;

public class ShareLocationDao
{
    public static final String TABLE_SCHEMA = "CREATE TABLE " + ShareLocation.TABLE + " ("
            + ShareLocation.EMAIL + " VARCHAR(255), "
            + ShareLocation.NAME + " VARCHAR(255), "
            + ShareLocation.PHOTO_URL + " VARCHAR(255), "
            + ShareLocation.USER_EMAIL + " VARCHAR(255) PRIMARY KEY );";

    public static void insert(ShareLocation account)
    {
        WBDataBase mWBDataBase = new WBDataBase();
        if (null != account)
        {
            ContentValues values = new ContentValues();
            values.put(ShareLocation.NAME, account.name);
            values.put(ShareLocation.EMAIL, account.email);
            values.put(ShareLocation.PHOTO_URL, account.photoUrl);
            values.put(ShareLocation.USER_EMAIL, account.userEmail);
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
        mWBDataBase.delete(ShareLocation.TABLE, ShareLocation.USER_EMAIL + " = ? ", new String []{email});
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
                ShareLocation account = new ShareLocation();
                account.name = cursor.getString(cursor.getColumnIndex(ShareLocation.NAME));
                account.email = cursor.getString(cursor.getColumnIndex(ShareLocation.EMAIL));
                account.photoUrl = cursor.getString(cursor.getColumnIndex(ShareLocation.PHOTO_URL));
                account.userEmail =  cursor.getString(cursor.getColumnIndex(ShareLocation.USER_EMAIL));
                accountsList.add(account);
            }
            cursor.close();
        }
        mWBDataBase.closeDb();
        return accountsList;
    }
}