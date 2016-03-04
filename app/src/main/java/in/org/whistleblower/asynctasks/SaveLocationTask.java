package in.org.whistleblower.asynctasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;

import in.org.whistleblower.fragments.MapFragment;
import in.org.whistleblower.models.FavPlaces;
import in.org.whistleblower.models.FavPlacesDao;
import in.org.whistleblower.utilities.FABUtil;

public class SaveLocationTask extends AsyncTask<String, Void, String>
{
    Context mContext;
    LatLng mLatLng;
    String placeName = "@Unknown Place";
    private SharedPreferences preferences;

    public SaveLocationTask(Context mContext, LatLng latLng)
    {
        this.mContext = mContext;
        this.mLatLng = latLng;
        preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    @Override
    protected String doInBackground(String... params)
    {
        Geocoder gcd = new Geocoder(mContext);
        try
        {
            Address address = gcd.getFromLocation(mLatLng.latitude, mLatLng.longitude, 1).get(0);
            if (address != null)
            {
                String addr = address.getAddressLine(1);
                if (addr.contains(address.getAdminArea()))
                {
                    addr = address.getAddressLine(0);
                }
                placeName = "@" + addr;
                if(params[0].equals(FABUtil.ADD_FAV_PLACE))
                {
                    FavPlacesDao favPlacesDao = new FavPlacesDao(mContext);
                    FavPlaces favPlace = new FavPlaces();
                    favPlace.featureName = address.getFeatureName();
                    favPlace.addressLine0 = address.getAddressLine(0);
                    favPlace.addressLine1 = address.getAddressLine(1);
                    favPlace.subLocality = address.getSubLocality();
                    favPlace.locality = address.getLocality();
                    favPlace.subAdminArea = address.getSubAdminArea();
                    favPlace.adminArea = address.getAdminArea();
                    favPlace.country = address.getCountryName();
                    favPlace.postalCode = address.getPostalCode();
                    favPlace.latitude = (float) mLatLng.latitude;
                    favPlace.longitude = (float) mLatLng.longitude;
                    return favPlacesDao.insert(favPlace);
                }
            }
        }
        catch (IOException e)
        {

            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result)
    {
        preferences.edit()
                .putFloat(MapFragment.LATITUDE, (float) mLatLng.latitude)
                .putFloat(MapFragment.LONGITUDE, (float) mLatLng.longitude)
                .putString(MapFragment.ADDRESS, placeName.trim())
                .apply();
        if(result != null)
        {
            Toast.makeText(mContext, result,Toast.LENGTH_SHORT).show();
        }
    }
}
