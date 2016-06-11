package in.org.whistleblower.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;

import in.org.whistleblower.models.Accounts;

public class AccountsDao
{
    public static final String TABLE_SCHEMA = "CREATE TABLE " + Accounts.TABLE + " ("
            + Accounts.EMAIL + " VARCHAR(255) PRIMARY KEY, "
            + Accounts.NAME + " VARCHAR(255), "
            + Accounts.PHOTO_URL + " VARCHAR(255),"
            + Accounts.TIME_STAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
            + Accounts.RELATION + " VARCHAR(255))";
    private static WBDataBase mWBDataBase;

    public static void insert(Accounts account)
    {
        mWBDataBase = new WBDataBase();
        if (null != account)
        {
            ContentValues values = new ContentValues();
            values.put(Accounts.NAME, account.name);
            values.put(Accounts.EMAIL, account.email);
            values.put(Accounts.PHOTO_URL, account.photo_url);
            values.put(Accounts.RELATION, account.relation);
            Log.d("AccountsDao", "insert : "+mWBDataBase.insert(Accounts.TABLE, values));
        }
    }

    public static void delete()
    {
        mWBDataBase = new WBDataBase();
        mWBDataBase.delete(Accounts.TABLE, null, null);
    }

    public static void delete(String email)
    {
        mWBDataBase = new WBDataBase();
        mWBDataBase.delete(Accounts.TABLE, Accounts.EMAIL + " = ? ", new String[]{email});
        mWBDataBase.closeDb();
    }

    public static ArrayList<Accounts> getUsersList()
    {
        mWBDataBase = new WBDataBase();
        ArrayList<Accounts> accountsList = new ArrayList<>();
        Cursor cursor = mWBDataBase.query(Accounts.TABLE, null, Accounts.RELATION + " != ? ",new String[]{Accounts.FRIEND}, null, null);
        if (null != cursor)
        {
            while (cursor.moveToNext())
            {
                Accounts account = new Accounts();
                account.name = cursor.getString(cursor.getColumnIndex(Accounts.NAME));
                account.email = cursor.getString(cursor.getColumnIndex(Accounts.EMAIL));
                account.photo_url = cursor.getString(cursor.getColumnIndex(Accounts.PHOTO_URL));
                account.relation = cursor.getString(cursor.getColumnIndex(Accounts.RELATION));
                accountsList.add(account);
            }
            cursor.close();
        }
        mWBDataBase.closeDb();
        return accountsList;
    }

    public static ArrayList<Accounts> getList()
    {
        mWBDataBase = new WBDataBase();
        ArrayList<Accounts> accountsList = new ArrayList<>();
        Cursor cursor = mWBDataBase.query(Accounts.TABLE, null, null, null, null, null);
        if (null != cursor)
        {
            while (cursor.moveToNext())
            {
                Accounts account = new Accounts();
                account.name = cursor.getString(cursor.getColumnIndex(Accounts.NAME));
                account.email = cursor.getString(cursor.getColumnIndex(Accounts.EMAIL));
                account.photo_url = cursor.getString(cursor.getColumnIndex(Accounts.PHOTO_URL));
                account.relation =  cursor.getString(cursor.getColumnIndex(Accounts.RELATION));
                accountsList.add(account);
            }
            cursor.close();

        }
        mWBDataBase.closeDb();
        return accountsList;
    }


    public static ArrayList<Accounts> getFriendsList()
    {
        mWBDataBase = new WBDataBase();
        ArrayList<Accounts> accountsList = new ArrayList<>();
        Cursor cursor = mWBDataBase.query(Accounts.TABLE, null, Accounts.RELATION+" = ? ",new String[]{Accounts.FRIEND}, null, null);
        if (null != cursor)
        {
            while (cursor.moveToNext())
            {
                Accounts account = new Accounts();
                account.name = cursor.getString(cursor.getColumnIndex(Accounts.NAME));
                account.email = cursor.getString(cursor.getColumnIndex(Accounts.EMAIL));
                account.relation = cursor.getString(cursor.getColumnIndex(Accounts.RELATION));
                account.photo_url = cursor.getString(cursor.getColumnIndex(Accounts.PHOTO_URL));

                accountsList.add(account);
            }
            cursor.close();
        }
        mWBDataBase.closeDb();
        return accountsList;
    }

    public static void update(String email, String relation, String friend)
    {
        mWBDataBase = new WBDataBase();
        ContentValues values = new ContentValues();
        values.put(relation, friend);
        mWBDataBase.update(Accounts.TABLE, values, Accounts.EMAIL + " = ? ", new String[]{email});
        mWBDataBase.closeDb();
    }
}
