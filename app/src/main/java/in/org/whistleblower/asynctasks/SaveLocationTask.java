package in.org.whistleblower.asynctasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

import in.org.whistleblower.fragments.MapFragment;

public class SaveLocationTask extends AsyncTask<Void, Void, Void>
{
    Context mContext;
    LatLng mLatLng;
    String address = "@Unknown Place";
    private SharedPreferences preferences;

    public SaveLocationTask(Context mContext, LatLng latLng)
    {
        this.mContext = mContext;
        this.mLatLng = latLng;
        preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    @Override
    protected Void doInBackground(Void... params)
    {
        Geocoder gcd = new Geocoder(mContext);
        try
        {
            List<Address> addresses = gcd.getFromLocation(mLatLng.latitude, mLatLng.longitude, 1);
            if (addresses.size() > 0)
            {
                address = "@" + addresses.get(0).getFeatureName();
            }
        }
        catch (IOException e)
        {

            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid)
    {
        preferences.edit()
                .putFloat(MapFragment.LATITUDE, (float) mLatLng.latitude)
                .putFloat(MapFragment.LONGITUDE, (float) mLatLng.longitude)
                .putString(MapFragment.ADDRESS, address.trim())
                .apply();
        super.onPostExecute(aVoid);
    }
}
