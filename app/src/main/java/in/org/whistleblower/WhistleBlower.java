package in.org.whistleblower;

import android.content.Context;

import in.org.whistleblower.components.AppComponent;
import in.org.whistleblower.components.DaggerAppComponent;
import in.org.whistleblower.modules.MiscModule;

public class WhistleBlower extends android.app.Application
{
    private static WhistleBlower sInstance;
    @Override
    public void onCreate()
    {
        super.onCreate();
        sInstance = this;
    }

    public static AppComponent getComponent()
    {
        return DaggerAppComponent
                .builder()
                .miscModule(new MiscModule())
                .build();
    }

    public static Context getAppContext()
    {
        return sInstance.getApplicationContext();
    }
}

