package in.org.whistleblower;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class WBActivity extends AppCompatActivity
{
    public static final String REQUEST_LOCATION_PERMISSION = "REQUEST_LOCATION_PERMISSION";
    private BroadcastReceiver mLocationReceiver;

    @Override
    protected void onStart()
    {
        super.onStart();
        registerLocationReceiver();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if (null != mLocationReceiver)
        {
            unregisterReceiver(mLocationReceiver);
        }
    }

    private void registerLocationReceiver()
    {
        mLocationReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                handleRequest(intent.getAction());
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(REQUEST_LOCATION_PERMISSION);
        registerReceiver(mLocationReceiver, intentFilter);
    }

    private void handleRequest(String request)
    {
        Toast.makeText(this, request, Toast.LENGTH_SHORT).show();
    }
}
