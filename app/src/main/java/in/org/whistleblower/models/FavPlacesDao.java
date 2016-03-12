package in.org.whistleblower.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;

public class FavPlacesDao
{
    public static final String ADD_SUCCESS_MSG = "Added to Favorite Places";
    public static final String TABLE = "FavPlaces";
    public static final String COUNTRY = "COUNTRY";

    public static final String LOCALITY = "LOCALITY";
    public static final String SUB_LOCALITY = "SUB_LOCALITY";

    public static final String FEATURE_NAME = "FEATURE_NAME";

    public static final String LATITUDE = "LATITUDE";
    public static final String LONGITUDE = "LONGITUDE";

    public static final String ADDRESS_LINE1 = "ADDRESS_LINE1";
    public static final String ADDRESS_LINE0 = "ADDRESS_LINE0";

    public static final String ADMIN_AREA = "ADMIN_AREA";
    public static final String SUB_ADMIN_AREA = "SUB_ADMIN_AREA";

    public static final String POSTAL_CODE = "POSTAL_CODE";

    public static final String TABLE_SCHEMA = "CREATE TABLE " + TABLE + "("
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + FEATURE_NAME + " VARCHAR(255), "
            + ADDRESS_LINE0 + " VARCHAR(255), "
            + ADDRESS_LINE1 + " VARCHAR(255), "
            + SUB_LOCALITY + " VARCHAR(255), "
            + LOCALITY + " VARCHAR(255), "
            + SUB_ADMIN_AREA + " VARCHAR(255), "
            + ADMIN_AREA + " VARCHAR(255), "
            + COUNTRY + " VARCHAR(255), "
            + POSTAL_CODE + " VARCHAR(255), "
            + LONGITUDE + " REAL, "
            + LATITUDE + " REAL );";

    public static final String ALTER_TABLE_SCHEMA = TABLE_SCHEMA;
    private static final float OFFSET_LAT = 0.008983f;
    private static final float OFFSET_LNG = 0.015060f;
    //TODO write a query to save the old data and just alter the table.
    Context mContext;
    WBDataBase mWBDataBase;

    public FavPlacesDao(Context context)
    {
        mContext = context;
        mWBDataBase = new WBDataBase(mContext);
    }

    public String insert(FavPlaces favPlaces)
    {
        if(!favPlaceExists(favPlaces.longitude, favPlaces.latitude))
        {
            ContentValues values = new ContentValues();
            values.put(FavPlacesDao.FEATURE_NAME, favPlaces.featureName);
            values.put(FavPlacesDao.ADDRESS_LINE0, favPlaces.addressLine0);
            values.put(FavPlacesDao.ADDRESS_LINE1, favPlaces.addressLine1);
            values.put(FavPlacesDao.SUB_LOCALITY, favPlaces.subLocality);
            values.put(FavPlacesDao.LOCALITY, favPlaces.locality);
            values.put(FavPlacesDao.SUB_ADMIN_AREA, favPlaces.subAdminArea);
            values.put(FavPlacesDao.ADMIN_AREA, favPlaces.adminArea);
            values.put(FavPlacesDao.COUNTRY, favPlaces.country);
            values.put(FavPlacesDao.POSTAL_CODE, favPlaces.postalCode);
            values.put(FavPlacesDao.LONGITUDE, favPlaces.longitude);
            values.put(FavPlacesDao.LATITUDE, favPlaces.latitude);
            if(-1 == mWBDataBase.insert(TABLE, values))
            {
                return "Couldn't Add to Favorite Places";
            }
            return ADD_SUCCESS_MSG;
        }
        return "Already added to Favorites";
    }

    public void delete()
    {
        mWBDataBase.delete(TABLE, null, null);
    }

    public void delete(String whereClause)
    {
        mWBDataBase.delete(TABLE, whereClause, null);
    }

    public void update(ContentValues cv, String whereClause)
    {
        mWBDataBase.update(TABLE, cv, whereClause, null);
    }
    public ArrayList<FavPlaces> getFavPlacesList()
    {
        ArrayList<FavPlaces> favPlacesArrayList = new ArrayList<>();
        Cursor cursor = mWBDataBase.query(FavPlacesDao.TABLE, null, null, null, null, null);
        if (null != cursor)
        {
            while (cursor.moveToNext())
            {
                FavPlaces favPlaces = new FavPlaces();
                favPlaces.featureName = cursor.getString(cursor.getColumnIndex(FavPlacesDao.FEATURE_NAME));
                favPlaces.addressLine0 = cursor.getString(cursor.getColumnIndex(FavPlacesDao.ADDRESS_LINE0));
                favPlaces.addressLine1 = cursor.getString(cursor.getColumnIndex(FavPlacesDao.ADDRESS_LINE1));
                favPlaces.subLocality = cursor.getString(cursor.getColumnIndex(FavPlacesDao.SUB_LOCALITY));
                favPlaces.locality = cursor.getString(cursor.getColumnIndex(FavPlacesDao.LOCALITY));
                favPlaces.subAdminArea = cursor.getString(cursor.getColumnIndex(FavPlacesDao.SUB_ADMIN_AREA));
                favPlaces.adminArea = cursor.getString(cursor.getColumnIndex(FavPlacesDao.ADMIN_AREA));
                favPlaces.country = cursor.getString(cursor.getColumnIndex(FavPlacesDao.COUNTRY));
                favPlaces.postalCode = cursor.getString(cursor.getColumnIndex(FavPlacesDao.POSTAL_CODE));
                favPlaces.latitude = cursor.getFloat(cursor.getColumnIndex(FavPlacesDao.LATITUDE));
                favPlaces.longitude = cursor.getFloat(cursor.getColumnIndex(FavPlacesDao.LONGITUDE));
                favPlacesArrayList.add(favPlaces);
            }
            cursor.close();
        }
        mWBDataBase.closeDb();
        return favPlacesArrayList;
    }

    public boolean favPlaceExists(float longitude, float latitude)
    {
        String query = "SELECT * FROM " + TABLE + " WHERE " +
                LONGITUDE + " < " + (longitude+ OFFSET_LNG) + " AND " +LONGITUDE + " > " + (longitude - OFFSET_LNG) +
                " AND "+
                LATITUDE + " < " + (latitude+ OFFSET_LAT) + " AND " +LATITUDE + " > " + (latitude - OFFSET_LAT) ;
        Cursor cursor = mWBDataBase.query(query);
        if (null != cursor)
        {
            if(cursor.getCount() <= 0){
                cursor.close();
                return false;
            }
            cursor.close();
        }
        return true;
    }

    public static float distFrom(float lat1, float lng1, float lat2, float lng2)
    {
        double earthRadius = 6371000; //meters

        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        float dist = (float) (earthRadius * c);
        return dist;
    }
}
