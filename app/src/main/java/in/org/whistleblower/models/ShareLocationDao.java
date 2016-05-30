package in.org.whistleblower.models;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;

public class ShareLocationDao
{
    public static final String TABLE_SCHEMA = "CREATE TABLE " + ShareLocation.TABLE + " ("
            + ShareLocation.EMAIL + " VARCHAR(255), "
            + ShareLocation.NAME + " VARCHAR(255), "
            + ShareLocation.PHOTO_URL + " VARCHAR(255), "
            + ShareLocation.USER_EMAIL + " VARCHAR(255) PRIMARY KEY );";

    private WBDataBase mWBDataBase;

    public ShareLocationDao()
    {
        mWBDataBase = new WBDataBase();
    }

    public void insert(ShareLocation account)
    {
        if (null != account)
        {
            ContentValues values = new ContentValues();
            values.put(ShareLocation.NAME, account.name);
            values.put(ShareLocation.EMAIL, account.email);
            values.put(ShareLocation.PHOTO_URL, account.photoUrl);
            values.put(ShareLocation.USER_EMAIL, account.userEmail);
            mWBDataBase.insert(ShareLocation.TABLE, values);
        }
    }

    public void delete()
    {
        mWBDataBase.delete(ShareLocation.TABLE, null, null);
    }

    public void delete(String email)
    {
        mWBDataBase.delete(ShareLocation.TABLE, ShareLocation.USER_EMAIL + " = '" + email+"'", null);
    }

    public ArrayList<ShareLocation> getList()
    {
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
        return accountsList;
    }
}