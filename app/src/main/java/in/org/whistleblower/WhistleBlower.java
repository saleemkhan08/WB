package in.org.whistleblower;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDexApplication;
import android.widget.Toast;

import in.org.whistleblower.components.AppComponent;
import in.org.whistleblower.components.DaggerAppComponent;
import in.org.whistleblower.modules.MiscModule;


public class WhistleBlower extends MultiDexApplication
{
    private static Context context;
    private static Typeface typeface;
    @Override
    public void onCreate()
    {
        super.onCreate();
        context = this.getApplicationContext();
        typeface = Typeface.createFromAsset(getAssets(), "Gabriola.ttf");
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
        return context;
    }

    public static Typeface getTypeface()
    {
        return typeface;
    }
}

