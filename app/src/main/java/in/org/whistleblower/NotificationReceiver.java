package in.org.whistleblower;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import in.org.whistleblower.services.LocationTrackingService;

public class NotificationReceiver extends BroadcastReceiver
{
    public NotificationReceiver()
    {
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d("WhistleBlower", "Notification Clicked");
        Toast.makeText(context, "Notification Clicked", Toast.LENGTH_SHORT).show();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(LocationTrackingService.ALARM_NOTIFICATION);
        context.stopService(new Intent(context, LocationTrackingService.class));
    }
}
