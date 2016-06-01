package in.org.whistleblower.models;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;

public class FavPlacesDao
{
    public static final String ADD_SUCCESS_MSG = "Added to Favorite Places";
    public static final String TABLE = "FavPlaces";
    private static final String PLACE_TYPE_INDEX = "PLACE_TYPE_INDEX";
    public static final String PLACE_TYPE = "PLACE_TYPE";
    public static final String LATITUDE = "LATITUDE";
    public static final String LONGITUDE = "LONGITUDE";
    public static final String ADDRESS_LINE = "ADDRESS_LINE";
    public static final String POSTAL_CODE = "POSTAL_CODE";
    private static final String RADIUS = "RADIUS";
    public static final String TABLE_SCHEMA = "CREATE TABLE " + TABLE + "("
            + ADDRESS_LINE + " VARCHAR(255) PRIMARY KEY, "
            + PLACE_TYPE + " VARCHAR(255), "
            + PLACE_TYPE_INDEX + " INTEGER, "
            + RADIUS + " INTEGER, "
            + LONGITUDE + " VARCHAR(255), "
            + LATITUDE + " VARCHAR(255));";

    public static final String ALTER_TABLE_SCHEMA = TABLE_SCHEMA;
    private static final float OFFSET_LAT = 0.008983f;
    private static final float OFFSET_LNG = 0.015060f;

    WBDataBase mWBDataBase;

    public FavPlacesDao()
    {
        mWBDataBase = new WBDataBase();
    }

    public String insert(FavPlaces favPlaces)
    {
        ContentValues values = new ContentValues();
        values.put(FavPlacesDao.ADDRESS_LINE, favPlaces.addressLine);
        values.put(FavPlacesDao.PLACE_TYPE_INDEX, favPlaces.placeTypeIndex);
        values.put(FavPlacesDao.RADIUS, favPlaces.radius);
        values.put(FavPlacesDao.LONGITUDE, favPlaces.longitude);
        values.put(FavPlacesDao.LATITUDE, favPlaces.latitude);
        values.put(FavPlacesDao.PLACE_TYPE, favPlaces.placeType);

        if (-1 == mWBDataBase.insert(TABLE, values))
        {
            return "Couldn't Add \""+favPlaces.addressLine+"\" to Favorite Places";
        }
        return ADD_SUCCESS_MSG;
    }

    public void delete()
    {
        mWBDataBase.delete(TABLE, null, null);
    }

    public void delete(String address)
    {
        String whereClause = ADDRESS_LINE+" = ?";
        String whereArgs[] = {address};
        mWBDataBase.delete(TABLE, whereClause, whereArgs);
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

                favPlaces.addressLine = cursor.getString(cursor.getColumnIndex(FavPlacesDao.ADDRESS_LINE));
                favPlaces.placeTypeIndex = cursor.getInt(cursor.getColumnIndex(FavPlacesDao.PLACE_TYPE_INDEX));
                favPlaces.radius = cursor.getInt(cursor.getColumnIndex(FavPlacesDao.RADIUS));

                favPlaces.latitude = cursor.getString(cursor.getColumnIndex(FavPlacesDao.LATITUDE));
                favPlaces.longitude = cursor.getString(cursor.getColumnIndex(FavPlacesDao.LONGITUDE));
                favPlaces.placeType = cursor.getString(cursor.getColumnIndex(FavPlacesDao.PLACE_TYPE));
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
                LONGITUDE + " < " + (longitude + OFFSET_LNG) + " AND " + LONGITUDE + " > " + (longitude - OFFSET_LNG) +
                " AND " +
                LATITUDE + " < " + (latitude + OFFSET_LAT) + " AND " + LATITUDE + " > " + (latitude - OFFSET_LAT);
        Cursor cursor = mWBDataBase.query(query);
        if (null != cursor)
        {
            if (cursor.getCount() <= 0)
            {
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

        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        float dist = (float) (earthRadius * c);
        return dist;
    }
}
