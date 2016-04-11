package in.org.whistleblower.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;

public class AccountsDao
{
    public static final String TABLE_SCHEMA = "CREATE TABLE " + Accounts.TABLE + " ("
            + Accounts.EMAIL + " VARCHAR(255) PRIMARY KEY, "
            + Accounts.NAME + " VARCHAR(255), "
            + Accounts.PHOTO_URL + " VARCHAR(255),"
            + Accounts.TIME_STAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
            + Accounts.RELATION + " VARCHAR(255))";
    private Context mContext;
    private WBDataBase mWBDataBase;

    public AccountsDao(Context context)
    {
        this.mContext = context;
        mWBDataBase = new WBDataBase(mContext);
    }

    public void insert(Accounts account)
    {
        if (null != account)
        {
            ContentValues values = new ContentValues();
            values.put(Accounts.NAME, account.name);
            values.put(Accounts.EMAIL, account.email);
            values.put(Accounts.PHOTO_URL, account.photo_url);
            values.put(Accounts.RELATION, account.relation);
            mWBDataBase.insert(Accounts.TABLE, values);
        }
    }

    public void delete()
    {
        mWBDataBase.delete(Accounts.TABLE, null, null);
    }

    public void delete(String email)
    {
        mWBDataBase.delete(Accounts.TABLE, Accounts.EMAIL + " = " + email, null);
    }

    public ArrayList<Accounts> getUsersList()
    {
        ArrayList<Accounts> accountsList = new ArrayList<>();
        Cursor cursor = mWBDataBase.query(Accounts.TABLE, null, Accounts.RELATION + " != '"+Accounts.FRIEND+"'",null, null, null);
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
        return accountsList;
    }

    public ArrayList<Accounts> getList()
    {
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
        return accountsList;
    }


    public ArrayList<Accounts> getFriendsList()
    {
        ArrayList<Accounts> accountsList = new ArrayList<>();
        Cursor cursor = mWBDataBase.query(Accounts.TABLE, null, Accounts.RELATION+" = '"+Accounts.FRIEND+"' ",null, null, null);
        if (null != cursor)
        {
            while (cursor.moveToNext())
            {
                Accounts account = new Accounts();
                account.name = cursor.getString(cursor.getColumnIndex(Accounts.NAME));
                account.email = cursor.getString(cursor.getColumnIndex(Accounts.EMAIL));
                account.relation = cursor.getString(cursor.getColumnIndex(Accounts.RELATION));
                accountsList.add(account);
                account.photo_url = cursor.getString(cursor.getColumnIndex(Accounts.PHOTO_URL));
            }
            cursor.close();
        }
        return accountsList;
    }

    public void update(String email, String relation, String friend)
    {
        ContentValues values = new ContentValues();
        values.put(relation, friend);
        mWBDataBase.update(Accounts.TABLE, values, Accounts.EMAIL + " = '" + email + "'", null);
    }
}
