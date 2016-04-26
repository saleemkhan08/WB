package in.org.whistleblower.modules;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import in.org.whistleblower.WhistleBlower;

@Module
public class MiscModule
{
    private final Context appContext;
    public MiscModule()
    {
        this.appContext = WhistleBlower.getAppContext();
    }

    @Singleton
    @Provides
    SharedPreferences provideSharedPreferences()
    {
        return PreferenceManager.getDefaultSharedPreferences(appContext);
    }
}
