package in.org.whistleblower;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;

import in.org.whistleblower.fragments.MapFragment;

public class LocationTrackingService extends Service implements LocationListener
{
    public static final int ALARM_NOTIFICATION = 1001;

    public LocationTrackingService()
    {
    }

    NotificationManager notificationManager;

    @Override
    public void onCreate()
    {
        super.onCreate();
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);
        PendingIntent dismissIntent = PendingIntent.getBroadcast(this, (int) System.currentTimeMillis(), new Intent(this, NotificationReceiver.class), 0);

        // Using RemoteViews to bind custom layouts into Notification
        RemoteViews remoteViews = new RemoteViews(getPackageName(),
                R.layout.custom_notification);
        remoteViews.setOnClickPendingIntent(R.id.dismissAlarm, dismissIntent);

        Notification n = new Notification.Builder(this)
                .setSmallIcon(R.drawable.bullhorn)
                .setContentIntent(pIntent)
                .setOngoing(true)
                .setContent(remoteViews)
                .build();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(ALARM_NOTIFICATION, n);
    }

    private static final float OFFSET_LAT = 0.008983f;
    private static final float OFFSET_LNG = 0.015060f;
    SharedPreferences preferences;

    @Override
    public IBinder onBind(Intent intent)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Toast.makeText(this, "Service Stopped", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location)
    {
        Toast.makeText(this, "Longitude : "+ location.getLongitude(), Toast.LENGTH_SHORT).show();
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        Intent intent = new Intent();
        intent.setAction("LOCATION_UPDATE");
        intent.putExtra(MapFragment.LOCATION, location);
        intent.putExtra(MapFragment.LATITUDE, location.getLatitude());
        intent.putExtra(MapFragment.LONGITUDE, location.getLongitude());
        manager.sendBroadcast(new Intent());
        float expLat = preferences.getFloat(MapFragment.LATITUDE, 0),
                expLng = preferences.getFloat(MapFragment.LONGITUDE, 0),
                actLat = (float) location.getLatitude(),
                actLng = (float) location.getLongitude();

        if (expLng < (actLng + OFFSET_LNG) && expLng > (actLng - OFFSET_LNG)
                &&
                expLat < (actLat + OFFSET_LAT) && expLat < (actLat - OFFSET_LAT))
        {
            startActivity(new Intent(this, AlarmActivity.class));// Make it Notification with audio and vibration.
            stopSelf();
            notificationManager.cancel(ALARM_NOTIFICATION);
        }
    }
}
