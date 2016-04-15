package in.org.whistleblower.asynctasks;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import in.org.whistleblower.interfaces.GeoCodeListener;
import in.org.whistleblower.utilities.MiscUtil;

public class GeoCoderTask extends AsyncTask<Integer, Void, String>
{
    Context mContext;
    LatLng mLatLng;
    GeoCodeListener mListener;

    public GeoCoderTask(Context context, LatLng latLng, GeoCodeListener listener)
    {
        mContext = context;
        mLatLng = latLng;
        mListener = listener;
    }

    @Override
    protected String doInBackground(Integer... params)
    {
        String address = "";
        try
        {
            if(MiscUtil.isConnected(mContext))
            {
                if (!isCancelled())
                {
                    Geocoder gcd = new Geocoder(mContext);
                    Thread.sleep(params[0] * 100);
                    List<Address> addresses = gcd.getFromLocation(mLatLng.latitude, mLatLng.longitude, 1);
                    if (addresses.size() > 0)
                    {
                        Address addressObj = addresses.get(0);
                        String addressLine = addressObj.getAddressLine(0);
                        if (null != addressLine)
                        {
                            address = addressLine;
                            addressLine = addressObj.getAddressLine(1);
                            if (addressLine != null)
                            {
                                address += ", " + addressLine;
                            }
                        }
                    }
                }
            }
            else
            {
                address = "No Internet...";
            }
        }
        catch (Exception e)
        {
            return null;
        }
        return address;
    }

    @Override
    protected void onCancelled()
    {
        super.onCancelled();
        mListener.onCancelled();
    }

    @Override
    protected void onPostExecute(String address)
    {
        if (address == null)
        {
            mListener.onGeoCodingFailed();
        }
        else
        {
            mListener.onAddressObtained(address);
        }
    }
}
