package in.org.whistleblower;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import in.org.whistleblower.components.AppComponent;
import in.org.whistleblower.components.DaggerAppComponent;
import in.org.whistleblower.modules.MiscModule;

public class WhistleBlower extends android.app.Application
{
    private static WhistleBlower sInstance;
    public static Context context;
    @Override
    public void onCreate()
    {
        super.onCreate();
        sInstance = this;
        context = this.getApplicationContext();
    }

    public static AppComponent getComponent()
    {
        return DaggerAppComponent
                .builder()
                .miscModule(new MiscModule())
                .build();
    }

    public static SharedPreferences getPreferences()
    {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void toast(String str)
    {
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
    }
    public static Context getAppContext()
    {
        return sInstance.getApplicationContext();
    }
}

