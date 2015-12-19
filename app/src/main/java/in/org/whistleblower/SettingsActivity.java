package in.org.whistleblower;

import android.app.FragmentManager;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import in.org.whistleblower.fragments.SettingsPrefs;

public class SettingsActivity extends AppCompatActivity
{
    FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        fragmentManager = getFragmentManager();
        showFragment(new SettingsPrefs());
    }


    public void showFragment(PreferenceFragment fragment)
    {
        fragmentManager.beginTransaction()
                .replace(R.id.setting_container, fragment)
                .commit();
    }
}
