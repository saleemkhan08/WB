package in.org.whistleblower.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;

/**
 * Created by vishnu on 19-03-2016.
 */
public class AccountsDao {

    public static final String TABLE_SCHEMA = "CREATE TABLE " + Accounts.TABLE + " ("
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + Accounts.NAME + " VARCHAR(255), "
            + Accounts.EMAIL + " VARCHAR(255), "
            + Accounts.GOOGLE_ID + " VARCHAR(255), "
            + Accounts.PHOTO_URL + " VARCHAR(255),"
            + Accounts.TIME_STAMP + " VARCHAR(255), "
            + Accounts.RELATION + " VARCHAR(255))";
    private Context mContext;
    private WBDataBase mWBDataBase;

    public AccountsDao(Context context) {
        this.mContext = context;
        mWBDataBase = new WBDataBase(mContext);
    }

    public void insert(Accounts account) {
        if (null != account) {
            ContentValues values = new ContentValues();
            values.put(Accounts.NAME, account.name);
            values.put(Accounts.EMAIL, account.email);
            values.put(Accounts.GOOGLE_ID, account.googleId);
            values.put(Accounts.PHOTO_URL, account.photo_url);
            values.put(Accounts.TIME_STAMP, account.timeStamp);
            mWBDataBase.insert(Accounts.TABLE, values);
        }
    }

    public void delete() {
        mWBDataBase.delete(Accounts.TABLE, null, null);
    }

    public ArrayList<Accounts> getAccountsList() {
        ArrayList<Accounts> accountsList = new ArrayList<>();
        Cursor cursor = mWBDataBase.query(Accounts.TABLE, null, null, null, null, null);
        if (null != cursor) {
            while (cursor.moveToNext()) {
                Accounts account = new Accounts();
                account.name = cursor.getString(cursor.getColumnIndex(Accounts.NAME));
                account.email = cursor.getString(cursor.getColumnIndex(Accounts.EMAIL));
                account.googleId = cursor.getString(cursor.getColumnIndex(Accounts.GOOGLE_ID));
                account.photo_url = cursor.getString(cursor.getColumnIndex(Accounts.PHOTO_URL));
                accountsList.add(account);
            }
            cursor.close();
        }
        return accountsList;
    }

}
